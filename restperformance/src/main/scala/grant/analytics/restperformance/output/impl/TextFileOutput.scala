package grant.analytics.restperformance.output.impl

import java.io.{BufferedWriter, File, FileWriter}

import grant.analytics.restperformance.output.{Output, PerformanceTestResponse}

/**
  * Created by grant on 2017-02-11.
  */
class TextFileOutput(file_path:String) extends Output{
  private lazy val writer: BufferedWriter = getWriter()

  private def getWriter():BufferedWriter = {
    new BufferedWriter(new FileWriter( new File(file_path)))
  }
  override def output(data: List[PerformanceTestResponse]): Unit = ???
}
