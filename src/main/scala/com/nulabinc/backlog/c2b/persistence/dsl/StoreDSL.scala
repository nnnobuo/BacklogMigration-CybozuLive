package com.nulabinc.backlog.c2b.persistence.dsl

import cats.free.Free
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.datas._
import monix.reactive.Observable

object StoreDSL {

  type StoreProgram[A] = Free[StoreADT, A]

  def empty[A]: StoreProgram[Option[A]] =
    Free.liftF(Pure(None))

  def pure[A](a: A): StoreProgram[A] =
    Free.liftF(Pure(a))

  lazy val createDatabase: StoreProgram[Unit] =
    Free.liftF(CreateDatabase)

  lazy val getIssues: StoreProgram[Observable[CybozuIssue]] =
    Free.liftF(GetIssues)

  lazy val getEvents: StoreProgram[Observable[CybozuEvent]] =
    Free.liftF(GetEvents)

  lazy val getForums: StoreProgram[Observable[CybozuForum]] =
    Free.liftF(GetForums)

  lazy val getCybozuPriorities: StoreProgram[Seq[CybozuPriority]] =
    Free.liftF(GetCybozuPriorities)

  lazy val getBacklogUsers: StoreProgram[Observable[BacklogUser]] =
    Free.liftF(GetBacklogUsers)

  lazy val getBacklogPriorities: StoreProgram[Observable[BacklogPriority]] =
    Free.liftF(GetBacklogPriorities)

  lazy val getBacklogStatuses: StoreProgram[Observable[BacklogStatus]] =
    Free.liftF(GetBacklogStatuses)

  def storeIssue(issue: CybozuIssue): StoreProgram[Option[AnyId]] =
    Free.liftF(StoreIssue(issue))

  def getIssueComments(issue: CybozuIssue): StoreProgram[Observable[CybozuComment]] =
    Free.liftF(GetIssueComments(issue))

  def storeIssueComment(comment: CybozuComment): StoreProgram[Option[AnyId]] =
    Free.liftF(StoreComment(comment))

  def storeComments(comments: Seq[CybozuComment]): StoreProgram[Int] =
    Free.liftF(StoreComments(comments))

  def storeEvent(event: CybozuEvent): StoreProgram[Option[AnyId]] =
    Free.liftF(StoreEvent(event))

  def storeForum(forum: CybozuForum): StoreProgram[Option[AnyId]] =
    Free.liftF(StoreForum(forum))

  def storeCybozuUser(user: CybozuUser): StoreProgram[Option[AnyId]] =
    Free.liftF(StoreCybozuUser(user))

  def writeDBStream[A](stream: Observable[StoreProgram[A]]): StoreProgram[A] =
    Free.liftF[StoreADT, A](WriteDBStream(stream))

  def storeBacklogUser(user: BacklogUser): StoreProgram[Option[AnyId]] =
    Free.liftF(StoreBacklogUser(user))

  def storeBacklogPriorities(priorities: Seq[BacklogPriority]): StoreProgram[Int] =
    Free.liftF(StoreBacklogPriorities(priorities))

  def storeBacklogStatuses(statuses: Seq[BacklogStatus]): StoreProgram[Int] =
    Free.liftF(StoreBacklogStatuses(statuses))

}
