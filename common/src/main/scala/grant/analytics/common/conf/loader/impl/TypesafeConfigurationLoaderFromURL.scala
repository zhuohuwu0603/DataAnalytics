package grant.analytics.common.conf.loader.impl

import java.net.URL

import com.typesafe.config.{Config, ConfigFactory}
import grant.analytics.common.conf.loader.TypesafeConfigurationLoader

/**
  * Created by grant on 2017-03-02.
  */
class TypesafeConfigurationLoaderFromURL(url:URL) extends TypesafeConfigurationLoader{

  override def load(): Config = {
    ConfigFactory.parseURL(url)
  }
}
