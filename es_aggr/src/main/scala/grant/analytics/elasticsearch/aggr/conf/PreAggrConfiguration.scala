package grant.analytics.elasticsearch.aggr.conf

/**
  * Created by grant on 2017-02-24.
  */
trait PreAggrConfiguration {

  def getS3RootPath():String
  def getESNodes():String
  def getTimeRange(): (Long, Long)
  def getMySQLProperties():Map[String, String]
  def getCountsIndexPrefix():String
  def getUniquesIndexPrefix():String
  def getIngestDataDirectory():String
  def getContentDataDirectory():String
}
