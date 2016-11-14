package grant.analytics.common.event.dynamic.exception

/**
  * Created by grant on 20/04/16.
  */
class NoEventFieldDefinitionException(val event_type:String, val field_name:String)
  extends Exception(s"There is no '${field_name}' defined in event type '${event_type}'!")
