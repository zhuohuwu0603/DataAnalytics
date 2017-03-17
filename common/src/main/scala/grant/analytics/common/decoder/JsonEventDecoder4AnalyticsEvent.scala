package grant.analytics.common.decoder

import grant.analytics.common.event.alternative.{AnalyticsEvent, DefaultAnalyticsEventParserContainer}
import kafka.utils.VerifiableProperties
import org.json4s.DefaultFormats
import org.json4s.JsonAST.{JNothing, JNull}
import org.json4s.jackson.JsonMethods

/**
  * Created by grant on 2016-11-28.
  */
class JsonEventDecoder4AnalyticsEvent (props: VerifiableProperties = null) extends scala.AnyRef with kafka.serializer.Decoder[AnalyticsEvent]{

  private lazy val event_parser_container = new DefaultAnalyticsEventParserContainer

  override def fromBytes(bytes: Array[Byte]): AnalyticsEvent = {
    val json = JsonMethods.parse(new String(bytes))

    (json \ "event_type") match {
      case JNothing | JNull => throw new Exception("no event_type field in the event")
      case value => {
        implicit val formats = DefaultFormats
        val event_type = value.extract[String]
        event_parser_container.getParser(event_type).parse(json)
      }//end of case value
    }//end of json \ "event_type
  }
}
