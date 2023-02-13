package org.hatke.queryfingerprint

import com.sksamuel.elastic4s.ElasticClient
import org.hatke.queryfingerprint.model.{Queryfingerprint => QFP}
import com.sksamuel.elastic4s.requests.searches.term.TermQuery

package object search {
  import org.hatke.queryfingerprint.index.QueryFingerprint._
  import scala.jdk.CollectionConverters._

  def search(sQFP: QFP,
             sb: SearchBuilder,
             _explain: Boolean
            )(client: ElasticClient): IndexedSeq[QFP] = {
    import com.sksamuel.elastic4s.ElasticDsl
    import com.sksamuel.elastic4s.ElasticDsl._

    val r = client.execute {
      val q = sb.searchQFP(sQFP).build
      ElasticDsl.search("query_fingerprint").query(q).explain(_explain)
    }.await.result

    r.to[QFP]

  }


  def searchTermsForTables(qfp : QFP) : Set[TermQuery] =
    qfp.getTablesReferenced.asScala.map(g => TermQuery("tablesReferenced", g)).toSet

  def searchTermsForGroupCols(qfp : QFP) : Set[TermQuery] =
    qfp.getGroupedColumns.asScala.map(g => TermQuery("groupedColumns", g)).toSet

  def searchTermsForOrderCols(qfp : QFP): Set[TermQuery] =
    qfp.getOrderedColumns.asScala.map(g => TermQuery("orderedColumns", g)).toSet


}
