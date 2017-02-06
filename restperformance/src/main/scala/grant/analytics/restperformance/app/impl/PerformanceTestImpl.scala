package grant.analytics.performance.app.impl

import java.net.URL

import grant.analytics.performance.app.PerformanceTest
import grant.analytics.performance.conf.impl.DefaultPerformanceConf

/**
  * Created by grant on 2017-02-05.
  */
class PerformanceTestImpl(val url:URL) extends PerformanceTest{

  val conf = new DefaultPerformanceConf(url)

  override def run(): Unit = {
    val engine = conf.getEngine()
    engine.initialize()
    engine.run(conf.getRequests())
  }
}
