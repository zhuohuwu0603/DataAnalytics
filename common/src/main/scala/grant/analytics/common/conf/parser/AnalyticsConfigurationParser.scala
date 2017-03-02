package grant.analytics.common.conf.parser

import com.typesafe.config.Config
import grant.analytics.common.conf._

/**
  * Created by grant on 2017-03-02.
  */
trait AnalyticsConfigurationParser extends ConfigurationParser{
  override type IN_MEMORY_TYPE = Config

  def getSparkConfigurations():SparkConfigurations
  def getElasticsearchConfigurations():ElasticsearchConfigurations
  def getZookeeperConfigurations():ZookeeperConfigurations
  def getCassandraConfigurations():CassandraConfigurations
  def getMysqlConfigurations():MysqlConfigurations

}
