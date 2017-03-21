package grant.analytics.env.embedded.sql.impl

import java.sql.Connection

import grant.analytics.env.embedded.sql.EmbeddedSQL

/**
  * Created by grant on 2017-03-21.
  */
class DefaultEmbeddedSQL extends EmbeddedSQL{
  override type ACCESS_HANDLER = Connection

  override def start(): Unit = ???

  override def stop(): Unit = ???

  override def getHandler(): Connection = ???
}
