package grant.analytics.env.embedded.cassandra.impl

import java.io.{FileOutputStream, ObjectInputStream}
import java.net.URL
import java.nio.file.Files

import com.datastax.driver.core.{Cluster, Session}
import com.google.common.reflect.ClassPath
import grant.analytics.common.url.ClasspathURLEnabler
import grant.analytics.env.embedded.cassandra.EmbeddedCassandra
import org.apache.cassandra.service.{CassandraDaemon, EmbeddedCassandraService}

import scala.collection.convert.WrapAsScala
import scala.io.Source

/**
  * Created by grant on 2017-03-20.
  *
  * The default configuration items include:
  * 
  *
  */
class DefaultEmbeddedCassandra(conf:Map[String, String]) extends EmbeddedCassandra with ClasspathURLEnabler{

  override type ACCESS_HANDLER = Session

  private lazy val cassandra = createCassandra()
  private lazy val cluster = Cluster.builder().addContactPoint("localhost").build()
  private lazy val temp_dir = Files.createTempDirectory("embedded_cassandra")

  private def createCassandra():CassandraDaemon = {
    new CassandraDaemon
  }

  private def copySigarFile2TempDir():String = {

    WrapAsScala.asScalaSet( ClassPath.from(getClass.getClassLoader).getResources ).filter(ri => {
      ri.getResourceName.contains("grant/analytics/embedded/cassandra/sigar")
    }).foreach(resource => {
      val res_path = resource.getResourceName
      val is = getClass.getClassLoader.getResourceAsStream(res_path)
      val os = new FileOutputStream(s"${temp_dir.toString}/${res_path.substring(res_path.lastIndexOf("/")+1)}")
      var buffer = new Array[Byte](1024)
      while(is.read(buffer) != -1){
        os.write(buffer)
        buffer = new Array[Byte](1024)
      }
      os.flush()
      os.close()
      is.close()
    })

    println(temp_dir.toString)
    temp_dir.toFile.deleteOnExit()

    temp_dir.toString
  }

  override def start(): Unit = {

//    val current_thread = Thread.currentThread()
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      override def run(): Unit = {
        temp_dir.toFile.deleteOnExit()
//        current_thread.join()
      }
    }))


    System.setProperty("cassandra.config", "classpath:grant/analytics/embedded/cassandra/cassandra.yaml")
    System.setProperty("java.library.path", copySigarFile2TempDir())
    cassandra.init(null)
    cassandra.start()
  }

  override def stop(): Unit = {
    cluster.close()
    cassandra.stop()
    Files.deleteIfExists(temp_dir)
  }

  override def getHandler(): Session = {
    cluster.connect() // the user should be responsible for releasing the resource
  }

  override def loadSchemas(schemas: List[String]): Unit = {
    val session = cluster.connect()
    schemas.foreach(session.execute(_))
    session.close()
  }

  override def loadSchemas(schema_file: URL): Unit = {
    val session = cluster.connect()
    Source.fromURL(schema_file).getLines().foreach(session.execute(_))
    session.close()
  }
}

object DefaultEmbeddedCassandra{
  def apply(conf:Map[String, String]):DefaultEmbeddedCassandra = {
    new DefaultEmbeddedCassandra(conf)
  }
}