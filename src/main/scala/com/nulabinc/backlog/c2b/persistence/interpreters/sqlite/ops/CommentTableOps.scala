package com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.ops

import com.nulabinc.backlog.c2b.datas.CybozuDBComment
import com.nulabinc.backlog.c2b.datas.Types.AnyId
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.core.DBIOTypes.DBIOStream
import com.nulabinc.backlog.c2b.persistence.interpreters.sqlite.tables.CommentTable
import monix.execution.Scheduler
import slick.jdbc.SQLiteProfile.api._

private[sqlite] case class CommentTableOps()(implicit exc: Scheduler) extends BaseTableOps[CybozuDBComment, CommentTable] {

  protected val tableQuery = TableQuery[CommentTable]

  def streamByParentId(id: AnyId): DBIOStream[CybozuDBComment] =
    tableQuery
      .filter(_.parentId === id)
      .sortBy(_.id.desc) // Comments are stored by desc. It means getting by asc.
      .result

}