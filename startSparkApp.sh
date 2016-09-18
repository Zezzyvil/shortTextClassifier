#!/bin/bash

echo reset
echo " *[1] show trending # tags "
echo "  [2] Classifiers"
echo "  [3] Extract features and train model "
echo "  [4] Collect Tweets "
echo "  [5] Exit "
echo -n " make a number default is marked *  "

read result

case "$result" in
  2)
  echo
      ~/Desktop/project/App/spark-1.6.1-bin-hadoop2.6/bin/spark-submit --supervise --class "classifiers" ./target/scala-2.10/twitterSparkApp.jar
    ;;

  3)
     echo
    ~/Desktop/project/App/spark-1.6.1-bin-hadoop2.6/bin/spark-submit --supervise --class "featureExtraction" ./target/scala-2.10/twitterSparkApp.jar
    ;;

  4)
   echo
    ~/Desktop/project/App/spark-1.6.1-bin-hadoop2.6/bin/spark-submit --supervise --class "CollectTweets" ./target/scala-2.10/twitterSparkApp.jar
   ;;

   5)
   echo
   " Exiting ..."
   ;;

  *)
  echo
~/Desktop/project/App/spark-1.6.1-bin-hadoop2.6/bin/spark-submit --supervise --class "TwitterByTags" ./target/scala-2.10/twitterSparkApp.jar "tKpqCHvl1LTqkNz3BOCo1u3i", "Yplsimn09zgpJfr4Fkp53mpJAjGkiT7V1QLAnruPcyCuyu1hE4", "370274844-myp3uE998LsjsYNql5t8kQQXSN4cL3uvVKVmbCYF", "bLNdv2QEq03ncpvhHB8iCPiwlitatn9BkVwYJRvngt6y3"
    ;;

  esac
  #exit 1;

echo "exiting ..."
echo 
