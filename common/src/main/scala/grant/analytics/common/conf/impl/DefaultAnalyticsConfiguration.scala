package grant.analytics.common.conf.impl

import grant.analytics.common.conf._
import grant.analytics.common.conf.parser.AnalyticsConfigurationParser
import grant.analytics.common.event.AnalyticsEventParser

/**
  * Created by grant on 2017-03-02.
  */
class DefaultAnalyticsConfiguration(parser: AnalyticsConfigurationParser) extends AnalyticsConfiguration{
  override def getSparkConfigurations(): SparkConfigurations = {
    parser.getSparkConfigurations()
  }

  override def getElasticsearchConfigurations(): ElasticsearchConfigurations = {
    parser.getElasticsearchConfigurations()
  }

  override def getZookeeperConfigurations(): ZookeeperConfigurations = ???

  override def getCassandraConfigurations(): CassandraConfigurations = ???

  override def getMysqlConfigurations(): MysqlConfigurations = ???

  override def getEventParserByEventType(event_type: String): AnalyticsEventParser = ???

}
