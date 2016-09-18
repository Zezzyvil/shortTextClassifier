/**
  * Created by zezzy on 9/8/16.
  */

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.VectorIndexer
import org.apache.spark.ml.regression.{RandomForestRegressionModel, RandomForestRegressor}
import org.apache.spark.mllib.classification.{LogisticRegressionWithLBFGS, NaiveBayes}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.feature.{HashingTF, IDF}

object featureExtraction {

  val conf = new SparkConf().setAppName("TweetClassification")
  val sc = new SparkContext(conf)
  sc.setLogLevel("ERROR")
  val sql = new SQLContext(sc)

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

    //see content of
    println("\n\n"+sql.createDataFrame(data).show(4))

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

  //  train.saveAsTextFile("data/tweetIDF_ModelingSet")

    println("Number of training examples: ", train.count())
    println("Number of test examples: ", test.count())

    // NAIVE BAYES MODEL
    println("\nTraining Naive Bayes Model...")
    val nbmodel = NaiveBayes.train(train, lambda = 1.0)
//    nbmodel.save(sc,"data/nvmTrained")
    val bayesTrain = train.map(p => (nbmodel.predict(p.features), p.label))
    val bayesTest = test.map(p => (nbmodel.predict(p.features), p.label))
    println("NB Training accuracy: ",bayesTrain.filter(x => x._1 == x._2).count() / bayesTrain.count().toDouble)
    println("NB Test set accuracy: ",bayesTest.filter(x => x._1 == x._2).count() / bayesTest.count().toDouble)

//    // RANDOM FOREST MODEL
//    println("Training Random Forest Regression Model...")
//    val categoricalFeaturesInfo = Map[Int, Int]()
//    val featureSubsetStrategy = "auto"
//    val impurity = "variance"
//    val maxDepth = 10
//    val maxBins = 32
//    val numTrees = 50
//    val modelRF = RandomForest.trainRegressor(train, categoricalFeaturesInfo, numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)
//
//    // Calculating random forest metrics
//    val trainScores = train.map { point =>
//      val prediction = modelRF.predict(point.features)
//      (prediction, point.label)
//    }
//    val testScores = test.map { point =>
//      val prediction = modelRF.predict(point.features)
//      (prediction, point.label)
//    }
//    val metricsTrain = new MulticlassMetrics(trainScores)
//    val metricsTest = new MulticlassMetrics(testScores)
//    metricsTrain.labels.foreach(label =>
//      println(s"Class $label precision = ${metricsTrain.precision(label)}")
//    )

//    println("RF Training AuROC: ",metricsTrain.confusionMatrix)
//    println("RF Test AuROC: ",metricsTest.precision)

    // Run training algorithm to build the model
    println("\nTraining LogisticRegression Model...")

    val model = new LogisticRegressionWithLBFGS()
      .setNumClasses(10)
      .run(train)

    // Compute raw scores on the test set.
    val trainAndLabels = train.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }

    // Compute raw scores on the test set.
    val predictionAndLabels = test.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }

    // Get evaluation metrics.
    val trainingMetrics = new MulticlassMetrics(trainAndLabels)
    println("Training Precision = " + trainingMetrics.precision)
    val predictionMetrics = new MulticlassMetrics(predictionAndLabels)
    println("Test Precision = " + predictionMetrics.precision)

//    // Save and load model
//    model.save(sc, "myModelPath")
//    val sameModel = LogisticRegressionModel.load(sc, "myModelPath")
//

    val datad = sql.read.format("libsvm").load("data/mllib/sample_libsvm_data.txt")

    // Automatically identify categorical features, and index them.
    // Set maxCategories so features with > 4 distinct values are treated as continuous.
    val featureIndexer = new VectorIndexer()
      .setInputCol("features")
      .setOutputCol("indexedFeatures")
      .setMaxCategories(4)
      .fit(datad)

    // Split the data into training and test sets (30% held out for testing)
    val Array(trainingData, testData) = datad.randomSplit(Array(0.7, 0.3))

    // Train a RandomForest model.
    val rf = new RandomForestRegressor()
      .setLabelCol("label")
      .setFeaturesCol("indexedFeatures")

    // Chain indexer and forest in a Pipeline
    val pipeline = new Pipeline()
      .setStages(Array(featureIndexer, rf))

    // Train model.  This also runs the indexer.
    val modell = pipeline.fit(trainingData)

    // Make predictions.
    val predictions = modell.transform(testData)

    // Select example rows to display.
    predictions.select("prediction", "label", "features").show()

    // Select (prediction, true label) and compute test error
    val evaluator = new RegressionEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("rmse")
    val rmse = evaluator.evaluate(predictions)
    println("Root Mean Squared Error (RMSE) on test data = " + rmse)

    val rfModel = modell.stages(1).asInstanceOf[RandomForestRegressionModel]
    println("Learned regression forest model:\n" + rfModel.toDebugString)

  }
}

