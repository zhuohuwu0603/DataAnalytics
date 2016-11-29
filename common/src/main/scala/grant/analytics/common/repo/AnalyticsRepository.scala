package grant.analytics.common.repo

/**
 * Created by grant on 2016-11-14.
 */
object AnalyticsRepository {
  def saveToRepository(magnet: AnalyticsRepositoryMagnet):magnet.Result = magnet()
}
