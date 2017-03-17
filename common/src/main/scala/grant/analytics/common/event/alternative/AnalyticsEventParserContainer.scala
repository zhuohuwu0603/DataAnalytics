package grant.analytics.common.event.alternative

import org.json4s.JsonAST.JValue

/**
  * Created by grant on 2017-01-03.
  */
trait AnalyticsEventParserContainer {
  def getParser(event_type:String): AnalyticsEventParserNonGeneral
  def getParser(json:JValue): AnalyticsEventParserNonGeneral
}
