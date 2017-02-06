package grant.analytics.performance.model.engine.impl.jmeter

import java.io.File
import java.text.DateFormat

import grant.analytics.performance.model.RequestGroup
import grant.analytics.performance.model.engine.TestEngine
import org.apache.jmeter.control.LoopController
import org.apache.jmeter.engine.StandardJMeterEngine
import org.apache.jmeter.protocol.http.control.{Cookie, CookieManager}
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy
import org.apache.jmeter.reporters.ResultCollector
import org.apache.jmeter.samplers.SampleSaveConfiguration
import org.apache.jmeter.testelement.TestPlan
import org.apache.jmeter.threads.ThreadGroup
import org.apache.jmeter.util.JMeterUtils
import org.apache.jorphan.collections.HashTree

/**
  * Created by grant on 2017-01-17.
  */
class JMeterTestEngine(config: Map[String, String], vf_session:String = null) extends TestEngine{

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

    val testPlanTree = new HashTree()

    val testPlan = new TestPlan("Analytics-Dispatcher REST API")

    val controller = new LoopController
    controller.setLoops(1)
    controller.setFirst(true)
    controller.initialize()

    val samplers =
    groups.flatMap(group => {
      group.requests.map(request => {
        val sampler = new HTTPSamplerProxy()
        sampler.setName(request.path)
        sampler.setDomain(request.host)
        sampler.setPort(request.port)
        sampler.setProtocol(request.protocol)
        sampler.setContentEncoding(request.content_encoding)
        sampler.setImplementation("HttpClient4")
        sampler.setPath(request.path)
        request.args.foreach(tuple => {
          sampler.addNonEncodedArgument(tuple._1, tuple._2, "=")
        })
        sampler.setMethod(request.method)

        if(vf_session != null){
          val cookieMgr = new CookieManager
          val cookie = new Cookie()
          cookie.setName("VfSess")
          cookie.setValue(vf_session)
          cookie.setDomainSpecified(true)
          cookie.setDomain(".viafoura.co")
          cookie.setPathSpecified(true)
          cookie.setSecure(false)
          cookie.setExpires(0)
          cookie.setPath("/")
          cookieMgr.add(cookie)
          cookieMgr.setCookiePolicy("standard")
          cookieMgr.setImplementation("org.apache.jmeter.protocol.http.control.HC4CookieHandler")
          cookieMgr.setClearEachIteration(true)
          cookieMgr.testStarted() // weird function call, if it is not called, the internal cookieHandler won't be initialized
          sampler.setCookieManager(cookieMgr)
        }
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

    testPlanTree.add(testPlan)

    val threadGroupTree = testPlanTree.add(testPlan, threadGroup)

    threadGroupTree.add(listener)

    samplers.foreach(threadGroupTree.add(_))

    engine.configure(testPlanTree)

    engine.run

  }
}
