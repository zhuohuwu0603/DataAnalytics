package grant.analytics.elasticsearch.cli

/**
  * Created by grant on 2017-03-01.
  */
trait CLIParas {
  def getTimeRange(): Option[(Long, Long)]
}
