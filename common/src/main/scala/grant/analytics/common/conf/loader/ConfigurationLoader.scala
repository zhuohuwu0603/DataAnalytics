package grant.analytics.common.conf.loader

/**
  * Created by grant on 2017-03-02.
  */
trait ConfigurationLoader {
  type OUTPUT
  def load():OUTPUT
}
