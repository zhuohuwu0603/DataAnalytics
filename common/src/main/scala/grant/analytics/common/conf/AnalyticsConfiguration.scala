package grant.analytics.common.conf

import java.util.Properties
import grant.analytics.common.conf.parser.impl.DefaultConfigurationParser
import grant.analytics.common.event.AnalyticsEventParser


/**
 * Created by grant on 2016-11-12.
 */
trait AnalyticsConfiguration extends Configuration{
  def getSparkConfigurations():Option[Properties]
  def getKafkaConfigurations():Option[Properties]
  def getEventParserByEventType(event_type:String): AnalyticsEventParser
}
