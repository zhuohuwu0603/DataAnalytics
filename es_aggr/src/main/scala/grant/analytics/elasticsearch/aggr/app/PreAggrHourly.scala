package grant.analytics.elasticsearch.aggr.app

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}
import java.util.Base64

import com.google.common.hash.Hashing
import com.twitter.algebird.{HyperLogLog, HyperLogLogMonoid}
import grant.analytics.common.event.alternative.{AnalyticsEngageEvent, AnalyticsEvent, AnalyticsViewEvent, DefaultAnalyticsEventParserContainer}
import grant.analytics.common.sectiontrees.SectionTrees
import grant.analytics.common.sectiontrees.mysql.SectionTreesFromMySQL
import grant.analytics.common.url.ClasspathURLEnabler
import grant.analytics.elasticsearch.aggr.conf.PreAggrConfiguration
import grant.analytics.elasticsearch.aggr.event._
import grant.analytics.elasticsearch.aggr.runtime.ElasticsearchPreAggrRuntime
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SQLContext
import org.apache.spark.storage.StorageLevel
import org.elasticsearch.spark.rdd.EsSpark
import org.json4s.JsonAST.{JArray, JInt, JObject, JString}
import org.json4s.jackson.JsonMethods
import org.slf4j.LoggerFactory

import scala.collection.immutable.NumericRange
import scala.util.{Failure, Success, Try}

/**
  * Created by grant on 2017-02-02.
  */
object PreAggrHourly extends App with ClasspathURLEnabler{
  val logger = LoggerFactory.getLogger("com.viafoura.analytics.elasticsearch.aggr.PreAggrHourly")
  def calculateDayMillis(hour: Long):Long = {
    hour - LocalDateTime.ofEpochSecond(hour/1000, 0, ZoneOffset.UTC).toLocalTime.toSecondOfDay*1000
  }

  def generateS3Path(hour: Long, s3_root_path:String, config: PreAggrConfiguration): Array[String] = {
    val hour_in_seconds = LocalDateTime.ofEpochSecond(hour/1000, 0, ZoneOffset.UTC)

    Array(config.getIngestDataDirectory(), config.getContentDataDirectory()).map(queue => {

      val hour = hour_in_seconds.toLocalTime.getHour
      val real_hour =
        if(hour < 10)
          s"0${hour}"
        else
          s"${hour}"

      s"${s3_root_path}/${queue}/${hour_in_seconds.toLocalDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}/${real_hour}/*.gz"
    })
  }

  def getGeneralEvents[T<:AnalyticsEvent](event:T, section_tree: SectionTrees):List[GeneralEvent] = {
    val ancestors = section_tree.getSectionAncestors(event.section_uuid)
    val minute = LocalDateTime.ofEpochSecond(event.time/1000, 0, ZoneOffset.UTC).getMinute
    event.event_type match {
      case "analytics.view" => {
        val view = event.asInstanceOf[AnalyticsViewEvent]
        ancestors.map( uuid =>
          GeneralEvent(event.event_type, uuid.toString, minute, event.device, event.referral, event.unique.toString, 1, if(view.viewcounts == 1) 1 else 0, if(view.recirculation) 1 else 0)
        ) :+ GeneralEvent(event.event_type, event.page_uuid.toString, minute, event.device, event.referral, event.unique.toString, 1,  if(view.viewcounts == 1) 1 else 0, if(view.recirculation) 1 else 0)
      }
      case "analytics.engage" => {
        val engage = event.asInstanceOf[AnalyticsEngageEvent]
        ancestors.map( uuid =>
          GeneralEvent(event.event_type, uuid.toString, minute, event.device, event.referral, event.unique.toString, 1, 0, 0, engage.engage_time, engage.comment_attention)
        ) :+ GeneralEvent(event.event_type, event.page_uuid.toString, minute, event.device, event.referral, event.unique.toString, 1, 0, 0, engage.engage_time, engage.comment_attention)
      }
      case _ => {
        ancestors.map( uuid =>
          GeneralEvent(event.event_type, uuid.toString, minute, event.device, event.referral, event.unique.toString)
        ) :+ GeneralEvent(event.event_type, event.page_uuid.toString, minute, event.device, event.referral, event.unique.toString)
      }
    }
  }

  def generateEntity1(uuid:String, event_type:String, minute:Int, counts:Long, viewCounts:Long, recirculation:Long, attention:Long, comment_attention:Long, device:String, referral:String):List[UUID_Feature_Minute_Counts_Device_Referral] = {

    event_type match {
      case "analytics.view" => {
        List(
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "recirculation", minute, recirculation, device, referral),
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "unique_visitors", minute, viewCounts, device, referral),
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "page_views", minute, counts, device, referral)
        )
      }
      case "analytics.engage" => {
        List(
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "comment_attention", minute, comment_attention, device, referral),
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "attention_time", minute, attention, device, referral)
        )
      }
      case "comment.disliked" => {
        List(
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "comment_dislikes", minute, counts, device, referral)
        )
      }
      case "comment.liked" => {
        List(
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "comment_likes", minute, counts, device, referral)
        )
      }
      case "comment.create" => {
        List(
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "comments", minute, counts, device, referral)
        )
      }
      case "content.follow" => {
        List(
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "all_follows", minute, counts, device, referral)
        )
      }
      case "page.follow" => {
        List(
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "all_follows", minute, counts, device, referral),
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "page_follows", minute, counts, device, referral)
        )
      }
      case "user.follow" => {
        List(
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "all_follows", minute, counts, device, referral),
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "user_follows", minute, counts, device, referral)
        )
      }
      case "user.login" => {
        List(
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "all_logins", minute, counts, device, referral),
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "logins", minute, counts, device, referral)
        )
      }
      case "user.login_third_party" => {
        List(
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "all_logins", minute, counts, device, referral),
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "logins_third_party", minute, counts, device, referral)
        )
      }
      case "user.create" => {
        List(
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "all_signups", minute, counts, device, referral),
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "signups", minute, counts, device, referral)
        )
      }
      case "user.create_third_party" => {
        List(
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "all_signups", minute, counts, device, referral),
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "signups_third_party", minute, counts, device, referral)
        )
      }
      case "page.share" => {
        List(
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "all_shares", minute, counts, device, referral),
          UUID_Feature_Minute_Counts_Device_Referral(uuid, "page_shares", minute, counts, device, referral)
        )
      }
      case s => {
        List(
          UUID_Feature_Minute_Counts_Device_Referral(uuid, s, minute, counts, device, referral)
        )
      }
    }
  }

  def getTimeRange(config:PreAggrConfiguration): NumericRange[Long] = {

    def getStartAndEnd():(Long, Long) = {
      config.getTimeRange()
    }

    val (starting_time_included, ending_time_excluded) =  getStartAndEnd
    val step = 3600000L

    Range.Long(starting_time_included, ending_time_excluded, step)
  }

  def calculateDateString(day_ms:Long):String = {
    LocalDateTime.ofEpochSecond(day_ms/1000, 0, ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE)
  }


  def run(sqlContext:SQLContext, config: PreAggrConfiguration, section_tree:Either[SectionTreesFromMySQL, Broadcast[SectionTreesFromMySQL]]):Unit = {

    getTimeRange(config).foreach(hour_ms => {

      val date_string = calculateDateString(hour_ms)
      val s3_root_path = config.getS3RootPath()

      val paths = generateS3Path(hour_ms, s3_root_path, config)
      paths.foreach(println _)

      import org.json4s.jackson.JsonMethods._
      import sqlContext.implicits._

      val event_parser_container = new DefaultAnalyticsEventParserContainer

      import sqlContext.implicits._

      val event_ds =
        ElasticsearchPreAggrRuntime.session.sqlContext.read.textFile(paths:_*).flatMap(line => {

          val jvalue =
            Try(JsonMethods.parse(line)) match {
              case Success(v) => v
              case Failure(e) => {
                JObject(
                  ("event_type", JString("bad_event")),
                  ("body", JString(line))
                )
              }
            }
          try {
            if(section_tree.isLeft)
              getGeneralEvents(event_parser_container.getParser(jvalue).parse(jvalue), section_tree.left.get)
            else
              getGeneralEvents(event_parser_container.getParser(jvalue).parse(jvalue), section_tree.right.get.value)
          } catch {
            case e: Exception => logger.warn("Failed to parse an event:", e); Seq.empty
          }
        }).persist(StorageLevel.MEMORY_AND_DISK_SER)

      event_ds.take(1)

      val all =
        event_ds.groupByKey(event => {
          UUID_EventType_Minute(event.uuid, event.event_type, event.minute)
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

          generateEntity1(key.uuid, key.event_type, key.minute, tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, "all", "all")

        }).groupByKey(entity1 => {
          UUID_Feature(entity1.uuid, entity1.feature)
        }).mapGroups((key, iterator) => {

          val counts =
            iterator.toList.groupBy(entity1 => {
              entity1.minute
            }).map(tuple => {
              (
                tuple._1,
                tuple._2.foldLeft(0L)(_+_.counts)
              )
            })


          UUID_Feature_Devie_Referral_Values(
            UUID_Feature_Device_Referral(
              key.uuid,
              key.feature,
              "no_filter",
              "no_filter"
            ),
            Range(0,60).map(min => {
              counts.get(min) match {
                case None => 0L
                case Some(value) => value
              }
            }).toArray
          )

        })


      val device_referrals =
        event_ds.groupByKey(event => {
          UUID_Eventtype_Minute_Device_Referral(event.uuid, event.event_type, event.minute, event.device, event.referral)
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

          generateEntity1(key.uuid, key.event_type, key.minute, tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, key.device, key.referral)
        }).groupByKey(entity1 => {
          UUID_Feature_Device_Referral(entity1.uuid, entity1.feature, entity1.device, entity1.referral)
        }).mapGroups((key, iterator) => {

          val counts =
            iterator.toList.groupBy(entity1 => {
              entity1.minute
            }).map(tuple => {
              (
                tuple._1,
                tuple._2.foldLeft(0L)(_+_.counts)
              )
            })

          UUID_Feature_Devie_Referral_Values(
            UUID_Feature_Device_Referral(
              key.uuid,
              key.feature,
              key.device,
              key.referral
            ),
            Range(0,60).map(min => {
              counts.get(min) match {
                case None => 0L
                case Some(value) => value
              }
            }).toArray
          )


        })

      val hash_function = Hashing.murmur3_128()

      EsSpark.saveJsonToEs(
        all.union(device_referrals).map(entity => {

          val hasher = hash_function.newHasher()
          val id = hasher.putBytes(entity.key.uuid.getBytes)
            .putBytes(entity.key.feature.getBytes)
            .putLong(hour_ms)
            .putBytes(entity.key.device.getBytes)
            .putBytes(entity.key.referral.getBytes).hash.toString


          compact(
            render(
              JObject(
                ("content", JString(s"${entity.key.uuid}")),
                ("feature", JString(s"${entity.key.feature}")),
                ("hour", JInt(hour_ms)),
                ("device", JString(s"${entity.key.device}")),
                ("referral", JString(s"${entity.key.referral}")),
                ("counts_per_minute", JArray(entity.values.map(JInt(_)).toList)),
                ("id", JString(id))
              )
            )
          )
        }).rdd,
        s"${config.getCountsIndexPrefix()}-${date_string}/counts",
        Map("es.mapping.id" -> "id")
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

          features.collect{
            case "new_views" if(event.viewCounts == 1) => {
              FilteredUniqueEntity(event.uuid, "new_views", event.referral, event.unique.hashCode)
            }
            case feature if(!feature.equals("new_views")) => {
              FilteredUniqueEntity(event.uuid, feature, event.referral, event.unique.hashCode)
            }
          }
        }).groupByKey(entity => {
          UUID_Feature_Referral(entity.uuid, entity.feature, entity.referral)
        }).mapGroups((key, iterator) => {
          val hllMonoid = new HyperLogLogMonoid(14)

          val hll_str = Base64.getEncoder.encodeToString(
            HyperLogLog.toBytes(
              iterator.foldLeft(hllMonoid.zero)((left, right) => {
                left + hllMonoid.create(right.hash_value)
              })
            )
          )

          val hasher = hash_function.newHasher()
          val id = hasher.putBytes(key.uuid.getBytes)
            .putBytes(key.feature.getBytes)
            .putLong(hour_ms)
            .putBytes(key.referral.getBytes).hash.toString

          compact(render(
            JObject(
              ("content", JString(key.uuid)),
              ("feature", JString(key.feature)),
              ("referral", JString(key.referral)),
              ("hour", JInt(hour_ms)),
              ("hll", JString(hll_str)),
              ("id", JString(id))
            )
          ))
        }).rdd,
        s"${config.getUniquesIndexPrefix()}-${date_string}/uniques",
        Map("es.mapping.id" -> "id")
      )
      event_ds.unpersist()
    })
  }

  ElasticsearchPreAggrRuntime.command_line = args

  val section_tree:Either[SectionTreesFromMySQL, Broadcast[SectionTreesFromMySQL]] =
    if(ElasticsearchPreAggrRuntime.cli_paras.isLocalMode())
      Left(ElasticsearchPreAggrRuntime.section_tree)
    else
      Right(ElasticsearchPreAggrRuntime.session.sparkContext.broadcast(ElasticsearchPreAggrRuntime.section_tree))

  run(
    ElasticsearchPreAggrRuntime.session.sqlContext,
    ElasticsearchPreAggrRuntime.config,
    section_tree
    )

}
