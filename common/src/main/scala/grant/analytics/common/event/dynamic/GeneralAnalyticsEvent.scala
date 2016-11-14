package grant.analytics.common.event.dynamic.common.event.dynamic

import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

/**
  * Created by grant on 2016-04-19.
  *
  * SectionTrees data could be a property in "fields" map, key is "ancestors", value is a list of UUID separated by ";"
  *
  */
case class GeneralAnalyticsEvent(val event_type:String, private val fields:Map[String, String]) {

  def apply(field_name:String): String = {

    fields.get(field_name) match {
      case Some(value) => value
      case None => throw new Exception(s"field name '${field_name}' is not supported in event type '${event_type}'!")
    }
  }

  def allFields: Map[String, String] = fields

  def toJson():String = {
    compact(render(fields))
  }

}
