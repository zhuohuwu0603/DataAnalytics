package grant.analytics.common.sectiontrees.cassandra

import java.net.InetAddress
import java.util.{Properties, UUID}

import com.datastax.spark.connector.cql.{CassandraConnector, CassandraConnectorConf, NoAuthConf}
import com.google.common.cache.{Cache, CacheBuilder}
import grant.analytics.common.sectiontrees.SectionTrees

import scala.collection.convert.WrapAsScala
import scala.collection.mutable

/**
  * Created by grant on 2017-01-03.
  */
class SectionTreesFromCassandra(private val stConfig:Properties) extends SectionTrees{
  private lazy val connector = createCassandraConnector()

  private val section_tree_cache: Cache[UUID, mutable.HashSet[UUID]] = CacheBuilder.newBuilder().build[UUID, mutable.HashSet[UUID]]()


  private def createCassandraConnector(): CassandraConnector = {
    CassandraConnector(Set(InetAddress.getByName(stConfig.getProperty("contactpoints"))),
      CassandraConnectorConf.ConnectionPortParam.default,
      NoAuthConf,
      None,
      stConfig.getProperty("connector.keepalive_ms", "10000").toInt)
  }

  override def getSectionAncestors(section: UUID): List[UUID] = {
    var ancestors = section_tree_cache.getIfPresent(section)

    if(ancestors == null){
      ancestors = new mutable.HashSet[UUID]()
      ancestors += section
      connector.withSessionDo(session => {

        val cql = s"SELECT ancestor_uuid from ${stConfig.getProperty("keyspace")}.${stConfig.getProperty("tables.sectiontrees")} where row_ancestor_type ='' and row_ancestor_uuid= 00000000-0000-0000-0000-000000000000 and row_descendant_type = '' and row_descendant_uuid = ${section};"

        val ret = session.execute(cql)

        WrapAsScala.asScalaIterator(ret.iterator()).foreach(row => {
          ancestors += row.getUUID("ancestor_uuid")
        })

        section_tree_cache.put(section, ancestors)

      })//end connector.withSessionDo
    }//end parents == null

    ancestors.toList
  }//end getOrPutIfAbsent
  override def getAll(): List[(UUID, Set[UUID])] = {
    WrapAsScala.mapAsScalaConcurrentMap( section_tree_cache.asMap() ).toList.map(tuple => {
      (tuple._1,
        tuple._2.toSet
      )
    })
  }
}
