package com.alisiikh.twitter.streaming

import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver
import twitter4j._
import twitter4j.auth.Authorization

import scala.util.Try
import scala.util.control.NonFatal

private[streaming] class TwitterReceiver(auth: Authorization, query: FilterQuery, storageLevel: StorageLevel)
    extends Receiver[Status](storageLevel)
    with StrictLogging {

  @volatile private var stopped: Boolean = false
  @volatile private var stream: TwitterStream = _

  override def onStart(): Unit =
    Try {
      val newStream = new TwitterStreamFactory().getInstance(auth)
      newStream.addListener(new StatusListener {
        override def onStatus(status: Status): Unit = store(status)
        override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {}
        override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {}
        override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = {}
        override def onStallWarning(warning: StallWarning): Unit = {}
        override def onException(ex: Exception): Unit = if (!stopped) restart("Error fetching tweets", ex)
      })
      newStream.filter(query)

      stream = newStream
      stopped = false
    }.recover {
      case NonFatal(ex) => restart("Error starting stream of tweets", ex)
    }

  override def onStop(): Unit = {
    stopped = true

    if (stream != null) {
      stream.shutdown()
    }
  }
}
