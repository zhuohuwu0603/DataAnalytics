package grant.analytics.common.event.dynamic.common.event.dynamic

import java.util.UUID

/**
  * Created by grant on 2016-04-19.
  */
object EventFieldType extends Enumeration{
  case class Val[T](val transformer:(String => T)) extends super.Val {
    def transform(value:String): T = {
      transformer(value)
    }
  }

  val INT = Val[Int]((value:String) => {
    value.toInt
  })

  val LONG = Val[Long]((value:String) => {
    value.toLong
  })

  val UUID_ = Val[UUID]((value:String) => {
    UUID.fromString(value)
  })

  val STRING = Val[String]((value:String) => {
    value
  })

  val BOOLEAN = Val[Boolean]((value:String)=> {
    value.toBoolean
  })

}
