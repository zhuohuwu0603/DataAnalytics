package grant.analytics.common.event.alternative

import grant.analytics.common.event.AnalyticsEventParser
import org.json4s.JsonAST.JValue

/**
 * Created by grant on 2016-11-13.
 */
class AnalyticsViewEventParser extends AnalyticsEventParser{
  override type EVENTTYPE = AnalyticsViewEvent

  override def parse(json: JValue): AnalyticsEvent = ???
}
