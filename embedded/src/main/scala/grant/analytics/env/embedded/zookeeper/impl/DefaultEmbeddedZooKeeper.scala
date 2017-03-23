package grant.analytics.env.embedded.zookeeper.impl

import grant.analytics.env.embedded.zookeeper.EmbeddedZooKeeper
import org.apache.zookeeper.ZooKeeper
import org.apache.zookeeper.server.ZooKeeperServer

/**
  * Created by grant on 2017-03-20.
  *
  * refer to the implementation of ZookeeperServerMain
  *
  * https://github.com/apache/zookeeper/blob/master/src/java/main/org/apache/zookeeper/server/ZooKeeperServerMain.java
  *
  */
class DefaultEmbeddedZooKeeper(conf:Map[String, String]) extends EmbeddedZooKeeper{

  override type ACCESS_HANDLER = ZooKeeper

  private lazy val zk = createZookeeperServer()

  private def createZookeeperServer():ZooKeeperServer = {
    val zk_server = new ZooKeeperServer()
    zk_server
  }

  override def start(): Unit = {

  }

  override def stop(): Unit = {
    zk.shutdown()
  }

  override def getHandler(): ZooKeeper = {
    new ZooKeeper("localhost", 2181, null)
  }
}
