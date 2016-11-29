package grant.analytics.common.decoder

import grant.analytics.common.event.alternative.{AnalyticsEvent, AnalyticsEventParserContainer}
import kafka.utils.VerifiableProperties
import org.json4s.DefaultFormats
import org.json4s.JsonAST.{JNothing, JNull}
import org.json4s.jackson.JsonMethods

/**
  * Created by grant on 2016-11-28.
  */
class JsonEventDecoder4AnalyticsEvent (props: VerifiableProperties = null) extends scala.AnyRef with kafka.serializer.Decoder[AnalyticsEvent]{
  override def fromBytes(bytes: Array[Byte]): AnalyticsEvent = {
    val json = JsonMethods.parse(new String(bytes))

    (json \ "event_type") match {
      case JNothing | JNull => throw new Exception("no event_type field in the event")
      case value => {
        implicit val formats = DefaultFormats
        val event_type = value.extract[String]
        AnalyticsEventParserContainer.getParser(event_type) match {
          case Some(parser) => parser.parse(json)
          case None => throw new Exception(s"event_type ${event_type} is not supported! No parser available...")
        }
      }//end of case value
    }//end of json \ "event_type
  }
}
