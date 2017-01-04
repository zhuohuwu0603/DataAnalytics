package grant.analytics.common.event.alternative

import java.util.UUID

/**
 * Created by grant on 2016-11-12.
 */
sealed abstract class AnalyticsEvent(val event_type:String,
                                      val event_uuid:UUID,
                             val page_uuid:UUID,
                             val section_uuid:UUID,
                             val time:Long,
                             val unique: UUID,
                             val device: String,
                             val referral: String)

case class AnalyticsViewEvent(override val event_uuid:UUID,
                              override val page_uuid:UUID,
                              override val section_uuid:UUID,
                              override val time:Long,
                              override val unique: UUID,
                              override val device: String,
                              override val referral: String ) extends AnalyticsEvent("analytics.view", event_uuid, page_uuid, section_uuid, time, unique,device, referral)

case class AnalyticsEngageEvent(override val event_uuid:UUID,
                                override val page_uuid:UUID,
                                override val section_uuid:UUID,
                                override val time:Long,
                                override val unique: UUID,
                                override val device: String,
                                override val referral: String,
                                 val engage_time: Long) extends AnalyticsEvent("analytics.engage", event_uuid, page_uuid, section_uuid, time, unique,device, referral)

case class GeneralContentEvent(override val event_type:String,
                               override val event_uuid:UUID,
                               override val section_uuid: UUID,
                               override val page_uuid: UUID,
                               override val time: Long,
                               override val device: String,
                               override val referral: String,
                               override val unique:UUID) extends AnalyticsEvent(event_type, event_uuid, section_uuid, page_uuid, time, unique, device, referral)