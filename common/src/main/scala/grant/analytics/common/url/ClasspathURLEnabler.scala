package grant.analytics.common.url

import java.net.URL

/**
  * Created by grant on 2016-04-06.
  */
trait ClasspathURLEnabler{

  ClasspathURLEnabler.getClass.synchronized {
    if(!ClasspathURLEnabler.enabled){
      URL.setURLStreamHandlerFactory(new ClasspathURLStreamHandlerFactory)
      ClasspathURLEnabler.enabled = true
    }

  }

}

object ClasspathURLEnabler {
  private var enabled = false;
}
