package grant.analytics.env.embedded.kafka.impl

import java.util.Properties

import grant.analytics.env.embedded.kafka.EmbeddedKafka
import kafka.server.KafkaServerStartable

import scala.io.Source

/**
  * Created by grant on 2017-03-21.
  *
  * configuration should include:
  *
  * configuration_url: kafka properties file url
  */
class DefaultEmbeddedKafka(conf:Map[String, String]) extends EmbeddedKafka{

  override type ACCESS_HANDLER = KafkaServerStartable

  private lazy val kafka = createKafka()

  private def createKafka(): KafkaServerStartable = {

    val props = new Properties()
    props.load(Source.fromURL(conf.getOrElse("configuration_url", "classpath:grant/analytics/embedded/kafka/kafka.properties")).bufferedReader())
    KafkaServerStartable.fromProps(props)
  }

  override def start(): Unit = kafka.startup()

  override def stop(): Unit = kafka.shutdown()

  override def getHandler(): KafkaServerStartable = throw new Exception("handlers are not available, please try to connnect through Producer or Consumer")
}
