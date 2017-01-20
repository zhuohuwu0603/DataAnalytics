package grant.analytics.performance.app

import java.net.URL

import grant.analytics.common.url.ClasspathURLEnabler
import grant.analytics.performance.conf.impl.DefaultPerformanceConf

/**
  * Created by grant on 2017-01-20.
  */
object PerformanceTestMain extends App with ClasspathURLEnabler{


  def getConfURL():URL = {
    new URL("classpath:viafoura/analytics/performance/performance.conf")
  }

  val url = getConfURL()
  val conf = new DefaultPerformanceConf(url)

  val engine = conf.getEngine()
  engine.initialize()
  engine.run(conf.getRequests())


}
