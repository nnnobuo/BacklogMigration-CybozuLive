package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import monix.execution.Scheduler
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class AllTableOps()(implicit exc: Scheduler) {
  val commentTableOps = CommentTableOps()
  val eventTableOps = EventTableOps()
  val forumTableOps = ForumTableOps()
  val todoTableOps = TodoTableOps()
  val backlogUserTableOps = BacklogUserTableOps()
  val backlogPriorityTableOps = BacklogPriorityTableOps()
  val backlogStatusTableOps = BacklogStatusTableOps()
  val cybozuUserTableOps = CybozuUserTableOps()
  val cybozuIssueUserTableOps = CybozuIssueUserTableOps()

  val createDatabaseOps =
    DBIO.seq(
      todoTableOps.createTable,
      commentTableOps.createTable,
      eventTableOps.createTable,
      forumTableOps.createTable,
      backlogUserTableOps.createTable,
      backlogPriorityTableOps.createTable,
      backlogStatusTableOps.createTable,
      cybozuUserTableOps.createTable,
      cybozuIssueUserTableOps.createTable
    )
}