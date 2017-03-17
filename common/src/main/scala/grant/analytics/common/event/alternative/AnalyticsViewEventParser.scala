package grant.analytics.common.event.alternative

import org.json4s.JsonAST.JValue

/**
 * Created by grant on 2016-11-13.
 */
class AnalyticsViewEventParser extends AnalyticsEventParserNonGeneral{
  override type EVENTTYPE = AnalyticsViewEvent

  override def parse(json: JValue): AnalyticsViewEvent = ???
}
