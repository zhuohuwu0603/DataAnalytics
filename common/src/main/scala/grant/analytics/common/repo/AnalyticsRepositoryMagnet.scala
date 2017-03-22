package grant.analytics.common.repo

import grant.analytics.common.event.alternative.AnalyticsEvent
import grant.analytics.common.event.dynamic.common.event.dynamic.GeneralAnalyticsEvent
import org.apache.spark.rdd.RDD

/**
 * Created by grant on 2016-11-14.
  *
  * in Magnet pattern, if you want to pass in multiple arguments, the corresponding implicit function should have a tuple as the input parameter
 */
sealed trait AnalyticsRepositoryMagnet {
  type Result
  def apply():Result
}

object AnalyticsRepositoryMagnet {
  implicit def save_rdd_analytics_event(data: RDD[AnalyticsEvent]) = {
    new AnalyticsRepositoryMagnet {

      override type Result = Unit
      override def apply():Result = {

      }
    }
  }

  implicit def save_list_analytics_event(data: List[AnalyticsEvent]) = {
    new AnalyticsRepositoryMagnet {

      override type Result = Unit

      override def apply(): Result = ???
    }
  }

  implicit def save_rdd_general_analytics_event(data: List[GeneralAnalyticsEvent]) = {
    new AnalyticsRepositoryMagnet {

      override type Result = Unit

      override def apply(): Result = ???
    }
  }

  implicit def save_list_general_analytics_event(data: List[GeneralAnalyticsEvent]) = {
    new AnalyticsRepositoryMagnet {

      override type Result = Unit

      override def apply(): Result = ???
    }
  }
}
