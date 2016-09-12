/**
  * Created by zezzy on 9/8/16.
  */

import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.feature.{HashingTF, IDF}

object featureExtraction {

  val conf = new SparkConf().setAppName("TfIdfExample")
  val sc = new SparkContext(conf)

  def main(args: Array[String]) {

    val topics = Map("business" -> 1, "computers" -> 2, "culture-arts-entertainment" -> 3,
      "education-science" -> 4, "engineering" -> 5, "health" -> 6, "politics-society" -> 7, "sports"->8)

    // read in data files (point to your location)
    val labeledTweets = sc.textFile("data/snippets/train.txt")

    val tokenizedTweets = labeledTweets.map { data =>
      val splits = data.split(':') // target : text
      (topics(splits(0).toLowerCase()).toDouble, Tokenizer.tokenize(splits(1)))
    }

    val stemmedTweet = tokenizedTweets.map{ tokenized =>
      ( tokenized._1, tokenized._2.map(Stemmer.stem) )
    }

    // convert data to numeric features with TF
    // we will consider Mockingbird passages = class 1, Watchman = class 0
    val tf = new HashingTF(10000)
    val data = stemmedTweet.map( dt => LabeledPoint(dt._1, tf.transform(dt._2)))


    // build IDF model and transform data into modeling sets
    val splits = data.randomSplit(Array(0.7, 0.3))
    val trainDocs = splits(0).map{ x=>x.features}
    val idfModel = new IDF(minDocFreq = 3).fit(trainDocs)
    val train = splits(0).map{ point=>
      LabeledPoint(point.label,idfModel.transform(point.features))
    }
    train.cache()

    val test = splits(1).map{ point =>
      LabeledPoint(point.label,idfModel.transform(point.features))
    }
    train.saveAsTextFile("data/tweetIDF_ModelingSet")

    println("Number of training examples: ", train.count())
    println("Number of test examples: ", test.count())

    // NAIVE BAYES MODEL
    println("Training Naive Bayes Model...")
    val nbmodel = NaiveBayes.train(train, lambda = 1.0)
    nbmodel.save(sc,"data/nvmTrained")
    val bayesTrain = train.map(p => (nbmodel.predict(p.features), p.label))
    val bayesTest = test.map(p => (nbmodel.predict(p.features), p.label))
    println("NB Training accuracy: ",bayesTrain.filter(x => x._1 == x._2).count() / bayesTrain.count().toDouble)
    println("NB Test set accuracy: ",bayesTest.filter(x => x._1 == x._2).count() / bayesTest.count().toDouble)
    println("Naive Bayes Confusion Matrix:")
//    println("Predict:mock,label:mock -> ",bayesTest.filter(x => x._1 == 1.0 & x._2==1.0).count())
//    println("Predict:watch,label:watch -> ",bayesTest.filter(x => x._1 == 0.0 & x._2==0.0).count())
//    println("Predict:mock,label:watch -> ",bayesTest.filter(x => x._1 == 1.0 & x._2==0.0).count())
//    println("Predict:watch,label:mock -> ",bayesTest.filter(x => x._1 == 0.0 & x._2==1.0).count())

  }
}

