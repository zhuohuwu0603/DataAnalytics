package grant.analytics.env.embedded.cassandra

import java.net.URL

import grant.analytics.env.embedded.EmbeddedEnvironment

/**
  * Created by grant on 2017-03-20.
  */
trait EmbeddedCassandra extends EmbeddedEnvironment{
  def loadSchemas(schemas:List[String]):Unit
  def loadSchemas(schema_file:URL):Unit
}
