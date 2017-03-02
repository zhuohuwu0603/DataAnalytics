package grant.analytics.common.conf.parser

/**
 * Created by grant on 2016-11-14.
 */
trait ConfigurationParser {
  type IN_MEMORY_TYPE
  lazy val config = loadConfig()
  def loadConfig():IN_MEMORY_TYPE
}
