package grant.analytics.common.event.dynamic.exception

/**
  * Created by grant on 2016-04-22.
  */
class FailedToExtractEventFieldValueFromScriptException(val event_type:String, val field_name:String)
  extends Exception(s"Parsing event field '${field_name}' in event type '${event_type}' is wrong, please check your parsing script")