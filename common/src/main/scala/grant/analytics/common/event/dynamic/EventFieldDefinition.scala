package grant.analytics.common.event.dynamic.common.event.dynamic

import org.json4s.JsonAST.JValue

/**
  * Created by grant on 2016-04-19.
  */
class EventFieldDefinition(val event_type:String, val field_name:String, val path_in_event:List[String], val field_type: EventFieldType.Val[_], val extractor:(JValue, List[String])=>Option[String]) {
  def getFieldValue(event:JValue):Option[String] = {
    extractor(event, path_in_event)
  }
}
