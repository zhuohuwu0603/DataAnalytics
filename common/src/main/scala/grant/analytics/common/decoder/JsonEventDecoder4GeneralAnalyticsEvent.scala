package grant.analytics.common.decoder

import grant.analytics.common.event.dynamic.common.event.dynamic.{GeneralAnalyticsEvent, GeneralAnalyticsEventParser}
import kafka.utils.VerifiableProperties
import org.json4s.jackson.JsonMethods

/**
  * Created by grant on 2016-11-28.
  */
class JsonEventDecoder4GeneralAnalyticsEvent (props: VerifiableProperties = null) extends scala.AnyRef with kafka.serializer.Decoder[GeneralAnalyticsEvent]{

  private val parser = new GeneralAnalyticsEventParser(null)
  override def fromBytes(bytes: Array[Byte]): GeneralAnalyticsEvent = {

    parser.parse(
      JsonMethods.parse(new String(bytes))
    )
  }
}
