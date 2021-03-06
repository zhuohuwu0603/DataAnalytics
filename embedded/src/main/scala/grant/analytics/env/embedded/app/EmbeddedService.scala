package grant.analytics.env.embedded.app

import grant.analytics.env.embedded.cassandra.impl.DefaultEmbeddedCassandra
import grant.analytics.env.embedded.sql.impl.DefaultEmbeddedSQL

/**
  * Created by grant on 2017-03-21.
  */
object EmbeddedService extends App{
  val conf = Map(
    "cluster.name" -> "grant",
    "path.home.prefix" -> "elasticsearch_temp_",
    "sigar.path" -> "/Users/grant/work/cassandra/apache-cassandra-3.5/lib/sigar-bin",
    "db_name" -> "embedded",
    "user" -> "grant",
    "password" -> "vf",
    "server_properties_url"->"classpath:grant/analytics/embedded/sql/hsqldb/server.properties"
  )

  println(System.getProperty("os.name"))
  println(System.getProperty("os.arch"))
  println(System.getProperty("os.version"))

//  DefaultEmbeddedCassandra(conf).start()

  val hsqldb = DefaultEmbeddedSQL(conf)
  hsqldb.start()
  hsqldb.getHandler()

  Thread.sleep(300000L)

}
