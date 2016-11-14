package grant.analytics.common.event.dynamic.field

import org.json4s.JsonAST.{JNothing, JNull, JValue}

/**
  * Created by grant on 2016-04-22.
  */
class DefaultEventFieldValueExtractor extends ((JValue, List[String]) => Option[String]){
  override def apply(event: JValue, path: List[String]): Option[String] = {
    require(path != null)
    getFieldValueRecursively(event, path)
  }

  private def getFieldValueRecursively(target:JValue, currentPath:List[String]):Option[String] = {
    currentPath match {
      case current::rest => {
        target \ current match {
          case JNothing | JNull => None
          case value => getFieldValueRecursively(value, rest)
        }
      }
      case List() => Some(target.values.toString)
    }
  }
}
