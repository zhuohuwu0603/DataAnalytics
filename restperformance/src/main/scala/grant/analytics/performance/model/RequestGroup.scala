package grant.analytics.performance.model

/**
  * Created by grant on 2017-01-20.
  */
case class RequestGroup(val requests: List[Request])

case class Request(val path:String,
                   val args: Map[String, String],
                   val method:String)
