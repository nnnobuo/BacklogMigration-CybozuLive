package com.nulabinc.backlog.c2b.services

import java.io.InputStream
import java.nio.file.Path

import scala.collection.JavaConverters._
import com.nulabinc.backlog.c2b.Config
import com.nulabinc.backlog.c2b.core.Logger
import com.nulabinc.backlog.c2b.datas.MappingContext
import com.nulabinc.backlog.c2b.interpreters.{AppDSL, ConsoleDSL}
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.persistence.dsl.{StorageDSL, StoreDSL}
import com.nulabinc.backlog.c2b.serializers.{CSVRecordSerializer, Serializer}
import com.nulabinc.backlog.c2b.services.DiffPatch.DiffMap
import com.osinka.i18n.Messages
import monix.reactive.Observable
import org.apache.commons.csv.CSVParser

import scala.collection.immutable.HashMap

object MappingFiles {
  import com.nulabinc.backlog.c2b.syntax.AppProgramOps._
  import CSVRecordSerializer._

  def write(config: Config): AppProgram[Unit] =
    for {
      _ <- writeUserMapping(config)
      _ <- writePriorityMapping(config)
      _ <- writeStatusMapping(config)
    } yield ()

  def read(path: Path): AppProgram[Observable[(String, String)]] =
    AppDSL.pure(
      Observable.fromIterator(CSVParser.parse(path.toFile, Config.charset, Config.csvFormat).iterator().asScala)
        .drop(1)
        .map(record => (record.get(0), record.get(1)))
    )

  def createMappingContext(config: Config): AppProgram[MappingContext] =
    for {
      userMappingStream <- read(config.USERS_PATH)
      users <- AppDSL.streamAsSeq(userMappingStream).orFail
      userMappings = indexSeqToHashMap(users)
      priorityMappingStream <- read(config.PRIORITIES_PATH)
      priorities <- AppDSL.streamAsSeq(priorityMappingStream).orFail
      priorityMappings = indexSeqToHashMap(priorities)
      statusMappingStream <- read(config.STATUSES_PATH)
      statuses <- AppDSL.streamAsSeq(statusMappingStream).orFail
      statusMappings = indexSeqToHashMap(statuses)
    } yield {
      MappingContext(
        userMappings = userMappings,
        priorityMappings = priorityMappings,
        statusMappings = statusMappings
      )
    }

  private def readCSVFile(is: InputStream): HashMap[String, String] = {
    val parser = CSVParser.parse(is, Config.charset, Config.csvFormat)
    parser.getRecords.asScala.foldLeft(HashMap.empty[String, String]) {
      case (acc, record) =>
        acc + (record.get(0) -> record.get(1))
    }
  }

  private def getOldRecords(path: Path): AppProgram[HashMap[String, String]] =
    for {
      oldExists <- mappingFileExists(path)
      oldRecords <- if (oldExists) {
        AppDSL.fromStorage(
          StorageDSL.readFile(path, readCSVFile)
        )
      } else {
        AppDSL.pure(HashMap.empty[String, String])
      }
    } yield oldRecords

  private def writeUserMapping(config: Config): AppProgram[Unit] = {
    for {
      _ <- AppDSL.fromStorage(
        StorageDSL.copy(config.USERS_PATH, config.USERS_TEMP_PATH)
      )
      cybozuUsersStream <- AppDSL.fromStore(StoreDSL.getCybozuUsers)
      cybozuUsers <- AppDSL.streamAsSeq(cybozuUsersStream).orFail
      newCybozuUsersMap = cybozuUsers.foldLeft(HashMap.empty[String, String]) {
        case (acc, cybozuUser) =>
          acc + (cybozuUser.userId -> "")
      }
      oldCybozuUserMap <- getOldRecords(config.USERS_TEMP_PATH)
      mergedCybozuUserMap = DiffPatch.applyChanges(oldCybozuUserMap, newCybozuUsersMap)
      _ <- writeDiffMap(config.USERS_PATH, mergedCybozuUserMap)
      backlogUserStream <- AppDSL.fromStore(StoreDSL.getBacklogUsers)
      _ <- writeMappingFile(config.BACKLOG_USER_PATH, "User", backlogUserStream)
    } yield ()
  }

  private def writePriorityMapping(config: Config): AppProgram[Unit] =
    for {
      _ <- AppDSL.fromStorage(
        StorageDSL.copy(config.PRIORITIES_PATH, config.PRIORITIES_TEMP_PATH)
      )
      cybozuPrioritiesStream <- AppDSL.fromStore(StoreDSL.getCybozuPriorities)
      cybozuPriorities <- AppDSL.streamAsSeq(cybozuPrioritiesStream).orFail
      newCybozuPrioritiesMap = cybozuPriorities.foldLeft(HashMap.empty[String, String]) {
        case (acc, cybozuPriority) =>
          acc + (cybozuPriority.value -> "")
      }
      oldCybozuPriorityMap <- getOldRecords(config.PRIORITIES_TEMP_PATH)
      mergedCybozuPriorityMap = DiffPatch.applyChanges(oldCybozuPriorityMap, newCybozuPrioritiesMap)
      _ <- writeDiffMap(config.PRIORITIES_PATH, mergedCybozuPriorityMap)
      backlogPriorityStream <- AppDSL.fromStore(StoreDSL.getBacklogPriorities)
      _ <- writeMappingFile(config.BACKLOG_PRIORITY_PATH, "Priority", backlogPriorityStream)
    } yield ()

  private def writeStatusMapping(config: Config): AppProgram[Unit] =
    for {
      _ <- AppDSL.fromStorage(
        StorageDSL.copy(config.STATUSES_PATH, config.STATUSES_TEMP_PATH)
      )
      cybozuStatusesStream <- AppDSL.fromStore(StoreDSL.getCybozuStatuses)
      cybozuStatuses <- AppDSL.streamAsSeq(cybozuStatusesStream).orFail
      newCybozuStatusesMap = cybozuStatuses.foldLeft(HashMap.empty[String, String]) {
        case (acc, cybozuStatus) =>
          acc + (cybozuStatus.value -> "")
      }
      oldCybozuStatusesMap <- getOldRecords(config.STATUSES_TEMP_PATH)
      mergedCybozuStatusMap = DiffPatch.applyChanges(oldCybozuStatusesMap, newCybozuStatusesMap)
      _ <- writeDiffMap(config.STATUSES_PATH, mergedCybozuStatusMap)
      backlogStatusStream <- AppDSL.fromStore(StoreDSL.getBacklogStatuses)
      _ <- writeMappingFile(config.BACKLOG_STATUS_PATH, "Status", backlogStatusStream)
    } yield ()

  private def indexSeqToHashMap(seq: IndexedSeq[(String, String)]): HashMap[String, String] =
    HashMap(seq map { a => a._1 -> a._2 }: _*)

  private def mappingFileExists(path: Path): AppProgram[Boolean] =
    AppDSL.fromStorage(StorageDSL.exists(path))

  private def writeDiffMap(path: Path, diffMap: DiffMap): AppProgram[Unit] =
    AppDSL.fromStorage(
      StorageDSL.writeAppendFile(
        path = path,
        writeStream =
          Observable(CSVRecordSerializer.header) ++
          Observable.fromIterator(
            CSVRecordSerializer.serializeMap(diffMap).iterator
          )
      )
    )

  private def writeMappingFile[A](path: Path,
                                  mappingFileKind: String,
                                  stream: Observable[A])
                                 (implicit serializer: Serializer[A, Array[Byte]]): AppProgram[Unit] =
    AppDSL.fromStorage(
      StorageDSL.writeNewFile(
        path = path,
        writeStream =
          Observable(CSVRecordSerializer.backlogHeader(mappingFileKind)) ++
          stream.map(value => CSVRecordSerializer.serialize(value))
      )
    )
}

object MappingFileConsole extends Logger {

  private val mappingResultBorder: AppProgram[Unit] =
    AppDSL.fromConsole(
      ConsoleDSL.print("\n--------------------------------------------------")
    )

  def show(config: Config): AppProgram[Unit] = {
    import MappingFiles._

    for {
      _ <- mappingResultBorder
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("mapping.output_file", Messages("name.mapping.user")) + "\n"))
      userStream <- read(config.USERS_PATH)
      _ <- AppDSL.consumeStream(
        userStream.map(mappingResult)
      )
      _ <- mappingResultBorder
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("mapping.output_file", Messages("name.mapping.priority")) + "\n"))
      priorityStream <- read(config.PRIORITIES_PATH)
      _ <- AppDSL.consumeStream(
        priorityStream.map(mappingResult)
      )
      _ <- mappingResultBorder
      _ <- AppDSL.fromConsole(ConsoleDSL.print(Messages("mapping.output_file", Messages("name.mapping.status")) + "\n"))
      statusStream <- read(config.STATUSES_PATH)
      _ <- AppDSL.consumeStream(
        statusStream.map(mappingResult)
      )
      _ <- mappingResultBorder
    } yield ()
  }

  private def mappingResult(row: (String, String)): AppProgram[Unit] =
    AppDSL.fromConsole(
      ConsoleDSL.print(s"${row._1} => ${row._2}")
    )
}