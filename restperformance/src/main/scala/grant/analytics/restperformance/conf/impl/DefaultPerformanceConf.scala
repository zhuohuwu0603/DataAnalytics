package grant.analytics.performance.conf.impl

import java.net.URL

import com.typesafe.config.{Config, ConfigFactory}
import grant.analytics.performance.conf.PerformanceConf
import grant.analytics.performance.model.{Request, RequestGroup}
import grant.analytics.performance.model.engine.impl.jmeter.JMeterTestEngine

import scala.collection.convert.WrapAsScala
import scala.util.{Failure, Success, Try}

/**
  * Created by grant on 2017-01-17.
  */
class DefaultPerformanceConf(url:URL) extends PerformanceConf{

  override type ENGINE = JMeterTestEngine

  private lazy val config = parseConfig()
  private lazy val globals = parseGlobals()
  private lazy val requestGroups = parseRequestGroups()
  private lazy val engine = parseEngine()

  private def parseConfig(): Config = {
    ConfigFactory.parseURL(url)
  }

  private def parseGlobals(): Map[String, String] = {
    WrapAsScala.asScalaIterator( config.getConfig("viafoura.analytics.performance.global").entrySet().iterator() ).map(item => {
      (item.getKey, item.getValue.unwrapped().toString)
    }).toMap
  }

  private def parseRequestGroups(): List[RequestGroup] = {

    WrapAsScala.asScalaBuffer( config.getConfigList("viafoura.analytics.performance.requests") ).map(configlet => {

      val arg_values =
        WrapAsScala.asScalaIterator( configlet.getConfig("args").entrySet().iterator() ).map(item => {
          (item.getKey, item.getValue.unwrapped().toString)
        }).toMap

      val method = Try(configlet.getString("method")) match {
        case Success(m) => m
        case Failure(ex) => globals.get("method").get
      }

      val host = globals.get("host") match {
        case Some(value) => value
        case None => "localhost"
      }

      val port = globals.get("port") match {
        case Some(value) => value.toInt
        case None => 80
      }

      val protocol = globals.get("protocol") match {
        case Some(value) => value
        case None => "http"
      }

      val content_encoding = globals.get("content_encoding") match {
        case Some(value) => value
        case None => "application/json"
      }

      RequestGroup(
        WrapAsScala.asScalaBuffer( configlet.getStringList("patterns") ).map(pattern => {
          val path_args = pattern.split('?')
          val path_with_variables = path_args(0)
          val path =
            if((path_with_variables.contains("section") && path_with_variables.contains("page")) || (path_with_variables.contains("section")))
              path_with_variables.replace("${uuid}", globals.get("section_uuid").get)
            else
              path_with_variables.replace("${uuid}", globals.get("page_uuid").get)
          val args = path_args(1).split('&').map(str => {
            val key = str.split('=')(0)
            (key, arg_values.get(key).get)
          }).toMap
          Request(host, port, protocol, content_encoding, path, args, method)
        }).toList
      )

    }).toList
  }

  private def parseEngine(): JMeterTestEngine = {
    val clazz = config.getString("viafoura.analytics.performance.engine.class")
    Class.forName(clazz)
      .getConstructor(classOf[Map[String, String]], classOf[String])
      .newInstance(
        WrapAsScala.asScalaIterator( config.getConfig("viafoura.analytics.performance.engine").entrySet().iterator() ).map(item => {
          (item.getKey, item.getValue.unwrapped().toString)
        }).toMap,
        globals.get("session") match {
          case Some(session) => session
          case None => null
        }

      )
      .asInstanceOf[JMeterTestEngine]
  }

  override def getGlobals(): Map[String, String] = globals

  override def getRequests(): List[RequestGroup] = requestGroups

  override def getEngine(): JMeterTestEngine = engine
}
