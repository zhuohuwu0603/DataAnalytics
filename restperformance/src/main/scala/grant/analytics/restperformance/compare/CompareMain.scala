package grant.analytics.performance.compare

import java.io.{BufferedWriter, File, FileWriter}
import java.net.URL
import java.util.concurrent.{CountDownLatch, Executors}

import com.typesafe.config.ConfigFactory
import grant.analytics.common.url.ClasspathURLEnabler
import grant.analytics.performance.app.impl.PerformanceTestImpl
import grant.analytics.restperformance.output.PerformanceTestResponse
import grant.analytics.restperformance.output.impl.TextFileOutput

import scala.collection.convert.WrapAsScala
import scala.xml.XML

/**
  * Created by grant on 2017-02-05.
  */
object CompareMain extends App with ClasspathURLEnabler{

  val config = ConfigFactory.parseURL(new URL("classpath:viafoura/analytics/performance/compare.conf"))
  val executors = Executors.newFixedThreadPool(3)
  val latch = new CountDownLatch(3)
  val pattern = """https?.+/v3/analytics(.+)""".r
  val indent = "    "

  WrapAsScala.asScalaBuffer( config.getStringList("viafoura.analytics.performance.compare.configuration_files") ).foreach(url => {

    executors.submit(new Runnable {
      override def run() = {
        (new PerformanceTestImpl(new URL(url))).run()
        latch.countDown()
      }
    })
  })

  latch.await()
  executors.shutdown()

  val responses =
    WrapAsScala.asScalaBuffer(
      config.getStringList("viafoura.analytics.performance.compare.test_output_files")
    ).flatMap(file => {

      val category = file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf("."))

      (XML.loadFile(new File(file)) \ "httpSample").map(node => {

        PerformanceTestResponse(
          (node \ "java.net.URL").text match {
            case pattern(path) => path
          },
          (node \ "@t").text.toLong,
          (node \ "responseData").text,
          category
        )
      })
    }).toList

  val output = new TextFileOutput(config.getString("viafoura.analytics.performance.compare.compare_output_file"))

  output.output(responses)

}


