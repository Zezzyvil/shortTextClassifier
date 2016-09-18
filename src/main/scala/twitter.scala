
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}

import org.apache.spark.streaming.twitter._
import org.apache.spark.SparkConf

object TwitterByTags {
  def main(args: Array[String]) {
    if (args.length < 4) {
      System.err.println("Usage: TwitterTags <consumer key> <consumer secret> " +
        "<access token> <access token secret> [<filters>]")
      System.exit(1)
    }

    val filters = args.takeRight(args.length - 4)


    val consumerKey = "tKpqCHVI1zLTqkNz3BOCo1u3i"
    val consumerSecret = "YpIsimn09zgpJfr4Fkp53pmJAjGkiT7V1QLAnruPcyCuyu1hE4"
    val accessToken = "370274844-myp3uE998LsjsYNqI5t8kQQXSN4cL3uvVKVmbCYF"
    val accessTokenSecret = "bLNdv2QEq03ncpvhHB8iCPiwIitatn9BkVwYJRvngt6y3"


    // Set the system properties so that Twitter4j library used by twitter stream
    // can use them to generat OAuth credentials
    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)

    val sparkConf = new SparkConf().setAppName("TwitterTags")
    val ssc = new StreamingContext(sparkConf, Seconds(8))
    val stream = TwitterUtils.createStream(ssc, None, filters)

    val hashTags = stream.flatMap(status => status.getText.split(" ").filter(_.startsWith("#")))

    val topCounts60 = hashTags.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(600))
                     .map{case (topic, count) => (count, topic)}
                     .transform(_.sortByKey(false))

    // Print popular hashtags
    topCounts60.foreachRDD(rdd => {
      val topList = rdd.take(10)
      println("\nPopular topics in last 60 seconds (%s total):".format(rdd.count()))
      topList.foreach{case (count, tag) => println("%s (%s tweets)".format(tag, count))}
    })

    ssc.start()
    ssc.awaitTermination()
  }
}


import java.io.File

import com.google.gson.Gson
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Collect at least the specified number of tweets into json text files.
 */
object CollectTweets {
  private var numTweetsCollected = 0L
  private var partNum = 0
  private val gson = new Gson()

  def main(args: Array[String]) {
    // Process program arguments and set properties
//    if (args.length < 3) {
//      System.err.println("Usage: " + this.getClass.getSimpleName +
//        "<outputDirectory> <numTweetsToCollect> <intervalInSeconds> <partitionsEachInterval>")
//      System.exit(1)
//    }

    val outputDir = new File("data/savedTweets")
    if (outputDir.exists()) {
      System.err.println("ERROR - data/savedTweets already exists: delete or specify another directory")
      System.exit(1)
    }
    outputDir.mkdirs()

    val consumerKey = "tKpqCHVI1zLTqkNz3BOCo1u3i"
    val consumerSecret = "YpIsimn09zgpJfr4Fkp53pmJAjGkiT7V1QLAnruPcyCuyu1hE4"
    val accessToken = "370274844-myp3uE998LsjsYNqI5t8kQQXSN4cL3uvVKVmbCYF"
    val accessTokenSecret = "bLNdv2QEq03ncpvhHB8iCPiwIitatn9BkVwYJRvngt6y3"


    // Set the system properties so that Twitter4j library used by twitter stream
    // can use them to generat OAuth credentials
    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)

    println("Initializing Streaming Spark Context...")
    val conf = new SparkConf().setAppName(this.getClass.getSimpleName)
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(3))

    val tweetStream = TwitterUtils.createStream(ssc, None).filter(_.getLang == "en")
      .map(_.getText)

    tweetStream.foreachRDD((rdd, time) => {
      val count = rdd.count()
      if (count > 0) {
        val outputRDD = rdd.repartition(10)
        outputRDD.saveAsTextFile("data/savedTweets/tweets_" + time.milliseconds.toString)
        numTweetsCollected += count
        if (numTweetsCollected > 10000) {
          System.exit(0)
        }
      }
    })

    ssc.start()
    ssc.awaitTermination()
  }
}