package com.nulabinc.backlog.c2b.generators

import java.nio.charset.Charset

import com.nulabinc.backlog.c2b.datas._
import monix.reactive.Observable

object CSVRecordGenerator {

  val charset: Charset = Charset.forName("UTF-8")

  def backlogUserToByteArray(user: Observable[BacklogUser]): Observable[Array[Byte]] =
    user.map { item =>
      s""""${item.userId.getOrElse("")}",""\n""".stripMargin.getBytes(charset)
    }

  def cybozuUserToByteArray(user: Observable[CybozuUser]): Observable[Array[Byte]] =
    user.map { item =>
      s""""","${item.userId}"\n""".stripMargin.getBytes(charset)
    }

  def backlogPriorityToByteArray(priority: Observable[BacklogPriority]): Observable[Array[Byte]] =
    priority.map { u =>
      s""""","${u.name}"\n""".stripMargin.getBytes(charset)
    }

  def cybozuPriorityToByteArray(priority: Observable[CybozuPriority]): Observable[Array[Byte]] =
    priority.map { u =>
      s""""${u.value}",""\n""".stripMargin.getBytes(charset)
    }

  def statusToByteArray(status: Observable[BacklogStatus]): Observable[Array[Byte]] =
    status.map { u =>
      s""""${u.name}",""\n""".stripMargin.getBytes(charset)
    }

  def splitToByteArray(): Observable[Array[Byte]] =
    Observable {
      s""""----------------------","----------------------"\n""".stripMargin.getBytes(charset)
    }

}

/*
  Data flow:
    1. stream all issues, forums, events from DB
    2. hold creator, updater, assignee from each item(issue, forum, events)
    3. save to DB or hold on memory the collected users (distinct)
    4. all done after, get all users

 */
//class UserService()(implicit ctx: MappingContext) {
//
//  private val issueConverter = new IssueConverter()
//
//  def mustExist[A](a: Option[A]): Validated[String, A] =
//    a.map(_.valid).getOrElse(Invalid("error"))
//
//  def toBacklogIssue(issue: CybozuIssue): Free[StoreADT, Either[String, Either[ConvertError, BacklogIssue]]] = {
//    val storePrg = for {
//      maybeCreator <- StoreDSL.getUser(Id.userId(issue.creatorId))
//      maybeUpdater <- StoreDSL.getUser(Id.userId(issue.updaterId))
//      maybeAssignee <- issue.assigneeId.map(id => StoreDSL.getUser(Id.userId(id)))
//        .getOrElse(StoreDSL.empty)
//    } yield {
//      val validation =
//        mustExist(maybeCreator) product
//        mustExist(maybeUpdater)
//
//      validation.map {
//        case (creator, updater) =>
//          issueConverter.to(FromCybozuIssue(issue, creator, updater, maybeAssignee))
//      }.toEither
//    }
//    storePrg
//  }
//}
