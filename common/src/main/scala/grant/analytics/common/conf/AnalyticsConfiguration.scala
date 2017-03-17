package grant.analytics.common.conf

import grant.analytics.common.event.alternative.AnalyticsEventParserNonGeneral


/**
 * Created by grant on 2016-11-12.
 */
trait AnalyticsConfiguration extends Configuration{
  def getSparkConfigurations():SparkConfigurations
  def getElasticsearchConfigurations():ElasticsearchConfigurations
  def getZookeeperConfigurations():ZookeeperConfigurations
  def getCassandraConfigurations():CassandraConfigurations
  def getMysqlConfigurations():MysqlConfigurations
  def getEventParserByEventType(event_type:String): AnalyticsEventParserNonGeneral
}
