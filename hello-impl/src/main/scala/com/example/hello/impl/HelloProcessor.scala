package com.example.hello.impl

import java.sql.Connection

import com.lightbend.lagom.scaladsl.persistence.{ AggregateEventTag, EventStreamElement, ReadSideProcessor }
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcReadSide

import scala.concurrent.ExecutionContext
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcSession.tryWith

/**
  *
  */
class HelloProcessor(readSide: JdbcReadSide)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[HelloEvent] {
  override def aggregateTags: Set[AggregateEventTag[HelloEvent]] = HelloEvent.Tag.allTags

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[HelloEvent] =
    readSide
      .builder[HelloEvent]("HelloEventOffset")
      .setGlobalPrepare(globalPrepare)
      .setEventHandler(handleGreetingChanged)
      .build()

  val globalPrepare: Connection => Unit = { connection =>
    tryWith(connection.prepareStatement("CREATE TABLE IF NOT EXISTS greetings ( " +
      "id VARCHAR(64), message VARCHAR(256), PRIMARY KEY (id))")) { ps =>
      ps.execute()
    }
  }
  val handleGreetingChanged: (Connection, EventStreamElement[GreetingMessageChanged]) => Unit = { (conn, evt) =>
    tryWith(conn.prepareStatement(
      """INSERT INTO greetings(id, message) VALUES (?, ?)
        |ON DUPLICATE KEY UPDATE
        |`message` = ?
    """.stripMargin)) { statement =>
      statement.setString(1, evt.entityId)
      statement.setString(2, evt.event.message)
      statement.setString(3, evt.event.message)
      statement.execute()
    }
  }

}

