package grant.analytics.common.url

import java.net.{URLStreamHandler, URLStreamHandlerFactory}

/**
  * Created by grant on 2016-04-05.
  */
class ClasspathURLStreamHandlerFactory extends URLStreamHandlerFactory{
  lazy val handler = createHandler()

  private def createHandler():URLStreamHandler = {
    new ClasspathURLStreamHandler
  }
  override def createURLStreamHandler(protocol: String): URLStreamHandler = {
    protocol match {
      case "classpath" => handler
      case _ => null
    }
  }
}
