package grant.analytics.common.sectiontrees.mysql

import scala.collection.Seq

/**
  * Created by grant on 2016-12-08.
  */
class TreeNode[T](val id: T, var children: Map[T, TreeNode[T]]) {
  var parentNode = None: Option[T]

  def this(_id: T) = {
    this(_id, Map.empty)
  }

  def addChild(child: TreeNode[T]): TreeNode[T] = {
    child.parentNode = Some(id)
    children += (child.id -> child)
    this
  }

  /**
    * Find all ordered descendent paths originating from the current TreeNode, including current
    *
    * @return
    */
  def findPaths(): Seq[Seq[T]] = {
    Seq(Seq(id)) ++ children
      .flatMap({ case (_, child) => child.findPaths() })
      .map(id +: _)
      .toSeq
  }

}