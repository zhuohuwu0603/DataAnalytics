package grant.analytics.common.event.dynamic.common.event.dynamic

import java.net.URL
import java.util.UUID

import com.typesafe.config.{Config, ConfigFactory}
import grant.analytics.common.compiler.AnalyticsDynamicScriptCompiler
import grant.analytics.common.event.AnalyticsEventParser
import grant.analytics.common.event.dynamic.exception.{NoEventTypeDefinedException, FailedToExtractEventFieldValueFromScriptException, FailedToExtractEventFieldValueException, NoEventDefinitionException}
import grant.analytics.common.event.dynamic.field.{DefaultEventFieldValueExtractor, EventFieldValueExtractorClassTemplates}
import org.json4s.JsonAST.{JNothing, JNull, JValue}

import scala.collection.convert.WrapAsScala
import scala.io.Source

/**
  * Created by grant on 2016-04-19.
  */
class GeneralAnalyticsEventParser(val section_trees_handler: (UUID => List[UUID]), private val definition_url:URL = new URL("classpath:viafoura/analytics/events/events.conf") ) extends AnalyticsEventParser with Serializable{

  type EVENTTYPE = GeneralAnalyticsEvent
  /*
      event_definitions: key is event_type
   */
  private lazy val event_definitions: Map[String, EventDefinition] = load()


  def load():Map[String, EventDefinition] = {
    val config = ConfigFactory.parseURL(definition_url)

    val compiler = new AnalyticsDynamicScriptCompiler

    val scripts = WrapAsScala.asScalaIterator(config.getConfigList("grant.dataanalytics.events.scripts").iterator()).map(script => {
      val name = script.getString("name")
      val code = Source.fromURL(script.getString("script")).mkString
      val package_name = "grant.dataanalytics.event.field.extractor"
      val class_body = EventFieldValueExtractorClassTemplates.generateEventFieldValueExtractorClassBody(package_name, name, code)
      val handler_in_script = try {
        this.getClass.getClassLoader.loadClass( script.getString("handler_in_script") ).getConstructor().newInstance().asInstanceOf[(String=>Some[String])]
      }
      catch {
        case _:Exception => {
          null
        }
      }//end cactch
      val extractor = compiler.compile(s"${package_name}.${name}", class_body).getConstructor(classOf[(String=>Some[String])]).newInstance(handler_in_script).asInstanceOf[(JValue, List[String])=>Option[String]]
      (name, extractor)
    }).toMap

    val commons = WrapAsScala.asScalaBuffer(config.getConfigList("grant.dataanalytics.events.commons")).groupBy(item => {
      try{
        if(item.getBoolean("default_in_all")) "in" else "out"
      }
      catch {
        case _:Exception => "out"
      }
    })

    def getStringOutOfConfig(cc:Config, name:String): Option[String] = {
      try{
        Some(cc.getString(name))
      }
      catch {
        case _:Exception => None
      }
    }

    val not_default_in_all = commons.get("out").get.map(item => {
      (item.getString("name"), item)
    }).toMap

    val default_field_value_extractor = new DefaultEventFieldValueExtractor

    WrapAsScala.asScalaBuffer( config.getConfigList("grant.dataanalytics.events.types") ).map(event => {
      val event_type = event.getString("type")
      val fields = (WrapAsScala.asScalaBuffer(event.getConfigList("fields")) ++ commons.get("in").get ).map(field => {
        val name = field.getString("name")

        val real_field = getStringOutOfConfig(field, "derived") match {
          case Some(field_in_commons) => {
            // event field is derived from "commons", which means the format is defined in "commons"
            not_default_in_all.get(field_in_commons).get
          }
          case None => {
            // current field defines the format
            field
          }
        }

        val (extractor, lstPath) = getStringOutOfConfig(real_field, "path") match {
          case Some(path) => {
            (default_field_value_extractor, path.split("/").drop(1).toList)
          }
          case None => {
            (scripts.get(real_field.getString("script")).get, List[String]())
          }
        }

        real_field.getString("type") match {
          case "INT" => {
            (name, new EventFieldDefinition(event_type, name, lstPath, EventFieldType.INT, extractor))
          }
          case "LONG" => {
            (name, new EventFieldDefinition(event_type, name, lstPath, EventFieldType.LONG, extractor))
          }
          case "STRING" => {
            (name, new EventFieldDefinition(event_type, name, lstPath, EventFieldType.STRING, extractor))
          }
          case "UUID" => {
            (name, new EventFieldDefinition(event_type, name, lstPath, EventFieldType.UUID_, extractor))
          }
          case "BOOLEAN" => {
            (name, new EventFieldDefinition(event_type, name, lstPath, EventFieldType.BOOLEAN, extractor))
          }
          case unsupported:String => throw new Exception(s"event field type ${unsupported} is not supported")
        }
      }).toMap
      (event_type, new EventDefinition(event_type, fields))
    }).toMap
  }


  def getEventDefinition(event_type:String):EventDefinition = {
    event_definitions.get(event_type) match {
      case Some(value) => value
      case None => throw new NoEventDefinitionException(event_type)
    }
  }

  def getEventFieldDefinition(event_type:String, field_name:String):EventFieldDefinition = {
    val event_definition = getEventDefinition(event_type)
    event_definition.getEventFieldDefinition(field_name)
  }

  def parseEventFieldValue(event:JValue, event_type:String, field_name:String):String = {
    val field_definition = getEventFieldDefinition(event_type, field_name)
    field_definition.getFieldValue(event) match {
      case Some(value) => value
      case None => {
        if(field_definition.path_in_event.isEmpty){
          throw new FailedToExtractEventFieldValueException(event_type, field_name, field_definition.path_in_event)
        }
        else {
          throw new FailedToExtractEventFieldValueFromScriptException(event_type, field_name)
        }
      }//end of None
    }//end match
  }

  def parse(event:JValue): GeneralAnalyticsEvent = {
    val event_type = event \ "event_type" match {
      case JNull | JNothing => throw new NoEventTypeDefinedException(event)
      case value => value.values.toString
    }

    getEventDefinition(event_type).parseEvent(event, section_trees_handler)

  }

  def getEventFieldType(event_type:String, field_name:String): EventFieldType.Val[_] ={
    event_definitions.get(event_type) match {
      case Some(event_definition) => {
        val field_definition = event_definition.getEventFieldDefinition(field_name)
        field_definition.field_type
      }
      case None => throw new NoEventDefinitionException(event_type)
    }
  }
}
