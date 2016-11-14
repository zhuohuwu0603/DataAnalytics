package grant.analytics.common.repo

import grant.analytics.common.event.alternative.AnalyticsEvent

/**
 * Created by grant on 2016-11-14.
 */
trait AnalyticsRepository[EVENT] {
  type CONTAINER <: RepositoryContainer[EVENT]
  def save(domain_objects: CONTAINER)
}
