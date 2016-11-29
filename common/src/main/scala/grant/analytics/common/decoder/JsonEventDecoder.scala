package grant.analytics.common.decoder

import kafka.utils.VerifiableProperties
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods

/**
 * Created by grant on 2016-11-11.
 */
class JsonEventDecoder (props: VerifiableProperties = null) extends scala.AnyRef with kafka.serializer.Decoder[JValue] {
  override def fromBytes(bytes: Array[Byte]): JValue = {
    JsonMethods.parse(new String(bytes))
  }
}
