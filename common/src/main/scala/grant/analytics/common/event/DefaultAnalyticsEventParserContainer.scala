package grant.analytics.common.event

import java.util.UUID

import grant.analytics.common.event.alternative.{AnalyticsEngageEventParser, AnalyticsViewEventParser, GeneralContentEvent}
import org.json4s.JsonAST.{JNothing, JValue}

import scala.collection.immutable.HashMap

/**
  * Created by grant on 2017-01-03.
  */
class DefaultAnalyticsEventParserContainer extends AnalyticsEventParserContainer with Serializable{

  private lazy val cache = createCache()

  private def createCache():HashMap[String, AnalyticsEventParser] = {
    HashMap(
      "analytics.view" -> new AnalyticsViewEventParser,
      "analytics.engage" -> new AnalyticsEngageEventParser
    )
  }

  override def getParser(event_type: String): AnalyticsEventParser = {
    cache.get(event_type) match {
      case Some(parser) => parser
      case None => new GeneralContentEventParser
    }
  }

  override def getParser(json: JValue): AnalyticsEventParser = {
    getParser(
      (json \ "event_type") match {
        case JNothing => throw new Exception("No event type attribute!")
        case value => value.values.toString
      }
    )
  }

}

class GeneralContentEventParser extends AnalyticsEventParser{
  override type EVENTTYPE = GeneralContentEvent

  override def parse(event: JValue): GeneralContentEvent = ???

}