package com.alisiikh.twitter.streaming

import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.storage.StorageLevel
import org.apache.spark.storage.StorageLevel.{MEMORY_AND_DISK_SER_2 => MADS2}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.receiver.Receiver
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder
import twitter4j.{FilterQuery, Status}

class TwitterReceiverInputDStream(ssc: StreamingContext, query: FilterQuery, storageLevel: StorageLevel = MADS2)
    extends ReceiverInputDStream[Status](ssc)
    with StrictLogging {

  private val auth = new OAuthAuthorization(new ConfigurationBuilder().build())

  override def getReceiver(): Receiver[Status] = new TwitterReceiver(auth, query, storageLevel)
}
