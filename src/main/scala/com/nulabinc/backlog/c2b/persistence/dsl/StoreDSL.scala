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

  lazy val getCybozuPriorities: StoreProgram[Observable[CybozuPriority]] =
    Free.liftF(GetCybozuPriorities)

  lazy val getCybozuStatuses: StoreProgram[Observable[CybozuStatus]] =
    Free.liftF(GetCybozuStatuses)

  lazy val getBacklogPriorities: StoreProgram[Observable[BacklogPriority]] =
    Free.liftF(GetBacklogPriorities)

  lazy val getBacklogStatuses: StoreProgram[Observable[BacklogStatus]] =
    Free.liftF(GetBacklogStatuses)

  //
  // Cybozu To-DO
  //
  lazy val getTodos: StoreProgram[Observable[CybozuDBTodo]] =
    Free.liftF(GetTodos)

  lazy val getTodoCount: StoreProgram[Int] =
    Free.liftF(GetTodoCount)

  def getTodo(id: Id[CybozuTodo]): StoreProgram[Option[CybozuTodo]] =
    Free.liftF(GetTodo(id))

  def storeTodo(todo: CybozuDBTodo): StoreProgram[AnyId] =
    Free.liftF(StoreTodo(todo))

  def storeTodoAssignees(todoId: AnyId, assigneeIds: Seq[AnyId]): StoreProgram[Int] =
    Free.liftF(StoreTodoAssignees(todoId, assigneeIds))

  //
  // Comment
  //
  def storeComment(comment: CybozuDBComment, commentType: CommentType): StoreProgram[AnyId] =
    Free.liftF(StoreComment(comment, commentType))

  def storeComments(comments: Seq[CybozuDBComment], commentType: CommentType): StoreProgram[Seq[AnyId]] =
    Free.liftF(StoreComments(comments, commentType))

  //
  // Cybozu Event
  //
  lazy val getEvents: StoreProgram[Observable[CybozuDBEvent]] =
    Free.liftF(GetEvents)

  lazy val getEventCount: StoreProgram[Int] =
    Free.liftF(GetEventCount)

  def getEvent(id: AnyId): StoreProgram[Option[CybozuEvent]] =
    Free.liftF(GetEvent(id))

  def storeEvent(event: CybozuDBEvent): StoreProgram[AnyId] =
    Free.liftF(StoreEvent(event))

  //
  // Cybozu Forum
  //
  lazy val getForums: StoreProgram[Observable[CybozuDBForum]] =
    Free.liftF(GetForums)

  lazy val getForumCount: StoreProgram[Int] =
    Free.liftF(GetForumCount)

  def getForum(id: AnyId): StoreProgram[Option[CybozuForum]] =
    Free.liftF(GetForum(id))

  def storeForum(forum: CybozuDBForum): StoreProgram[AnyId] =
    Free.liftF(StoreForum(forum))

  //
  // Cybozu Chat
  //
  lazy val getChats: StoreProgram[Observable[CybozuDBChat]] =
    Free.liftF(GetChats)

  lazy val getChatCount: StoreProgram[Int] =
    Free.liftF(GetChatCount)

  def getChat(id: AnyId): StoreProgram[Option[CybozuChat]] =
    Free.liftF(GetChat(id))

  def storeChat(chat: CybozuDBChat): StoreProgram[AnyId] =
    Free.liftF(StoreChat(chat))

  //
  // Cybozu user
  //
  lazy val getCybozuUsers: StoreProgram[Observable[CybozuUser]] =
    Free.liftF(GetCybozuUsers)

  def getCybozuUserById(id: AnyId): StoreProgram[Option[CybozuUser]] =
    Free.liftF(GetCybozuUserById(id))

  def getCybozuUserByKey(key: String): StoreProgram[Option[CybozuUser]] =
    Free.liftF(GetCybozuUserBykey(key))

  def storeCybozuUser(user: CybozuUser, writeType: WriteType): StoreProgram[AnyId] =
    Free.liftF(StoreCybozuUser(user, writeType))

  //
  // Backlog user
  //
  lazy val getBacklogUsers: StoreProgram[Observable[BacklogUser]] =
    Free.liftF(GetBacklogUsers)

  def storeBacklogUser(user: BacklogUser): StoreProgram[AnyId] =
    Free.liftF(StoreBacklogUser(user))

  def storeBacklogPriorities(priorities: Seq[BacklogPriority]): StoreProgram[Seq[AnyId]] =
    Free.liftF(StoreBacklogPriorities(priorities))

  def storeBacklogStatuses(statuses: Seq[BacklogStatus]): StoreProgram[Seq[AnyId]] =
    Free.liftF(StoreBacklogStatuses(statuses))

  def writeDBStream[A](stream: Observable[StoreProgram[A]]): StoreProgram[Unit] =
    Free.liftF[StoreADT, Unit](WriteDBStream(stream))
}
