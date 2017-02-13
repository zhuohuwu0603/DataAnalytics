package grant.analytics.restperformance.output.impl

import grant.analytics.restperformance.output.{Output, PerformanceTestResponse}

/**
  * Created by grant on 2017-02-11.
  */
class CSVAndTextFilesOutput(file_path:String) extends Output {
  val csv = new CSVFileOutput(file_path+".csv")
  val txt = new TextFileOutput(file_path+".txt")

  override def output(data: List[PerformanceTestResponse]): Unit = {
    csv.output(data)
    txt.output(data)
  }
}
