package grant.analytics.jetty

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.servlet.ServletContainer

/**
  * Created by grant on 2017-04-21.
  */
object EmbeddedJetty extends App{

  val config = new ResourceConfig
  config.packages("grant.analytics.jetty")
  val servlet = new ServletHolder(new ServletContainer(config))
  val server = new Server(7070)
  val context = new ServletContextHandler(server, "/viafoura")
  context.addServlet(servlet, "/*")

  try {
    server.start()
    server.join()
  } catch {
    case e: Exception =>
      e.printStackTrace()
  } finally server.destroy()

}
