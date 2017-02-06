package grant.analytics.performance.conf

import grant.analytics.performance.model.RequestGroup
import grant.analytics.performance.model.engine.TestEngine


/**
  * Created by grant on 2017-01-16.
  */
trait PerformanceConf {
  type ENGINE <: TestEngine
  def getGlobals():Map[String, String]
  def getRequests(): List[RequestGroup]
  def getEngine(): ENGINE
}
