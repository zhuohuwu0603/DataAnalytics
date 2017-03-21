package grant.analytics.env.embedded.zookeeper.impl

import grant.analytics.env.embedded.zookeeper.EmbeddedZooKeeper
import org.apache.zookeeper.server.ZooKeeperServer

/**
  * Created by grant on 2017-03-20.
  */
class DefaultEmbeddedZooKeeper extends EmbeddedZooKeeper{

  override type ACCESS_HANDLER = ZooKeeperServer

  override def start(): Unit = ???

  override def stop(): Unit = ???

  override def getHandler(): ZooKeeperServer = ???
}
