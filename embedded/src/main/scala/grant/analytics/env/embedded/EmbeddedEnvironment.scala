package grant.analytics.env.embedded

/**
  * Created by grant on 2017-03-20.
  */
trait EmbeddedEnvironment {

  type ACCESS_HANDLER

  def start():Unit
  def stop():Unit
  def getHandler():ACCESS_HANDLER

}
