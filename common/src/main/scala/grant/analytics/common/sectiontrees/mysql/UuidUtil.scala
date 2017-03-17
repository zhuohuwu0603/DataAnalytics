package grant.analytics.common.sectiontrees.mysql

import java.util.UUID

/**
  * Created by grant on 2016-12-08.
  */
object UuidUtil {
  def toUuid(id: String): UUID = {
    java.util.UUID.fromString(id)
  }

  def toUuid(id: BigInt) : UUID = UUID.fromString(f"00000000-0000-4000-8000-${id.longValue()}%012x")
}