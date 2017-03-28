package grant.analytics.elasticsearch.cli.impl

import java.time.{LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter

import grant.analytics.elasticsearch.cli.CLIParas
import org.apache.commons.cli._

/**
  * Created by grant on 2017-02-24.
  */
class DefaultCLIParasImpl(args:Array[String]) extends CLIParas{

  private lazy val parser = createCLIParser()
  private lazy val time_range = parseTimeRange()
  private lazy val options = createOptions()
  private lazy val parsed_cmd = parseCommandline()

  private def parseCommandline():CommandLine = {
    parser.parse(options, args)
  }

  private def createCLIParser(): CommandLineParser = {
    new DefaultParser
  }

  private def parseTimeRange(): scala.Option[(Long, Long)] = {
    if(args == null || args.size == 0) {
      None
    }
    else{
      // the format of 'start' and 'end' follows DateTimeFormatter.ISO_OFFSET_DATE_TIME
      // for example : 2011-12-03T10:15:30+01:00

      if(parsed_cmd.hasOption("s")){
        val start = LocalDateTime.parse( parsed_cmd.getOptionValue("s"), DateTimeFormatter.ISO_OFFSET_DATE_TIME ).toEpochSecond(ZoneOffset.UTC)*1000
        val end =
          if(parsed_cmd.hasOption("e")){
            LocalDateTime.parse( parsed_cmd.getOptionValue("e"), DateTimeFormatter.ISO_OFFSET_DATE_TIME ).toEpochSecond(ZoneOffset.UTC)*1000
          }
          else{
            start + 3600000L
          }
        Some((start, end))
      }
      else{
        None
      }
    }
  }

  private def createOptions(): Options = {
    val o = new Options
    o.addOption(Option.builder("s").argName("start_time").longOpt("start").hasArg.numberOfArgs(1).required(false).build())
    o.addOption(Option.builder("e").argName("end_time").longOpt("end").hasArg.numberOfArgs(1).required(false).build())
    o.addOption(Option.builder("l").argName("local_mode").longOpt("local").required(false).build())
    o
  }

  override def getTimeRange(): scala.Option[(Long, Long)] = {
    time_range
  }

  override def isLocalMode(): Boolean = {
    parsed_cmd.hasOption("l")
  }
}
