package grant.analytics.elasticsearch.agg

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}
import java.util.Base64

import com.twitter.algebird.{HyperLogLog, HyperLogLogMonoid}
import grant.analytics.common.event.DefaultAnalyticsEventParserContainer
import grant.analytics.common.event.alternative.{AnalyticsEngageEvent, AnalyticsEvent, AnalyticsViewEvent}
import grant.analytics.common.url.ClasspathURLEnabler
import org.elasticsearch.spark.rdd.EsSpark
import org.json4s.JsonAST._
import org.json4s.jackson.JsonMethods

/**
  * Created by grant on 2016-12-15.
  */
object PreAggrHourly_ds extends App with ClasspathURLEnabler{


  def calculateDayMillis(hour: Long):Long = {
    hour - LocalDateTime.ofEpochSecond(hour/1000, 0, ZoneOffset.UTC).toLocalTime.toSecondOfDay*1000
  }

  def generateS3Path(hour: Long, s3_root_path:String): Array[String] = {
    val hour_in_seconds = LocalDateTime.ofEpochSecond(hour/1000, 0, ZoneOffset.UTC)

    Array("content-events-gzip", "ingest-hourly-gzip").map(queue => {

      val hour = hour_in_seconds.toLocalTime.getHour
      val real_hour =
        if(hour < 10)
          s"0${hour}"
        else
          s"${hour}"

      s"${s3_root_path}/${queue}/${hour_in_seconds.toLocalDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}/${real_hour}"
    })
  }

  def getGeneralEvents[T<:AnalyticsEvent](event:T):List[GeneralEvent] = {
    val ancestors = ElasticsearchPreAggrRuntime.section_tree.getSectionAncestors(event.section_uuid)
    val minute = LocalDateTime.ofEpochSecond(event.time/1000, 0, ZoneOffset.UTC).getMinute
    event.event_type match {
      case "analytics.view" => {
        val view = event.asInstanceOf[AnalyticsViewEvent]
        ancestors.map( uuid =>
          GeneralEvent(event.event_type, uuid.toString, minute, event.device, event.referral, event.unique.toString, 1, 1, 0)
        ) :+ GeneralEvent(event.event_type, event.page_uuid.toString, minute, event.device, event.referral, event.unique.toString, 1,  1, 0)
      }
      case "analytics.engage" => {
        val engage = event.asInstanceOf[AnalyticsEngageEvent]
        ancestors.map( uuid =>
          GeneralEvent(event.event_type, uuid.toString, minute, event.device, event.referral, event.unique.toString, 1, 0, 0, 0, 0)
        ) :+ GeneralEvent(event.event_type, event.page_uuid.toString, minute, event.device, event.referral, event.unique.toString, 1, 0, 0, 0, 0)
      }
      case _ => {
        ancestors.map( uuid =>
          GeneralEvent(event.event_type, uuid.toString, minute, event.device, event.referral, event.unique.toString)
        ) :+ GeneralEvent(event.event_type, event.page_uuid.toString, minute, event.device, event.referral, event.unique.toString)
      }
    }
  }

  def generateEntity1(uuid:String, event_type:String, minute:Int, counts:Long, viewCounts:Long, recirculation:Long, attention:Long, comment_attention:Long, device:String, referral:String):List[Entity1] = {

    event_type match {
      case "analytics.view" => {
        List(
          Entity1(uuid, "recirculation", minute, recirculation, device, referral),
          Entity1(uuid, "unique_visitors", minute, viewCounts, device, referral),
          Entity1(uuid, "page_views", minute, counts, device, referral)
        )
      }
      case "analytics.engage" => {
        List(
          Entity1(uuid, "comment_attention", minute, comment_attention, device, referral),
          Entity1(uuid, "attention_time", minute, attention, device, referral)
        )
      }
      case "comment.disliked" => {
        List(
          Entity1(uuid, "comment_dislikes", minute, counts, device, referral)
        )
      }
      case "comment.liked" => {
        List(
          Entity1(uuid, "comment_likes", minute, counts, device, referral)
        )
      }
      case "comment.create" => {
        List(
          Entity1(uuid, "comments", minute, counts, device, referral)
        )
      }
      case "comment.share" => {
        List(
          Entity1(uuid, "comment_shares", minute, counts, device, referral)
        )
      }
      case "content.follow" => {
        List(
          Entity1(uuid, "all_follows", minute, counts, device, referral)
        )
      }
      case "page.follow" => {
        List(
          Entity1(uuid, "all_follows", minute, counts, device, referral),
          Entity1(uuid, "page_follows", minute, counts, device, referral)
        )
      }
      case "user.follow" => {
        List(
          Entity1(uuid, "all_follows", minute, counts, device, referral),
          Entity1(uuid, "user_follows", minute, counts, device, referral)
        )
      }
      case "user.login" => {
        List(
          Entity1(uuid, "all_logins", minute, counts, device, referral),
          Entity1(uuid, "logins", minute, counts, device, referral)
        )
      }
      case "user.login_third_party" => {
        List(
          Entity1(uuid, "all_logins", minute, counts, device, referral),
          Entity1(uuid, "logins_third_party", minute, counts, device, referral)
        )
      }
      case "user.create" => {
        List(
          Entity1(uuid, "all_signups", minute, counts, device, referral),
          Entity1(uuid, "signups", minute, counts, device, referral)
        )
      }
      case "user.create_third_party" => {
        List(
          Entity1(uuid, "all_signups", minute, counts, device, referral),
          Entity1(uuid, "signups_third_party", minute, counts, device, referral)
        )
      }
      case "page.share" => {
        List(
          Entity1(uuid, "all_shares", minute, counts, device, referral),
          Entity1(uuid, "page_shares", minute, counts, device, referral)
        )
      }
      case "user.share" => {
        List(
          Entity1(uuid, "all_shares", minute, counts, device, referral)
        )
      }
      case s => {
        List(
          Entity1(uuid, s, minute, counts, device, referral)
        )
      }
    }
  }

  val hour_ms_list = List(1476867600000L, 1476871200000L) // 2016/10/19 9AM and 10AM UTC

  hour_ms_list.foreach(hour_ms => {

    val day_ms : Long = calculateDayMillis(hour_ms)
    val s3_root_path = "/Users/grant/work/data"

    val paths = generateS3Path(hour_ms, s3_root_path)
    paths.foreach(println _)


    val sc = ElasticsearchPreAggrRuntime.sc
    val session = ElasticsearchPreAggrRuntime.session

    import org.json4s.jackson.JsonMethods._

    sc.broadcast(
      ElasticsearchPreAggrRuntime.section_tree
    )

    val event_parser_container = new DefaultAnalyticsEventParserContainer

    val event_ds =
      session.read.text(paths:_*).flatMap(line => {
        val jvalue = JsonMethods.parse(line.getString(0))
        getGeneralEvents(event_parser_container.getParser(jvalue).parse(jvalue))
      }).persist()

    val all =
      event_ds.groupByKey(event => {
        Key1(event.uuid, event.event_type, event.minute)
      }).flatMapGroups((key, iterator) => {
        val tuple = iterator.foldLeft((0L, 0L, 0L, 0L, 0L))((right, left) => {
          (
            right._1 + left.counts,
            right._2 + left.viewCounts,
            right._3 + left.recirculation,
            right._4 + left.attention,
            right._5 + left.comment_attention
          )
        })

        generateEntity1(key.uuid, key.event_type, key.minute, tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, null, null)

      }).groupByKey(entity1 => {
        Key(entity1.uuid, entity1.feature)
      }).mapGroups((key, iterator) => {

        val counts =
          iterator.map(entity1 => {
            (entity1.minute, entity1.counts)
          }).toMap

        FilteredCountEntity(
          key.uuid,
          key.feature,
          "all",
          "all",
          Range(0,60).map(min => {
            counts.get(min) match {
              case None => 0L
              case Some(value) => value
            }
          }).toArray
        )
      })

    val devices =
      event_ds.groupByKey(event => {
        Key2(event.uuid, event.event_type, event.minute, event.device)
      }).flatMapGroups((key, iterator) => {
        val tuple = iterator.foldLeft((0L, 0L, 0L, 0L, 0L))((right, left) => {
          (
            right._1 + left.counts,
            right._2 + left.viewCounts,
            right._3 + left.recirculation,
            right._4 + left.attention,
            right._5 + left.comment_attention
          )
        })

        generateEntity1(key.uuid, key.event_type, key.minute, tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, key.device, null)
      }).groupByKey(entity1 => {
        Key2WithDevice(entity1.uuid, entity1.feature, entity1.device)
      }).mapGroups((key, iterator) => {

        val counts =
          iterator.map(entity1 => {
            (entity1.minute, entity1.counts)
          }).toMap

        FilteredCountEntity(
          key.uuid,
          key.feature,
          "device",
          key.device,
          Range(0,60).map(min => {
            counts.get(min) match {
              case None => 0L
              case Some(value) => value
            }
          }).toArray
        )
      })

    val referrals =
      event_ds.groupByKey(event => {
        Key3(event.uuid, event.event_type, event.minute, event.referral_type)
      }).flatMapGroups((key, iterator) => {
        val tuple = iterator.foldLeft((0L, 0L, 0L, 0L, 0L))((right, left) => {
          (
            right._1 + left.counts,
            right._2 + left.viewCounts,
            right._3 + left.recirculation,
            right._4 + left.attention,
            right._5 + left.comment_attention
          )
        })

        generateEntity1(key.uuid, key.event_type, key.minute, tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, null, key.referral)
      }).groupByKey(entity1 => {
        Key2WithReferral(entity1.uuid, entity1.feature, entity1.referral_type)
      }).mapGroups((key, iterator) => {

        val counts =
          iterator.map(entity1 => {
            (entity1.minute, entity1.counts)
          }).toMap

        FilteredCountEntity(
          key.uuid,
          key.feature,
          "referral_type",
          key.referral,
          Range(0,60).map(min => {
            counts.get(min) match {
              case None => 0L
              case Some(value) => value
            }
          }).toArray
        )
      })

    EsSpark.saveJsonToEs(

      all.union(devices).union(referrals).groupByKey(entity => {
        Key(entity.uuid, entity.feature)
      }).mapGroups((key, iterator) => {
        val inner_map =
          iterator.toList.groupBy(entity => {
            entity.filter_type
          }).map(tuple => {
            (
              tuple._1,
              tuple._2.map(entity => {
                (entity.filter_instance_name, entity.values)
              })
            )
          })

        val devices_ =
          inner_map.get("device").get.map(tuple => {
            JObject(
              ("name", JString(s"${tuple._1}")),
              ("counts_per_minute", JArray(tuple._2.map(JInt(_)).toList))
            )
          })
        val referral_types_ =
          inner_map.get("referral_type").get.map(tuple => {
            JObject(
              ("name", JString(s"${tuple._1}")),
              ("counts_per_minute", JArray(tuple._2.map(JInt(_)).toList))
            )
          })
        compact(
          render(
            JObject(
              ("content", JString(s"${key.uuid}")),
              ("feature", JString(s"${key.feature}")),
              ("hour", JInt(hour_ms)),
              ("day", JInt(day_ms)),
              ("filter", JObject(
                ("all", JArray(inner_map.get("all").get(0)._2.map(JInt(_)).toList)),
                ("device", JArray(devices_.toList)),
                ("referral_type", JArray(referral_types_.toList))
              ))
            )
          )
        )

      }).rdd,
      "pre_aggr_hourly/counts"
    )

    import com.twitter.algebird.HyperLogLog._

    EsSpark.saveJsonToEs(

      event_ds.filter(event => {
        event.event_type.equals("analytics.view") ||
          event.event_type.equals("comment.create") ||
          event.event_type.equals("comment.liked") ||
          event.event_type.equals("content.follow") ||
          event.event_type.equals("page.share")
      }).flatMap(event => {
        val features = event.event_type match {
          case "analytics.view" => List("new_views", "views")
          case "comment.create" => List("comments")
          case "comment.liked" => List("likes")
          case "content.follow" => List("follows")
          case "page.share" => List("shares")
        }
        features.map(feature => {
          FilteredUniqueEntity(event.uuid, feature, event.unique.hashCode)

        })
      }).groupByKey(entity => {
        Key(entity.uuid, entity.feature)
      }).mapGroups((key, iterator) => {
        val hllMonoid = new HyperLogLogMonoid(14)

        val hll_str = Base64.getEncoder.encodeToString(
          HyperLogLog.toBytes(
            iterator.foldLeft(hllMonoid.zero)((left, right) => {
              left + hllMonoid.create(right.hash_value)
            })
          )
        )

        compact(render(
          JObject(
            ("content", JString(s"${key.uuid}")),
            ("feature", JString(s"${key.feature}")),
            ("hour", JInt(hour_ms)),
            ("day", JInt(day_ms)),
            ("hll", JString(hll_str))
          )
        ))
      }).rdd,
      "pre_aggr_hourly/uniques"
    )

  })

}