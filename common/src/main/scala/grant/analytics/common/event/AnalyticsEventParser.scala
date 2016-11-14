package grant.analytics.common.event

import grant.analytics.common.event.alternative.AnalyticsEvent
import org.json4s.JsonAST.JValue

/**
 * Created by grant on 2016-11-12.
 */
trait AnalyticsEventParser {
  type EVENTTYPE
  def parse(json: JValue): EVENTTYPE
}
