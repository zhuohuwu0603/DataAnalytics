package grant.analytics.performance.model.engine.impl.jmeter

import java.io.File
import java.text.DateFormat

import grant.analytics.performance.model.RequestGroup
import grant.analytics.performance.model.engine.TestEngine
import org.apache.jmeter.config.{Arguments, ConfigTestElement}
import org.apache.jmeter.control.LoopController
import org.apache.jmeter.engine.StandardJMeterEngine
import org.apache.jmeter.protocol.http.sampler.HTTPSampler
import org.apache.jmeter.reporters.ResultCollector
import org.apache.jmeter.samplers.SampleSaveConfiguration
import org.apache.jmeter.testelement.TestPlan
import org.apache.jmeter.util.JMeterUtils
import org.apache.jorphan.collections.HashTree
import org.apache.jmeter.threads.ThreadGroup


/**
  * Created by grant on 2017-01-20.
  */
class JMeterTestEngine(host:String, port:Int, config: Map[String, String]) extends TestEngine{

  private lazy val engine = createEngine()

  private def createEngine():StandardJMeterEngine = {
    new StandardJMeterEngine()
  }

  override def initialize(): Unit = {
    JMeterUtils.loadJMeterProperties(
      config.get("jmeter_home") match {
        case Some(value) => s"${value}/bin/jmeter.properties"
        case None => ""
      }
    )// In the source code, if property file loading failed, a classpath file org/apache/jmeter/jmeter.properties will be loaded instead

    JMeterUtils.setJMeterHome(config.get("jmeter_home").get)

    val output = new File(config.get("output_file_path").get)
    if(output.exists()){
      output.delete()

    }
    output.createNewFile()

  }

  override def run(groups: List[RequestGroup]): Unit = {

    val hashTree = new HashTree()

    val plan = new TestPlan("Analytics-Dispatcher REST API")

    hashTree.add(plan)

    val defaults = new ConfigTestElement
    defaults.setProperty("HTTPSampler.domain", host)
    defaults.setProperty("HTTPSampler.port", port)
    defaults.setProperty("HTTPSampler.protocol", "https")
    defaults.setProperty("HTTPSampler.contentEncoding", "json/application")
    defaults.setProperty("HTTPSampler.implementation", "HttpClient4")

    val controller = new LoopController
    controller.setLoops(1)
    controller.setFirst(true)

    val requests =
      groups.flatMap(group => {
        group.requests.map(request => {
          val sampler = new HTTPSampler()
          sampler.setPath(request.path)
          val args = new Arguments
          request.args.foreach(tuple => {
            args.addArgument(tuple._1, tuple._2)
          })
          sampler.setArguments(args)
          sampler.setMethod(request.method)
          sampler
        })
      })

    val threadGroup = new ThreadGroup
    threadGroup.setNumThreads(
      config.get("numOfThreads") match {
        case Some(value) => value.toInt
        case None => 1
      }
    )
    threadGroup.setRampUp(1)
    threadGroup.setSamplerController(controller)

    val listener = new ResultCollector()
    listener.setFilename(config.get("output_file_path").get)

    val sampleSaveConf = new SampleSaveConfiguration()
    sampleSaveConf.setAssertionResultsFailureMessage(true)
    sampleSaveConf.setAssertions(true)
    sampleSaveConf.setAsXml(true)
    sampleSaveConf.setBytes(true)
    sampleSaveConf.setCode(true)
    sampleSaveConf.setConnectTime(true)
    sampleSaveConf.setDataType(true)
    sampleSaveConf.setDefaultTimeStampFormat()
    sampleSaveConf.setEncoding(true)
    sampleSaveConf.setFieldNames(true)
    sampleSaveConf.setFileName(true)
    sampleSaveConf.setHostname(true)
    sampleSaveConf.setIdleTime(true)
    sampleSaveConf.setFormatter(DateFormat.getDateTimeInstance)
    sampleSaveConf.setLabel(true)
    sampleSaveConf.setLatency(true)
    sampleSaveConf.setMessage(true)
    sampleSaveConf.setRequestHeaders(true)
    sampleSaveConf.setResponseData(true)
    sampleSaveConf.setResponseHeaders(true)
    sampleSaveConf.setSampleCount(true)
    sampleSaveConf.setSamplerData(true)
    sampleSaveConf.setSentBytes(true)
    sampleSaveConf.setSubresults(true)
    sampleSaveConf.setSuccess(true)
    sampleSaveConf.setThreadCounts(true)
    sampleSaveConf.setThreadName(true)
    sampleSaveConf.setTime(true)
    sampleSaveConf.setTimestamp(true)
    sampleSaveConf.setUrl(true)

    listener.setSaveConfig(sampleSaveConf)

    plan.addThreadGroup(threadGroup)



    val threadGroupTree = hashTree.add(plan, threadGroup)

    threadGroupTree.add(defaults)
    threadGroupTree.add(listener)

    requests.foreach(threadGroupTree.add(_))


    engine.configure(hashTree)

    engine.run

  }
}
