package grant.analytics.common.repo

import grant.analytics.common.event.alternative.AnalyticsEvent
import org.apache.spark.rdd.RDD

/**
 * Created by grant on 2016-11-14.
 */
trait DomainObjectCollection[T] {
  type COLLECTION[T]
  def initialize(data:COLLECTION[T]):Unit
}

class RDDDomainObjectCollection extends DomainObjectCollection[AnalyticsEvent]{
  type COLLECTION = RDD[AnalyticsEvent]

  override def initialize(data: RDD[AnalyticsEvent]): Unit = ???
}

class ListDomainObjectCollection extends DomainObjectCollection[AnalyticsEvent]{
  type COLLECTION = List[AnalyticsEvent]

  override def initialize(data: List[AnalyticsEvent]): Unit = ???
}