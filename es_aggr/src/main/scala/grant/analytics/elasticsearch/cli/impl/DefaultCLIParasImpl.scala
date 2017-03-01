package grant.analytics.elasticsearch.cli.impl

import java.time.{LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter

import grant.analytics.elasticsearch.cli.CLIParas
import org.apache.commons.cli.{CommandLineParser, DefaultParser, Option, Options}

/**
  * Created by grant on 2017-03-01.
  */
class DefaultCLIParasImpl(args:Array[String]) extends CLIParas{

  private lazy val parser = createCLIParser()
  private lazy val time_range = parseCommand()
  private lazy val options = createOptions()

  private def createCLIParser(): CommandLineParser = {
    new DefaultParser
  }

  private def parseCommand(): scala.Option[(Long, Long)] = {
    if(args == null || args.size == 0) {
      None
    }
    else{
      // the format of 'start' and 'end' follows DateTimeFormatter.ISO_OFFSET_DATE_TIME
      // for example : 2011-12-03T10:15:30+01:00

      val cmd = parser.parse(options, args)
      if(cmd.hasOption("s")){
        val start = LocalDateTime.parse( cmd.getOptionValue("s"), DateTimeFormatter.ISO_OFFSET_DATE_TIME ).toEpochSecond(ZoneOffset.UTC)*1000
        val end =
          if(cmd.hasOption("e")){
            LocalDateTime.parse( cmd.getOptionValue("e"), DateTimeFormatter.ISO_OFFSET_DATE_TIME ).toEpochSecond(ZoneOffset.UTC)*1000
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
    o
  }

  override def getTimeRange(): scala.Option[(Long, Long)] = {
    time_range
  }
}
