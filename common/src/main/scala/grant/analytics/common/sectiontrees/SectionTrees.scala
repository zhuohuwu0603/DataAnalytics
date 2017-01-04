package grant.analytics.common.sectiontrees

import java.util.UUID

/**
  * Created by grant on 2017-01-03.
  */
trait SectionTrees {
  def getSectionAncestors(section: UUID): List[UUID]
  def getAll():List[(UUID, Set[UUID])]
}
