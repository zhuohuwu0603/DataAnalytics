package grant.analytics.common.conf.parser

/**
 * Created by grant on 2016-11-14.
 */
trait ConfigurationParser {
  type INPUT
  type OUTPUT
  def parse(input:INPUT):OUTPUT
}
