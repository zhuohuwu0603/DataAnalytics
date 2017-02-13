package grant.analytics.restperformance.output

/**
  * Created by grant on 2017-02-11.
  */
trait Output {
  def output(data:List[PerformanceTestResponse])

}
