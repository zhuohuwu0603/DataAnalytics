package grant.analytics.env.embedded.zookeeper.impl

import java.io.File
import java.net.URL

import grant.analytics.env.embedded.zookeeper.EmbeddedZooKeeper
import org.apache.zookeeper.ZooKeeper
import org.apache.zookeeper.server.persistence.FileTxnSnapLog
import org.apache.zookeeper.server.{ServerCnxnFactory, ServerConfig, ZooKeeperServer, ZooKeeperServerMain}

import scala.io.Source

/**
  * Created by grant on 2017-03-20.
  *
  * refer to the implementation of ZookeeperServerMain
  *
  * https://github.com/apache/zookeeper/blob/master/src/java/main/org/apache/zookeeper/server/ZooKeeperServerMain.java
  *
  * configuration should include:
  *
  * configuation_url: zookeeper property file url
  *
  */
class DefaultEmbeddedZooKeeper(conf:Map[String, String]) extends EmbeddedZooKeeper{

  override type ACCESS_HANDLER = ZooKeeper

  private lazy val zk = createZookeeperServer()

  private lazy val cnxnFactory = ServerCnxnFactory.createFactory
  

  private def createZookeeperServer():ZooKeeperServer = {
    val zk_server = new ZooKeeperServer()
    zk_server
  }

  override def start(): Unit = {
    val config = new ServerConfig
    config.parse(
      Source.fromURL(
        new URL(conf.getOrElse("configuration_url", "classpath:grant/analytics/embedded/zookeeper/zk.properties"))
      ).getLines().filter(_.trim.equals("")).toArray
    )

    val txnLog = new FileTxnSnapLog(new File(config.getDataLogDir), new File(config.getDataDir))
    zk.setTxnLogFactory(txnLog)
    zk.setTickTime(config.getTickTime)
    zk.setMinSessionTimeout(config.getMinSessionTimeout)
    zk.setMaxSessionTimeout(config.getMaxClientCnxns)
    cnxnFactory.configure(config.getClientPortAddress, config.getMaxClientCnxns)
    cnxnFactory.startup(zk)

    // seems the above codes should be run in a separate thread
  }

  override def stop(): Unit = {
    zk.shutdown()
    cnxnFactory.shutdown()
  }

  override def getHandler(): ZooKeeper = {
    new ZooKeeper("localhost", 2181, null)
  }
}
