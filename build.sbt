name := "twitterSparkApp"

version := "0.0.1"

scalaVersion := "2.10.5"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.6.1"

libraryDependencies ++= Seq(
"org.apache.spark" % "spark-streaming_2.10" % "1.6.1" % "provided",
"org.apache.spark" % "spark-streaming-twitter_2.10" % "1.6.1",
"org.apache.spark" % "spark-mllib_2.10" % "1.3.0",
"com.google.code.gson" % "gson" % "1.7.1"


)

//resolvers += "mvnrepository" at "http://mvnrepository.com/artifact/"

// Configure JAR used with the assembly plug-in
jarName in assembly := "twitterSparkApp.jar"

assemblyMergeStrategy in assembly := {
    case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
    case m if m.startsWith("META-INF") => MergeStrategy.discard
    case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
    case PathList("org", "apache", xs @ _*) => MergeStrategy.last
    case PathList("org", "jboss", xs @ _*) => MergeStrategy.last
    case "about.html"  => MergeStrategy.rename
    case "reference.conf" => MergeStrategy.concat
    case _ => MergeStrategy.last
}


// A special option to exclude Scala itself from our assembly JAR, since Spark
// already bundles Scala.
//assemblyOption in assembly :=
//(assemblyOption in assembly).value.copy(includeScala = false)
