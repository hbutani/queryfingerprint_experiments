package org.hatke.queryfingerprint.index

import com.sksamuel.elastic4s.ElasticClient

package object search {

  def search(sQFP : TestQueryFingerPrint,
             sb : SearchBuilder,
             _explain : Boolean
            )(client : ElasticClient) : IndexedSeq[TestQueryFingerPrint] = {
    import com.sksamuel.elastic4s.ElasticDsl
    import com.sksamuel.elastic4s.ElasticDsl._

    val r = client.execute {
      val q = sb.searchQFP(sQFP).build
      ElasticDsl.search("query_fingerprint").query(q).explain(_explain)
    }.await.result

    r.to[TestQueryFingerPrint]

  }

  val sampleQ1 = TestQueryFingerPrint(
    "test_1",
    Set("TPCDS.STORE_RETURNS"),
    Set(),
    Set(),
    Set(
        TestPredicate(column = "TPCDS.DATE_DIM.D_YEAR", operator = "equals", functionApplication = None)
    ),
    Set(
      TestJoin(leftTable = "TPCDS.STORE_RETURNS", rightTable = "TPCDS.STORE_RETURNS",
        leftColumn = "TPCDS.STORE_RETURNS.SR_STORE_SK",
        rightColumn = "TPCDS.STORE_RETURNS.SR_STORE_SK", joinType = TestJoinType.inner)
    )
  )

}
