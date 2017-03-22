package grant.analytics.env.embedded.elasticsearch.impl

import java.net.{InetAddress, URL}
import java.nio.file.Files
import java.util

import grant.analytics.env.embedded.elasticsearch.EmbeddedElasticsearch
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest
import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.node.Node
import org.elasticsearch.transport.client.PreBuiltTransportClient

import scala.io.Source
import org.elasticsearch.transport.Netty3Plugin
import org.elasticsearch.node.internal.InternalSettingsPreparer
import org.elasticsearch.plugins.Plugin


/**
  * Created by grant on 2017-03-20.
  *
  * default configuration items include:
  * cluster.name: cluster's name, which is needed when trying to get java client
  * path.home.prefix: used as temporay directory's name prefix
  *
  */
class DefaultEmbeddedElasticsearch(conf:Map[String, String]) extends EmbeddedElasticsearch{

  override type ACCESS_HANDLER = Client

  private lazy val node = createNode()
  private lazy val cluster_name = conf.getOrElse("cluster.name", "analytics")
  private lazy val homeDir = Files.createTempDirectory(conf.getOrElse("path.home.prefix", "embedded_es_"))
  private lazy val settings = Settings
    .builder()
    .put("node.name", "embedded_node")
    .put("path.home", homeDir.toString)
    .put("cluster.name", cluster_name)
    .put("transport.type", "local")
    .put("http.enabled", true)
    .put("http.type", "netty3")
    .build()

  private def createNode():Node = {

    // the purpose is that transport modules should be in the classpath, then pass the class object to Node's constructor. This solution works for 5.0 or above
    // refer to the following link:
    // http://stackoverflow.com/questions/41263143/unsupported-http-type-netty3-when-trying-to-start-embedded-elasticsearch-node

    class PluginConfigurableNode[T<:Plugin](override val settings: Settings)
      extends Node(InternalSettingsPreparer.prepareEnvironment(settings, null), util.Arrays.asList(classOf[Netty3Plugin]))

    new PluginConfigurableNode(settings)

//    new Node(settings)
  }

  override def start(): Unit = {
    node.start()
  }

  override def stop(): Unit = {
    node.close()
    Files.deleteIfExists(homeDir)
  }

  override def loadTemplate(url: URL): Unit = {
    def generateTemporayTemplateName(url:URL):String = {
      val path = url.getPath
      path.substring(path.lastIndexOf("/")+1, path.indexOf(".")) + "_template"
    }

    val client = node.client()
    val index_template_name = generateTemporayTemplateName(url)
    val index_template_req = new PutIndexTemplateRequest(index_template_name)
    val sources = Source.fromURL(url).getLines().mkString("\n").getBytes
    client.admin().indices().putTemplate(index_template_req.source(sources))
    client.close()

  }

  override def getHandler(): Client = {
//    node.client()

    new PreBuiltTransportClient(Settings.builder().put("cluster.name", cluster_name).build())
      .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))
  }
}

object DefaultEmbeddedElasticsearch {
  def apply(conf: Map[String, String]): DefaultEmbeddedElasticsearch = {
    new DefaultEmbeddedElasticsearch(conf)
  }
}
