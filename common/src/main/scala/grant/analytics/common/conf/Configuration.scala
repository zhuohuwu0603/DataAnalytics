package grant.analytics.common.conf

import grant.analytics.common.conf.parser.ConfigurationParser

/**
 * Created by grant on 2016-11-14.
 */
trait Configuration {
  type PARSER <: ConfigurationParser
  def getParser(): PARSER
}
