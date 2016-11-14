package grant.analytics.common.compiler

import scala.collection.mutable
import scala.reflect.internal.util.{AbstractFileClassLoader, BatchSourceFile}
import scala.reflect.io.VirtualDirectory
import scala.tools.nsc.{Global, Settings}

/**
  * Created by grant on 2016-04-22.
  */
class AnalyticsDynamicScriptCompiler {
  private lazy val (global, classLoader) = getCompiler()

  private val classCache = mutable.Map[String, Class[_]]()

  private def getCompiler(): (Global, ClassLoader) = {
    val targetDir = new VirtualDirectory("memory", None)
    val settings = new Settings()
    settings.deprecation.value = true // enable detailed deprecation warnings
    settings.unchecked.value = true // enable detailed unchecked warnings
    settings.outputDirs.setSingleOutput(targetDir)
    settings.usejavacp.value = true

    (new Global(settings), new AbstractFileClassLoader(targetDir, this.getClass.getClassLoader) )

  }

  def compile(classname:String, code: String): Class[_] = {
    val run = new global.Run

    val sourceFiles = List(new BatchSourceFile(classname, code))
    run.compileSources(sourceFiles)

    synchronized{
      classCache.get(classname).orElse({
        val cls = classLoader.loadClass(classname)
        classCache(classname) = cls
        Some(cls)
      }).get
    }
//    classLoader.loadClass(classname)
  }
}
