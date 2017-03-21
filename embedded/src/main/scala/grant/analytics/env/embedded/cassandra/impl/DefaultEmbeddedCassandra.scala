package grant.analytics.env.embedded.cassandra.impl

import com.datastax.driver.core.Session
import grant.analytics.env.embedded.cassandra.EmbeddedCassandra

/**
  * Created by grant on 2017-03-20.
  */
class DefaultEmbeddedCassandra extends EmbeddedCassandra{

  override type ACCESS_HANDLER = Session

  override def start(): Unit = ???

  override def stop(): Unit = ???

  override def getHandler(): Session = ???
}
