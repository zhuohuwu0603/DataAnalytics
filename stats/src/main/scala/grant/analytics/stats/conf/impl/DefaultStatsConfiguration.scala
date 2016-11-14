package grant.analytics.stats.conf.impl

import java.net.URL

import grant.analytics.common.conf.impl.AbstractAnalyticsConfiguration
import grant.analytics.stats.conf.StatsConfiguration

/**
 * Created by grant on 2016-11-14.
 */
class DefaultStatsConfiguration(val url:URL) extends AbstractAnalyticsConfiguration(url) with StatsConfiguration{

}
