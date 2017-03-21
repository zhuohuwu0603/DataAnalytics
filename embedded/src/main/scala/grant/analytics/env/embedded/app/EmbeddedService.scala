package grant.analytics.env.embedded.app

import grant.analytics.env.embedded.elasticsearch.impl.DefaultEmbeddedElasticsearch

/**
  * Created by grant on 2017-03-21.
  */
object EmbeddedService extends App{
  val conf_es = Map(
    "cluster.name" -> "grant",
    "path.home.prefix" -> "elasticsearch_temp_"
  )

  DefaultEmbeddedElasticsearch(conf_es).start()

  Thread.sleep(300000L)

}
