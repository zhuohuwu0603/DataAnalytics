package grant.analytics.elasticsearch.agg

import java.util.Properties

import grant.analytics.common.sectiontrees.SectionTrees
import grant.analytics.common.sectiontrees.cassandra.SectionTreesFromCassandra
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext};

/**
  * Created by grant on 2016-12-08.
  */
object ElasticsearchPreAggrRuntime {

  lazy val session = createSparkSession()
  lazy val section_tree = createSectionTree()
  lazy val sc = getSparkContext()
  private lazy val mysqlOptions = createMysqlOptions()
  private lazy val cassandraOptions = createCassandraOptions()

  private def createSparkSession():SparkSession = {
    SparkSession.builder()
      .appName("Aggregation")
      .master("local[4]")
      .config("es.nodes", "localhost")
      .config("es.port", "9200").getOrCreate()
  }

  private def createSectionTree(): SectionTrees = {
    new SectionTreesFromCassandra(cassandraOptions)
  }

  private def getSparkContext(): SparkContext = {
    session.sparkContext
  }

  private def createMysqlOptions(): Map[String, String] = {
//    Map(
//      "url" -> "jdbc:mysql://10.3.100.17:3306/grant_testing",
//      "driver" -> "com.mysql.jdbc.Driver",
//      "user" -> "grant",
//      "password" -> "vf"
//    )

    Map(
      "url" -> "jdbc:mysql://127.0.0.1:3306/grant_testing",
      "driver" -> "com.mysql.jdbc.Driver",
      "user" -> "root",
      "password" -> "vf"
    )
  }

  private def createCassandraOptions(): Properties = {
    val p = new Properties()

    p.put("contactpoints", "seed.content.cassandra.dev.aws.viafoura.net")
    p.put("keyspace", "grant_analytics_beta1")
    p.put("tables.sectiontrees", "section_trees")
    p.put("connection.keepalive_ms", "10000")
    p
  }
}
