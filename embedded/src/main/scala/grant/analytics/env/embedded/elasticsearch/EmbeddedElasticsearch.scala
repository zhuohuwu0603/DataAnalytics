package grant.analytics.env.embedded.elasticsearch

import java.net.URL

import grant.analytics.env.embedded.EmbeddedEnvironment

/**
  * Created by grant on 2017-03-20.
  */
trait EmbeddedElasticsearch extends EmbeddedEnvironment{
  def loadTemplate(url:URL):Unit
}
