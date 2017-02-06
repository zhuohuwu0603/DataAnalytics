package grant.analytics.performance.model

/**
  * Created by grant on 2017-01-17.
  */
case class RequestGroup(val requests: List[Request])

case class Request(val host:String,
                   val port:Int,
                   val protocol:String,
                   val content_encoding:String,
                    val path:String,
              val args: Map[String, String],
                   val method:String)