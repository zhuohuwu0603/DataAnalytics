package grant.analytics.performance.app

import java.net.URL

import grant.analytics.common.url.ClasspathURLEnabler
import grant.analytics.performance.app.impl.PerformanceTestImpl

/**
  * Created by grant on 2017-01-17.
  */
object PerformanceTestMain extends App with ClasspathURLEnabler{

  val config_path = if(args.length == 0){
    "classpath:viafoura/analytics/performance/performance_non_aggr.conf"
  }
  else{
    args(0)
  }

  val url = new URL(config_path)

  (new PerformanceTestImpl(url)).run()




}
