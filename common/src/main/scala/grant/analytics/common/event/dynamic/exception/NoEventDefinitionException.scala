package grant.analytics.common.event.dynamic.exception

/**
  * Created by grant on 20/04/16.
  */
class NoEventDefinitionException(val event_type:String) extends Exception(s"Event type '${event_type}' is not supported!")
