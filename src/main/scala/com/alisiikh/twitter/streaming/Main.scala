package com.alisiikh.twitter.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import twitter4j.{FilterQuery, Status}

/**
 * For the program to work twitter's OAuth credentials are required.
 * They could be provided by the following system properties:
 * -Dtwitter4j.oauth.consumerKey
 * -Dtwitter4j.oauth.consumerSecret
 * -Dtwitter4j.oauth.accessToken
 * -Dtwitter4j.oauth.accessTokenSecret
 */
object Main extends App with TweetFilters {
  val conf = new SparkConf().setAppName("tweets").setMaster("local[*]")
  val batchDuration = Seconds(5)

  val ssc = new StreamingContext(conf, batchDuration)

  val query = new FilterQuery().track(args: _*)

  new TwitterReceiverInputDStream(ssc, query)
    .filter(isRetweet)
    .filter(hasLang("en"))
    .filter(hasFollowers(100))
    .foreachRDD {
      _.foreach { tweet =>
        println(s"${tweet.getUser.getScreenName}: ${normalizeText(tweet.getText)}")
      }
    }

  ssc.start()
  ssc.awaitTermination()
}

private[streaming] sealed trait TweetFilters {
  import TweetFilters._

  def normalizeText(text: String): String = text.replaceAll("\r\n|\n", " ")
  def hasPhotos: Filter = _.getMediaEntities.nonEmpty
  def hasFollowers(atLeast: Int): Filter = _.getUser.getFollowersCount >= atLeast
  def hasLang(lang: String): Filter = _.getLang == lang
  def isRetweet: Filter = _.isRetweet
}
object TweetFilters {
  type Filter = Status => Boolean
}
