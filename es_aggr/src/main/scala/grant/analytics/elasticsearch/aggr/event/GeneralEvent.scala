package grant.analytics.elasticsearch.aggr.event


/**
  * Created by grant on 2016-12-07.
  */
case class GeneralEvent(val event_type:String,
                        val uuid:String,
                        val minute: Int,
                        val device: String,
                        val referral:String,
                        val unique:String,
                        val counts:Long = 1L,
                        val viewCounts: Long = 0,
                        val recirculation: Long = 0,
                        val attention:Long = 0,
                        val comment_attention: Long = 0)


case class UUID_Feature_Minute_Counts_Device_Referral(val uuid:String,
                                                      val feature:String,
                                                      val minute:Int,
                                                      val counts:Long,
                                                      val device:String,
                                                      val referral:String)

case class UUID_Feature(val uuid:String,
                        val feature:String)

case class UUID_Feature_Referral(val uuid:String,
                                 val feature:String,
                                 val referral:String)


case class UUID_EventType_Minute(val uuid:String,
                                 val event_type:String,
                                 val minute:Int)

case class UUID_Eventtype_Minute_Device_Referral(val uuid:String,
                                                 val event_type:String,
                                                 val minute:Int,
                                                 val device:String,
                                                 val referral:String)


case class UUID_Feature_Device_Referral(val uuid:String,
                                        val feature:String,
                                        val device:String,
                                        val referral:String)

case class UUID_Feature_Devie_Referral_Values(val key:UUID_Feature_Device_Referral,
                                    val values:Array[Long])

case class FilteredUniqueEntity(val uuid:String,
                                val feature:String,
                                val referral:String,
                                val hash_value:Int)

