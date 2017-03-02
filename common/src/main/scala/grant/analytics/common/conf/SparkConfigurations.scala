package grant.analytics.common.conf

/**
  * Created by grant on 2017-03-02.
  */
case class SparkConfigurations(val appName:String,
                               val streaming: SparkStreamingConfigurations)

case class SparkStreamingConfigurations(val interval:Int,
                                        val kafka: KafkaConfigurations)

case class KafkaConfigurations(val broker_list:String,
                               val topics:String,
                               val auto_offset_reset: String = "largest",
                               val auto_commit_enable: Boolean = false)

case class MysqlConfigurations(val dsn:String,
                               val jdbc_driver:String,
                               val user:String,
                               val password:String)

case class CassandraConfigurations(val contact_points: String,
                                   val keyspace:String,
                                   val keepalive_ms:Long,
                                   val consistency_level:String)

case class ZookeeperConfigurations(val connection:String,
                                   val timeout:Long,
                                   val custom_path:String)

case class ElasticsearchConfigurations(val nodes:String,
                                       val port:Int,
                                       val cluster_name:String)