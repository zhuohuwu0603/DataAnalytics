package grant.analytics.env.embedded.sql.impl

import java.net.URL
import java.sql.{Connection, DriverManager}
import java.util.Properties

import grant.analytics.common.url.ClasspathURLEnabler
import grant.analytics.env.embedded.sql.EmbeddedSQL
import org.hsqldb.persist.HsqlProperties
import org.hsqldb.server.Server

import scala.io.Source

/**
  * Created by grant on 2017-03-21.
  *
  * default configuration items include:
  *
  * server_properties_url:
  *
  */
class DefaultEmbeddedSQL(conf:Map[String, String]) extends EmbeddedSQL with ClasspathURLEnabler{
  override type ACCESS_HANDLER = Connection

  private lazy val server = createServer()

  private def createServer():Server = {
    Class.forName("org.hsqldb.jdbc.JDBCDriver")

    val properties = new Properties()
    properties.load(Source.fromURL(new URL(conf.getOrElse("server_properties_url", "classpath:grant/analytics/embedded/sql/hsqldb/server.properties"))).bufferedReader())
    val s = new Server()
    s.setProperties(new HsqlProperties(properties))
    s
  }

  override def start(): Unit = {
    server.start()
  }

  override def stop(): Unit = {
    server.stop()
  }

  override def getHandler(): Connection = {
    DriverManager.getConnection(s"jdbc:hsqldb:mem:${conf.getOrElse("db_name", "embedded")}")
  }
}

object DefaultEmbeddedSQL {
  def apply(conf:Map[String, String]):DefaultEmbeddedSQL = {
    new DefaultEmbeddedSQL(conf)
  }
}