package grant.analytics.elasticsearch.aggr.conf.impl

import java.net.URL
import java.time.{LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter

import com.typesafe.config.{Config, ConfigFactory}
import grant.analytics.elasticsearch.aggr.conf.PreAggrConfiguration
import grant.analytics.elasticsearch.cli.CLIParas

import scala.util.{Failure, Success, Try}

/**
  * Created by grant on 2017-02-24.
  */
class DefaultPreAggrConfigurationImpl(val url: URL, val cli:CLIParas) extends PreAggrConfiguration{

  private lazy val config = loadConfig()

  private def loadConfig():Config = {
    ConfigFactory.parseURL(url)
  }

  override def getS3RootPath(): String = {
    config.getString("viafoura.analytics.aggregation.s3_root_path")
  }

  override def getESNodes(): String = {
    config.getString("viafoura.analytics.aggregation.elasticsearch_nodes")
  }

  override def getTimeRange(): (Long, Long) = {
    cli.getTimeRange() match{
      case Some(range) => range
      case None => {
        (
          LocalDateTime.parse(config.getString("viafoura.analytics.aggregation.starting_hour_included"), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toEpochSecond(ZoneOffset.UTC)*1000,
          LocalDateTime.parse(config.getString("viafoura.analytics.aggregation.ending_hour_excluded"), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toEpochSecond(ZoneOffset.UTC)*1000
        )
      }
    }

  }

  override def getMySQLProperties(): Map[String, String] = {
    val mysql = config.getConfig("viafoura.analytics.aggregation.mysql")
    Map(
      "url" -> mysql.getString("url"),
      "driver" -> mysql.getString("driver"),
      "user" -> mysql.getString("user"),
      "password" -> mysql.getString("password")
    )
  }

  override def getCountsIndexPrefix(): String = {
    config.getString("viafoura.analytics.aggregation.counts_index_prefix")
  }

  override def getUniquesIndexPrefix(): String = {
    config.getString("viafoura.analytics.aggregation.uniques_index_prefix")
  }

  override def getIngestDataDirectory(): String = {
    Try(config.getString("viafoura.analytics.aggregation.ingest_dir")) match {
      case Success(value) => value
      case Failure(e) => "ingest-hourly-gzip"
    }
  }

  override def getContentDataDirectory(): String = {
    Try(config.getString("viafoura.analytics.aggregation.content_dir")) match {
      case Success(value) => value
      case Failure(e) => "content-events-gzip"
    }
  }
}
