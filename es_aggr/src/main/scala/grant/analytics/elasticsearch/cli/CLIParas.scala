package grant.analytics.elasticsearch.cli

/**
  * Created by grant on 2017-02-24.
  */
trait CLIParas {
  def getTimeRange(): Option[(Long, Long)]
  def isLocalMode():Boolean
}
