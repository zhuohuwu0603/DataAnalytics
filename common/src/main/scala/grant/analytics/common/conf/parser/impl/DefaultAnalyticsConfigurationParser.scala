package grant.analytics.common.conf.parser.impl

import com.typesafe.config.Config
import grant.analytics.common.conf._
import grant.analytics.common.conf.loader.TypesafeConfigurationLoader
import grant.analytics.common.conf.parser.AnalyticsConfigurationParser

import scala.collection.convert.WrapAsScala

/**
  * Created by grant on 2017-03-02.
  */
class DefaultAnalyticsConfigurationParser(loader: TypesafeConfigurationLoader) extends AnalyticsConfigurationParser{
  override def loadConfig(): Config = {
    loader.load()
  }

  override def getSparkConfigurations(): SparkConfigurations = {
    val kafka = KafkaConfigurations(
      config.getString("grant.analytics.kafka.brokers"),
      WrapAsScala.asScalaBuffer( config.getStringList("grant.analytics.kafka.topics") ).mkString(","),
      config.getString("grant.analytics.kafka.auto.offset.reset"),
      config.getBoolean("grant.analytics.kafka.auto.commit.enable")
    )

    val streaming = SparkStreamingConfigurations(
      config.getInt("grant.analytics.spark.streaming.interval"),
      kafka
    )

    SparkConfigurations(
      config.getString("grant.analytics.spark.app.name"),
      streaming
    )
  }

  override def getElasticsearchConfigurations(): ElasticsearchConfigurations = ???

  override def getZookeeperConfigurations(): ZookeeperConfigurations = ???

  override def getCassandraConfigurations(): CassandraConfigurations = ???

  override def getMysqlConfigurations(): MysqlConfigurations = ???
}
