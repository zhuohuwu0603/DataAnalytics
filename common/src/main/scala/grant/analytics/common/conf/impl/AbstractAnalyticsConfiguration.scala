package grant.analytics.common.conf.impl

import java.net.URL
import java.util.Properties

import com.typesafe.config.Config
import grant.analytics.common.conf.AnalyticsConfiguration
import grant.analytics.common.conf.parser.impl.DefaultConfigurationParser
import grant.analytics.common.conf.parser.impl.DefaultConfigurationParser
import grant.analytics.common.event.AnalyticsEventParser

/**
 * Created by grant on 2016-11-13.
 */
abstract class AbstractAnalyticsConfiguration(url: URL) extends AnalyticsConfiguration{

  type PARSER = DefaultConfigurationParser
  lazy val conf = load()

  override def getSparkConfigurations():Option[Properties] = ???
  override def getKafkaConfigurations():Option[Properties] = ???
  override def getEventParserByEventType(event_type:String): AnalyticsEventParser = ???

  override def getParser(): DefaultConfigurationParser = {
    new DefaultConfigurationParser
  }
  private def load(): Config = ???

}
