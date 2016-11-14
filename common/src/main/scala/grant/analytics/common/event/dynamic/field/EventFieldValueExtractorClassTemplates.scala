package grant.analytics.common.event.dynamic.field

/**
  * Created by grant on 2016-04-22.
  */
object EventFieldValueExtractorClassTemplates {

  def generateEventFieldValueExtractorClassBody(package_name:String, classname:String, code:String): String = {
    s"""
       | package ${package_name}

       | import org.json4s.JsonAST.JValue
       | import org.json4s.JsonAST.{JNothing, JNull}
       |
       | class ${classname}(val handler:(String=>Some[String])) extends ((JValue, List[String]) => Option[String]) with Serializable{
       |     override def apply(event:JValue, path: List[String]): Option[String] = {
       |         ${code}
       |     }
       | }
     """.stripMargin
  }

}
