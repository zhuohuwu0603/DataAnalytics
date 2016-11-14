package grant.analytics.common.event.dynamic.exception

import com.google.common.base.Joiner

import scala.collection.convert.WrapAsJava

/**
  * Created by grant on 20/04/16.
  */
class FailedToExtractEventFieldValueException(val event_type:String, val field_name:String, val path:List[String])
  extends Exception(s"Parsing event field '${field_name}' in event type '${event_type}' is wrong, path is ${Joiner.on('/').join(WrapAsJava.asJavaIterable(path))}")
