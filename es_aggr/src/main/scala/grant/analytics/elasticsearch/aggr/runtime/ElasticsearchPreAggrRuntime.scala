package grant.analytics.elasticsearch.aggr.runtime

import java.net.URL
import java.util.Properties

import grant.analytics.common.sectiontrees.mysql.SectionTreesFromMySQL
import grant.analytics.elasticsearch.aggr.conf.PreAggrConfiguration
import grant.analytics.elasticsearch.aggr.conf.impl.DefaultPreAggrConfigurationImpl
import grant.analytics.elasticsearch.cli.CLIParas
import grant.analytics.elasticsearch.cli.impl.DefaultCLIParasImpl
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by grant on 2016-12-08.
  */
object ElasticsearchPreAggrRuntime {

  var command_line:Array[String] = Array.empty[String]

  lazy val cli_paras = parseCommandLine()
  lazy val config = loadConfiguration()
  lazy val section_tree = createSectionTree()
  lazy val session = createSparkSession()
  private lazy val mysqlOptions = createMysqlOptions()
  private lazy val cassandraOptions = createCassandraOptions()

  private def parseCommandLine(): CLIParas = {
    new DefaultCLIParasImpl(command_line)
  }

  private def loadConfiguration():PreAggrConfiguration = {

    val path = System.getProperty("viafoura.analytics.preaggr.configuration.url")
    if(path != null){
      new DefaultPreAggrConfigurationImpl(new URL(path), cli_paras)
    }
    else{
      new DefaultPreAggrConfigurationImpl(new URL("classpath:viafoura/analytics/elasticsearch/aggregation.conf"), cli_paras)
    }

  }

  private def createSparkSession():SparkSession = {
    SparkSession.builder().appName("").master("local[*]").config("spark.serializer", "org.apache.spark.serializer.KryoSerializer").getOrCreate()
  }

  private def createSectionTree(): SectionTreesFromMySQL = {
    new SectionTreesFromMySQL(mysqlOptions, session.sqlContext)
//    new SectionTreesFromCassandra(cassandraOptions)
  }

  private def createSparkContext(): SparkContext = {
    val conf = new SparkConf()
    conf.set("es.nodes", config.getESNodes())
    conf.set("es.port", "9200")
    conf.setAppName("Pre-Aggregation")
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    if(cli_paras.isLocalMode())
      conf.setMaster("local[*]")
    new SparkContext(conf)
  }

  private def createMysqlOptions(): Map[String, String] = {

    config.getMySQLProperties()
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
