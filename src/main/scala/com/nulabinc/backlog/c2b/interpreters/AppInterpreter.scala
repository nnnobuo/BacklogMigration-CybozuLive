package com.nulabinc.backlog.c2b.interpreters

import cats.free.Free
import cats.~>
import com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL.StorageProgram
import com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram
import com.nulabinc.backlog.c2b.persistence.interpreters._
import monix.eval.Task

sealed trait AppADT[+A]
case class Pure[A](a: A) extends AppADT[A]
case class FromStorage[A](prg: StorageProgram[A]) extends AppADT[A]
case class FromDB[A](prg: StoreProgram[A]) extends AppADT[A]

object AppDSL {
  type AppProgram[A] = Free[AppADT, A]

  def pure[A](a: A): AppProgram[A] =
    Free.liftF(Pure(a))

  /*
    Renamed

    [error] cybozulive-importer/src/main/scala/com/nulabinc/backlog/c2b/interpreters/AppInterpreter.scala:25:7: double definition:
    [error] def from[A](dbProgram: com.nulabinc.backlog.c2b.persistence.dsl.StoreDSL.StoreProgram[A]): com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram[A] at line 22 and
    [error] def from[A](storageProgram: com.nulabinc.backlog.c2b.persistence.dsl.StorageDSL.StorageProgram[A]): com.nulabinc.backlog.c2b.interpreters.AppDSL.AppProgram[A] at line 25
    [error] have same type after erasure: (dbProgram: cats.free.Free)cats.free.Free
    [error]   def from[A](storageProgram: StorageProgram[A]): AppProgram[A] =
    [error]       ^

   */

  def fromDB[A](dbProgram: StoreProgram[A]): AppProgram[A] =
    Free.liftF(FromDB(dbProgram))

  def fromStorage[A](storageProgram: StorageProgram[A]): AppProgram[A] =
    Free.liftF(FromStorage(storageProgram))
}

class AppInterpreter(storageInterpreter: StorageInterpreter,
                     dbInterpreter: DBInterpreter) extends (AppADT ~> Task) {

  def run[A](appProgram: AppProgram[A]): Task[A] =
    appProgram.foldMap(this)

  override def apply[A](fa: AppADT[A]): Task[A] = fa match {
    case Pure(a) => Task(a)
    case FromStorage(storePrg) =>
      storageInterpreter.run(storePrg)
    case FromDB(dbPrg) =>
      dbInterpreter.run(dbPrg)
  }
}
