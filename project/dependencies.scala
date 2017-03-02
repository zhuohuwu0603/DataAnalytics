
import sbt._

object Dependencies {
  val scala_main_version = "2.11"
  val spark_version = "2.1.0"
  val kafka_version = "0-8"

  val scala_compiler = "org.scala-lang" % "scala-compiler" % "2.11.8"

  val spark_core = ("org.apache.spark" % s"spark-core_${scala_main_version}" % spark_version)
    .exclude("org.mortbay.jetty", "servlet-api")
    .exclude("commons-beanutils", "commons-beanutils-core")
    .exclude("commons-collections", "commons-collections")
    .exclude("commons-logging", "commons-logging")
    .exclude("com.esotericsoftware.minlog", "minlog")

  val spark_streaming = "org.apache.spark" % s"spark-streaming_${scala_main_version}" % spark_version

  val spark_streaming_kafka =   ("org.apache.spark" % s"spark-streaming-kafka-${kafka_version}_${scala_main_version}" % spark_version)
    .exclude("org.spark-project.spark", "unused")

  val spark_sql = ("org.apache.spark" % s"spark-sql_${scala_main_version}" % spark_version)

  val spark_cassandra_connector = ("com.datastax.spark" % s"spark-cassandra-connector_${scala_main_version}" % "2.0.0-M3")
    .exclude("com.codahale.metrics", "metrics-core")
    .exclude("io.netty", "netty-handler")
    .exclude("io.netty", "netty-buffer")
    .exclude("io.netty", "netty-common")
    .exclude("io.netty", "netty-transport")
    .exclude("io.netty", "netty-codec")

  val typesafe_config_lib = "com.typesafe" % "config" % "1.3.1"

  val google_guice = ("com.google.inject" % "guice" % "4.0")
    .exclude("com.google.guava", "guava")
    .exclude("stax", "stax-api")

  val aspectj = "org.aspectj" % "aspectjrt" % "1.8.9"
  val hadoop_aws = ("org.apache.hadoop" % "hadoop-aws" % "2.7.3")
    .exclude("org.apache.hadoop", "hadoop-common")
  val scalatest = "org.scalatest" % "scalatest_2.11" % "2.2.6"
  val junit = "junit" % "junit" % "4.12"
  val elasticsearch_spark = "org.elasticsearch" % "elasticsearch-spark-20_2.11" % "5.2.2"
  val elasticsearch =  "org.elasticsearch" % "elasticsearch" % "5.2.1"

  val cassandra = ("org.apache.cassandra" % "cassandra-all" % "3.5").exclude("org.slf4j", "log4j-over-slf4j").exclude("org.slf4j", "slf4j-api")
  val kafka = ("org.apache.kafka" % "kafka_2.11" % "0.8.2.1")

  val mysql_connector_java = "mysql" % "mysql-connector-java" % "5.1.17"
  val mysql_connector_mxj =  "mysql" % "mysql-connector-mxj" % "5.0.12"
  val mysql_connector_mxj_db_file = "mysql" % "mysql-connector-mxj-db-files" % "5.0.12"
  val twitter_algebird = "com.twitter" % "algebird-core_2.11" % "0.11.0"

  val jmeter_http = "org.apache.jmeter" % "ApacheJMeter_http" % "3.1"
  val typesafe_config = "com.typesafe" % "config" % "1.3.1"



  val default_dependencies_seq = Seq(
    (spark_core).exclude("net.java.dev.jets3t", "jets3t") % Provided,
    spark_cassandra_connector,
    spark_streaming % Provided ,
    spark_streaming_kafka,
    spark_sql,
    typesafe_config_lib,
    google_guice,
    aspectj,
    twitter_algebird,
    hadoop_aws,
    scalatest % Test,
    junit  % Test
  )

}