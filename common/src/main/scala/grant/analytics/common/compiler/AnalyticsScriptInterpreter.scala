package grant.analytics.common.compiler

import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain

/**
  * Created by grant on 2016-04-22.
  * Assumption is that the returned variable named "ret"
  */
class AnalyticsScriptInterpreter {

  private lazy val main = getMain()

  private def getMain(): IMain = {
    val settings = new Settings()
    settings.usejavacp.value = true
    new IMain(settings)
  }

  def eval(code:String):Option[Any] = {
    val ret = main.interpret(code)
    main.valueOfTerm("ret")
  }
}
