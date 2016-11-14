package grant.analytics.common.conf.parser.impl

import java.net.URL

import com.typesafe.config.{Config, ConfigFactory}
import grant.analytics.common.conf.parser.ConfigurationParser

/**
 * Created by grant on 2016-11-14.
 */
class DefaultConfigurationParser extends ConfigurationParser{
  override type INPUT = URL
  override type OUTPUT = Config

  override def parse(input: URL): Config = {
    ConfigFactory.parseURL(input)
  }

}
