package grant.analytics.elasticsearch.agg

import java.util.UUID

/**
  * Created by grant on 2016-12-07.
  */
case class GeneralEvent(val event_type:String,
                        val uuid:String,
                        val minute: Int,
                        val device: String,
                        val referral_type:String,
                        val unique:String,
                        val counts:Long = 1L,
                        val viewCounts: Long = 0,
                        val recirculation: Long = 0,
                        val attention:Long = 0,
                        val comment_attention: Long = 0)

case class Entity1(val uuid:String,
                   val feature:String,
                   val minute:Int,
                   val counts:Long,
                   val device:String,
                   val referral_type:String)

case class CountEntity(val uuid:String,
                       val feature:String,
                       val values: Array[Long])

case class CountEntity_Device(val uuid:String,
                              val feature:String,
                              val device:String,
                              val values: Array[Long])

case class CountEntity_ReferralType(val uuid:String,
                                    val feature:String,
                                    val referral_type:String,
                                    val values: Array[Long])

case class Key(val uuid:String,
               val feature:String)
case class Value(val filter_type:String,
                 val filter_instance_name:String,
                 val values:Seq[Long])

case class FilteredCountEntity(val uuid:String,
                               val feature:String,
                               val filter_type:String,
                               val filter_instance_name:String,
                               val values:Array[Long])


case class FilterDevice(val hour:Int,
                        val name:String,
                        val counts:Array[Long])

case class FilterReferralType(val hour:Int,
                              val name:String,
                              val counts:Array[Long])


case class HourlyAggrEntity(val hour:Int,
                            val key:Key,
                            val all: Array[Long],
                            val devices: Seq[FilterDevice],
                            val referrals: Seq[FilterReferralType])

case class HourlyAggrUniqueEntity(val key:Key,
                                  val hll: String)