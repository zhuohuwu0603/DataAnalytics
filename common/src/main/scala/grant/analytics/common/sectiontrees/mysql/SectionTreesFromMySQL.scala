package grant.analytics.common.sectiontrees.mysql

import java.math.{BigDecimal => JBigDecimal}
import java.util.UUID

import grant.analytics.common.sectiontrees.SectionTrees
import org.apache.spark.sql.SQLContext

/**
  * Created by grant on 2017-03-17.
  */
class SectionTreesFromMySQL(mysqlOptions:Map[String, String], sqlContext:SQLContext) extends SectionTrees with Serializable{

  private val cache: Map[UUID, scala.collection.immutable.Set[UUID]] = createCache()

  override def getSectionAncestors(section: UUID): List[UUID] = {
    cache.get(section) match {
      case Some(ancestors) => ancestors.toList
      case None => List(section)
    }
  }

  private def createCache(): Map[UUID, scala.collection.immutable.Set[UUID]] = {
    sqlContext.read.format("jdbc").options(mysqlOptions + ("dbtable" -> "Site_groups")).load().registerTempTable("Site_groups")
    sqlContext.read.format("jdbc").options(mysqlOptions + ("dbtable" -> "Client_site_groups")).load().registerTempTable("Client_site_groups")
    sqlContext.read.format("jdbc").options(mysqlOptions + ("dbtable" -> "Sites")).load().registerTempTable("Sites")
    sqlContext.read.format("jdbc").options(mysqlOptions + ("dbtable" -> "Sections")).load().registerTempTable("Sections")

    var nodes: Map[Long, TreeNode[Long]] = Map.empty

    import sqlContext.implicits._

    sqlContext.read.format("jdbc").options(mysqlOptions + ("dbtable" -> "Section_trees")).load()
      .where("ancestor != descendant")
      .where("depth = 1")
      .map(row => (row.getAs[JBigDecimal]("ancestor").longValue, row.getAs[JBigDecimal]("descendant").longValue))
      .collect.foreach({
      case (ancestor: Long, descendant: Long) => {

        if (!nodes.contains(ancestor)) {
          nodes += (ancestor -> new TreeNode[Long](ancestor))
        }

        if (!nodes.contains(descendant)) {
          val newNode = new TreeNode[Long](descendant)
          newNode.parentNode = Some(ancestor)
          nodes += (descendant -> newNode)
        }
        else if (nodes.contains(descendant) && nodes.get(descendant).get.parentNode.nonEmpty) {
          nodes.get(descendant).get.parentNode = Some(ancestor)
        }

        nodes.get(ancestor).get.addChild(nodes.get(descendant).get)
      }
    })

    val sectionTrees = nodes.filter({ case (_, node) => node.parentNode.isEmpty }) // find root nodes
      .map({
      case (_, node) => node.findPaths().map(path => {
        // find paths from each root
        (path.last -> path)
      }).toMap
    }).reduce(_ ++ _)

    val sectionsRdd = sqlContext.sql(" SELECT sg.id AS client_id, " +
      "       IF(s.cassandra_section_tree_site_group != null, s.cassandra_section_tree_site_group, csg.site_group_id) AS site_group_id, " +
      "       s.id AS site_id, " +
      "       section_id " +
      " from Site_groups sg " +
      "    JOIN Client_site_groups csg ON (csg.client_id = sg.id) " +
      "    JOIN Sites s ON (s.id = csg.site_id) " +
      "    JOIN Sections sc ON (sc.site_id = s.id) " +
      " WHERE sg.processing_status = 1 AND s.processing_status = 1 AND sc.processing_status = 1 "
    ).map(row => {
      Seq(row.getAs[JBigDecimal]("client_id").longValue, row.getAs[JBigDecimal]("site_group_id").longValue, row.getAs[JBigDecimal]("site_id").longValue, row.getAs[JBigDecimal]("section_id").longValue)
    })

    sectionsRdd.map(seq => {
      //match up section trees with their accompanying site groups etc.
      val path = if (sectionTrees.contains(seq.last)) {
        seq.take(seq.length - 2) ++ sectionTrees.get(seq.last).get
      }
      else {
        seq.take(seq.length - 1)
      }

      (path.last, path.distinct)
    }).map({
      case (key, value) => (UuidUtil.toUuid(key), value.map(UuidUtil.toUuid(_)).toSet)
    }).collect().toMap

  }

  override def getAll(): List[(UUID, Set[UUID])] = {
    cache.toList
  }
}