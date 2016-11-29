package grant.analytics.common.event.alternative

import grant.analytics.common.event.AnalyticsEventParser

import scala.collection.mutable

/**
  * Created by grant on 2016-11-28.
  */
object AnalyticsEventParserContainer {

  lazy val cache = createCache()

  private def createCache():mutable.HashMap[String, AnalyticsEventParserNonGeneral] = {
    new  mutable.HashMap[String, AnalyticsEventParserNonGeneral]
  }

  def getParser(event_type:String): Option[AnalyticsEventParserNonGeneral] = {
    cache.get(event_type)
  }
}
