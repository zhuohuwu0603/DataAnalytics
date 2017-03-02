package grant.analytics.common.conf.loader

import com.typesafe.config.Config

/**
  * Created by grant on 2017-03-02.
  */
trait TypesafeConfigurationLoader extends ConfigurationLoader{
  override type OUTPUT = Config
}
