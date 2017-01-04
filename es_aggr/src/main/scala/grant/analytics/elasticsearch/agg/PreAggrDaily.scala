package grant.analytics.elasticsearch.agg

import java.time.{LocalDateTime, ZoneOffset}
import java.util.Base64

import com.twitter.algebird.{HyperLogLog, HyperLogLogMonoid}
import grant.analytics.common.url.ClasspathURLEnabler
import org.elasticsearch.spark.rdd.EsSpark
import org.json4s.JInt
import org.json4s.JsonAST.{JArray, JObject, JString}
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.JsonMethods.{compact, render}

import scala.collection.mutable.{HashMap => MutableHashMap}

/**
  * Created by grant on 2016-12-19.
  */
object PreAggrDaily extends App with ClasspathURLEnabler{

  def getHourlyAggrEntity(json:String): HourlyAggrEntity = {
    val jvalue = JsonMethods.parse(json)
    val hour = LocalDateTime.ofEpochSecond(  (jvalue \ "hour").values.toString.toLong / 100, 0, ZoneOffset.UTC).getHour
    val key = Key((jvalue \ "content").values.toString, (jvalue \ "feature").values.toString)
    val all = (jvalue \ "filter" \ "all" \ classOf[JInt]).map(_.toLong).toArray

    val devices =
    (jvalue \ "filter" \ "device" \ classOf[JObject]).map(fields_map => {
      val name = fields_map.get("name").get.toString
      val counts =  (fields_map.get("counts_per_minute").get match {
        case JArray(list) => list.map(cc => {
          cc match {
            case JInt(internal_value) => internal_value.toLong
          }
        })
      }).toArray
      FilterDevice(hour, name, counts)
    })

    val referrals =
      (jvalue \ "filter" \ "referral_type" \ classOf[JObject]).map(fields_map => {
        val name = fields_map.get("name").get.toString
        val counts =  (fields_map.get("counts_per_minute").get match {
          case JArray(list) => list.map(cc => {
            cc match {
              case JInt(internal_value) => internal_value.toLong
            }
          })
        }).toArray
        FilterReferralType(hour, name, counts)
      })
    HourlyAggrEntity(hour, key, all, devices, referrals)
  }

  def getHourlyAggrUniqueEntity(json:String): HourlyAggrUniqueEntity = {
    val jvalue = JsonMethods.parse(json)
    val key = Key((jvalue \ "content").values.toString, (jvalue \ "feature").values.toString)
    HourlyAggrUniqueEntity(key, (jvalue \ "hll").values.toString)
  }

  val day_ms = 1476835200000L   // 2016-10-19

  import org.elasticsearch.spark._

  val session = ElasticsearchPreAggrRuntime.session
  val sc = ElasticsearchPreAggrRuntime.sc


  val counts_rdd =
  session.createDataset[HourlyAggrEntity](
    sc.esJsonRDD("pre_aggr_hourly/counts", s"?q=${day_ms}").map(tuple => {
      getHourlyAggrEntity(tuple._2)
    })
  ).groupByKey(entity => {
    entity.key
  })
    .mapGroups((key, iterator) => {
    val all =
    iterator.map(entity => {
      (entity.hour, entity.all)
    }).toMap

    val all_list =
      Range(0,24,1).flatMap(min => {
        all.get(min) match {
          case Some(counts) => {
            counts.map(JInt(_))
          }
          case None => {
            (new Array[JInt](60)).map(value => JInt(0))
          }
        }
      }).toList

    val devices =
    iterator.flatMap(_.devices).toList.groupBy(device => {
      device.name
    }).map(tuple => {
      (
        tuple._1,
        tuple._2.groupBy(device => {
          device.hour
        }).map(inner_tuple => {
          (
            inner_tuple._1,
            inner_tuple._2(0).counts
          )
        }).toMap
      )
    }).map(tuple => {

      JObject(
        ("name", JString(tuple._1)),
        ("counts_per_minute", JArray(
          Range(0,24,1).flatMap(hour => {
            tuple._2.get(hour) match {
              case Some(counts) => {
                counts.map(JInt(_))
              }
              case None => {
                (new Array[JInt](60)).map(value => JInt(0))
              }
            }
          }).toList
        ))
      )
    })

    val referrals =
    iterator.flatMap(_.referrals).toList.groupBy(referral => {
      referral.name
    }).map(tuple => {
      (
        tuple._1,
        tuple._2.groupBy(referral => {
          referral.hour
        }).map(inner_tuple => {
          (
            inner_tuple._1,
            inner_tuple._2(0).counts
          )
        }).toMap
      )
    }).map(tuple => {

      JObject(
        ("name", JString(tuple._1)),
        ("counts_per_minute", JArray(
          Range(0,24,1).flatMap(hour => {
            tuple._2.get(hour) match {
              case Some(counts) => {
                counts.map(JInt(_))
              }
              case None => {
                (new Array[JInt](60)).map(value => JInt(0))
              }
            }
          }).toList
        ))
      )
    })

    import JsonMethods._
    compact(
      render(
        JObject(
          ("content", JString(key.uuid)),
          ("feature", JString(key.feature)),
          ("day", JInt(day_ms)),
          ("filter", JObject(
            ("all", JArray(all_list)),
            ("device", JArray(devices.toList)),
            ("referral_type", JArray(referrals.toList))
          ))
        )
      )
    )

  }).rdd

  EsSpark.saveJsonToEs(counts_rdd, "pre_aggr_daily/counts")

  val uniques_rdd =
  session.createDataset[HourlyAggrUniqueEntity](
    sc.esJsonRDD("pre_aggr_hourly/uniques", s"?q=${day_ms}").map(tuple => {
      getHourlyAggrUniqueEntity(tuple._2)
    })
  ).groupByKey(entity => {
    entity.key
  })
    .mapGroups((key, iterator) => {
    val hll_monoid = new HyperLogLogMonoid(14)

    val hll_str =
      Base64.getEncoder.encodeToString(
        HyperLogLog.toBytes(
          iterator.foldLeft(hll_monoid.zero)((left, right) => {
            left + hll_monoid.create(Base64.getDecoder.decode(right.hll))
          })
        )
      )
    compact(
      render(
        JObject(
          ("content", JString(key.uuid)),
          ("feature", JString(key.feature)),
          ("day", JInt(day_ms)),
          ("hll", JString(hll_str))
        )
      )
    )
  }).rdd

  EsSpark.saveJsonToEs(uniques_rdd, "pre_aggr_daily/uniques")

}
