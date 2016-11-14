package grant.analytics.common.event.dynamic.exception

import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods._

/**
  * Created by grant on 20/04/16.
  */
class NoEventTypeDefinedException(event:JValue)
  extends Exception(s"No event type field is defined in the event ${compact(render(event))}")
