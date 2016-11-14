package grant.analytics.common.event.dynamic.common.event.dynamic

import java.util.UUID

import com.google.common.base.Joiner
import grant.analytics.common.event.dynamic.exception.NoEventFieldDefinitionException
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods._

import scala.collection.convert.WrapAsJava

/**
  * Created by grant on 2016-04-19.
  */
class EventDefinition(val event_type:String,
                      val fields: Map[String, EventFieldDefinition]) {
  def getEventFieldDefinition(field_name:String): EventFieldDefinition = {
    fields.get(field_name) match {
      case Some(value) => value
      case None => throw new NoEventFieldDefinitionException(event_type, field_name)
    }
  }

  def parseEvent(event:JValue, section_trees_handler: (UUID => List[UUID])):GeneralAnalyticsEvent = {
    val values = fields.map(pair => {
      val field_name = pair._1
      val field_definition = pair._2
      val field_value = field_definition.getFieldValue(event) match {
        case Some(value) => value
        case None => {
          field_definition.field_type match {
            case EventFieldType.INT | EventFieldType.LONG => {
              "0"
            }
            case EventFieldType.STRING => {
              "unknown"
            }
            case EventFieldType.UUID_ => {
              "00000000-0000-0000-0000-000000000000"
            }
            case EventFieldType.BOOLEAN => {
              "false"
            }
            case _ => throw new Exception(s"event field type ${field_definition.field_type} is not supported")
          }
//          if(field_definition.path_in_event.isEmpty){
//            throw new FailedToExtractEventFieldValueException(event_type, field_name, field_definition.path_in_event)
//          }
//          else {
//            throw new FailedToExtractEventFieldValueFromScriptException(event_type, field_name)
//          }
        }
      }
      (field_name, field_value)
    })

    val ancestors = if(section_trees_handler != null) {
      values.get("section") match {
        case Some(value) => {
          Joiner.on(' ').join(WrapAsJava.asJavaIterable(section_trees_handler(UUID.fromString(value)).map(_.toString))
          )//end of AnalyticsEventEx
        }//end of Some(value)
        case None => throw new Exception(s"No section uuid data in the event: ${compact(render(event))}")
      }
    }
    else {
      ""
    }

    val event_uuid = values.get("event_uuid") match {
      case Some(value) => UUID.fromString(value)
      case None => {
//        AnalyticsEventHashing.hash(event)
//        AnalyticsEventHashing.hash(event_type, values)
      }
    }

    GeneralAnalyticsEvent(event_type,
      values ++ Map("ancestors" -> ancestors, "event_uuid"-> event_uuid.toString, "event_type" -> event_type)
    )
  }
}
