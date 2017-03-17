package grant.analytics.common.event.alternative

import org.json4s.JsonAST.JValue

/**
 * Created by grant on 2016-11-13.
 */
class AnalyticsEngageEventParser extends AnalyticsEventParserNonGeneral{
  override type EVENTTYPE = AnalyticsEngageEvent

  override def parse(json: JValue): AnalyticsEngageEvent = ???
}
