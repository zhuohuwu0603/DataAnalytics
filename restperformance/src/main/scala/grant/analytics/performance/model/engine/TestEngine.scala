package grant.analytics.performance.model.engine

import grant.analytics.performance.model.RequestGroup

/**
  * Created by grant on 2017-01-20.
  */
trait TestEngine {

  def initialize():Unit
  def run(groups: List[RequestGroup]):Unit

}
