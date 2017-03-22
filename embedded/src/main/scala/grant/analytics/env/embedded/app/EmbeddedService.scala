package grant.analytics.env.embedded.app

import grant.analytics.env.embedded.cassandra.impl.DefaultEmbeddedCassandra

/**
  * Created by grant on 2017-03-21.
  */
object EmbeddedService extends App{
  val conf_es = Map(
    "cluster.name" -> "grant",
    "path.home.prefix" -> "elasticsearch_temp_",
    "sigar.path" -> "/Users/grant/work/cassandra/apache-cassandra-3.5/lib/sigar-bin"
  )

  println(System.getProperty("os.name"))
  println(System.getProperty("os.arch"))
  println(System.getProperty("os.version"))

  DefaultEmbeddedCassandra(conf_es).start()

  Thread.sleep(300000L)

}
