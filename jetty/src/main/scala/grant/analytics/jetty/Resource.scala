package grant.analytics.jetty

import javax.ws.rs.core.MediaType
import javax.ws.rs._

/**
  * Created by grant on 2017-04-21.
  */
@Path("server")
class Resource {

  @POST
  @Path("info")
  @Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def info(@FormParam("key") key:String):String = {
    s"""
       {
         "ret": "${key}"
       }
    """.stripMargin
  }

}
