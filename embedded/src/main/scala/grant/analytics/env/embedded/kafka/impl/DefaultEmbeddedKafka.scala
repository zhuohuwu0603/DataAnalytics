package grant.analytics.env.embedded.kafka.impl

import grant.analytics.env.embedded.kafka.EmbeddedKafka
import kafka.server.KafkaServerStartable

/**
  * Created by grant on 2017-03-21.
  */
class DefaultEmbeddedKafka extends EmbeddedKafka{

  override type ACCESS_HANDLER = KafkaServerStartable

  override def start(): Unit = ???

  override def stop(): Unit = ???

  override def getHandler(): KafkaServerStartable = ???
}
