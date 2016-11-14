package grant.analytics.common.conf.impl

import java.util.Properties

import grant.analytics.common.conf.AnalyticsConfiguration
import grant.analytics.common.event.AnalyticsEventParser

/**
 * Created by grant on 2016-11-13.
 */
abstract class AbstractAnalyticsConfiguration extends AnalyticsConfiguration{
  override def getSparkConfigurations():Option[Properties] = ???
  override def getKafkaConfigurations():Option[Properties] = ???
  override def getEventParserByEventType(event_type:String): AnalyticsEventParser = ???
}
