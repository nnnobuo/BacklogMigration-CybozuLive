package com.nulabinc.backlog.c2b.persistence.dsl

import cats.free.Free
import com.nulabinc.backlog.c2b.datas._
import monix.reactive.Observable

object StoreDSL {

  type StoreProgram[A] = Free[StoreADT, A]

  def pure[A](a: A): StoreProgram[A] =
    Free.liftF(Pure(a))

  def getUsers(offset: Long, size: Long): StoreProgram[Observable[Seq[CybozuUser]]] =
    Free.liftF(GetUsers(offset, size))

  def storeUser(user: CybozuUser): StoreProgram[Unit] =
    Free.liftF(StoreUser(user))

  def getIssues(limit: Int, start: Int = 0, step: Int = 100): StoreProgram[Observable[Seq[CybozuIssue]]] =
    Free.liftF(GetIssues(limit, start, step))

  def storeIssue(issue: CybozuIssue): StoreProgram[Unit] =
    Free.liftF(StoreIssue(issue))

  def getIssueComments(issue: CybozuIssue): StoreProgram[Observable[Seq[CybozuComment]]] =
    Free.liftF(GetIssueComments(issue))

  def storeIssueComment(issue: CybozuIssue, comment: CybozuComment): StoreProgram[Unit] =
    Free.liftF(StoreIssueComment(issue, comment))

//  def getPriorities: StoreProgram[Observable[CybozuCSVPriority]] =
//    Free.liftF(GetPriorities)
//
//  def storePriority(priority: CybozuCSVPriority): StoreProgram[Unit] =
//    Free.liftF(StorePriority(priority))
//
//  def getStatuses: StoreProgram[Observable[CybozuCSVStatus]] =
//    Free.liftF(GetStatuses)
//
//  def storeStatus(status: CybozuCSVStatus): StoreProgram[Unit] =
//    Free.liftF(StoreStatus(status))

  def getEvents(limit: Int, start: Int = 0, step: Int = 100): StoreProgram[Observable[Seq[CybozuEvent]]] =
    Free.liftF(GetEvents(limit, start, step))

  def storeEvent(event: CybozuEvent): StoreProgram[Unit] =
    Free.liftF(StoreEvent(event))

  def getForums(limit: Int, start: Int = 0, step: Int = 100): StoreProgram[Observable[Seq[CybozuForum]]] =
    Free.liftF(GetForums(limit, start, step))

  def storeForum(forum: CybozuForum): StoreProgram[Unit] =
    Free.liftF(StoreForum(forum))

}
