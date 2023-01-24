package org.hatke.queryfingerprint.index.search

import com.sksamuel.elastic4s.handlers.searches.queries.QueryBuilderFn
import com.sksamuel.elastic4s.requests.searches.queries.{Query, RawQuery}
import com.sksamuel.elastic4s.requests.searches.queries.compound.BoolQuery
import com.sksamuel.elastic4s.requests.searches.term.TermQuery
import org.hatke.queryfingerprint.index.TestQueryFingerPrint
import org.hatke.queryfingerprint.json.JsonUtils

object FirstSearchDesign {

  private def scriptScoreQuery(q : Query,
                               searchFV : Array[Int]) : Query = {

    val qStr = QueryBuilderFn(q).string
    val fV = JsonUtils.asJson(searchFV)

    val scriptQ = s"""
       |{
       |    "script_score": {
       |      "query" : ${qStr},
       |      "script": {
       |        "source": "cosineSimilarity(params.featureVector, 'featureVector') * _score",
       |        "params": {
       |          "featureVector": ${fV}
       |        }
       |      }
       |    }
       |}""".stripMargin

    RawQuery(scriptQ)
  }

}

class FirstSearchDesign extends SearchBuilder {

  private var searchQFP: TestQueryFingerPrint = null

  def searchQFP(qfp: TestQueryFingerPrint): SearchBuilder = {
    this.searchQFP = qfp
    this
  }

  /**
   *  - Add [[BoolQuery]] for {{{joins, groupBys, predicates, orderBy, tablesReferenced}}}
   *  - Each [[BoolQuery]] is a list of `should` [[TermQuery]].
   *  - Boosting Logic
   *    - If there are more than 5 joins boost the Join Query
   *    - If there are more than 5 gBys or 5 agg FuncCalls boost the Join Query
   *    - If neither gBy or join i boosted
   *      - If there are more than 3 more join features than gBys than boost the join Clause
   *      - If there are more than 3 more gBy features(or aggFuncCalls) than joins than boost the gBy Clause
   *    - if neither gBy or join are not boosted and there are more than 5 predicates boost the predicate Clause
   *    - if neither gBy or join or predicates are not boosted and there are more than 5 orderBys boost the orderBy Clause
   *  - Combine into a [[BoolQuery]]
   *    - any boosted Clause Query add as a `must` child
   *    - others add as a `should` child.
   */
  class QB(boost : Double,
           manyThreshold : Int = 5) {

    def minShouldMatch(featureSz : Int) : Int = {
      featureSz match {
        case v if v < 2 => 0
        case 3 => 1
        case v if v < 6 => 2
        case _ => 3
      }
    }

    lazy val numJoins = searchQFP.joins.size
    lazy val numGBys = searchQFP.groupedColumns.size
    lazy val numAggCals = aggFuncTerms.size
    lazy val numPredicates = searchQFP.predicates.size
    lazy val numOrderBys = searchQFP.orderedColumns.size

    lazy val (joinBoost : Boolean, gByBoost : Boolean, predicateBoost : Boolean, orderByBoost) = {
      var jB, gbB, pB, oB : Boolean = false

      val manyJoins = numJoins >= manyThreshold
      val manyGBys = numGBys >= manyThreshold
      val manyAggCals = numAggCals > manyThreshold
      val manyPreds = numPredicates > manyThreshold
      val manyOrderBys = numOrderBys > manyThreshold

      jB = manyJoins
      gbB = manyGBys || manyAggCals

      if (!jB && !gbB) {
        if (numJoins - numGBys > 3) {
          jB = true
        } else if ( numGBys - numJoins > 3 || numAggCals - numJoins > 3) {
          gbB = true
        }
      }

      if (!jB && !gbB && manyPreds) {
        pB = true
      }

      if (!jB && !gbB & !pB && manyOrderBys) {
        oB = true
      }


      (jB, gbB, pB, oB)
    }


    lazy val predicateSearchTerms = for (j <- searchQFP.predicates) yield {
      j.searchQuery
    }

    lazy val predicatesMinShouldMatch : Int = if (predicateBoost) minShouldMatch(searchQFP.joins.size) else 0

    lazy val joinSearchTerms = for (j <- searchQFP.joins) yield {
      j.searchQuery
    }

    lazy val joinMinShouldMatch = minShouldMatch(searchQFP.joins.size)

    lazy val (scalarFuncTerms : Set[Query], aggFuncTerms : Set[Query]) = {
      var scalarTerms = Set.empty[Query]
      var aggTerms = Set.empty[Query]

      for (fA <- searchQFP.functionApplications) {
        val (s, a) = fA.searchQuery

        scalarTerms = scalarTerms ++ s.toSeq
        aggTerms = aggTerms ++ a.toSeq
      }

      (scalarTerms, aggTerms)
    }


    private def joinClause : Option[Query] = {

      if (joinSearchTerms.nonEmpty) {
        var bq = BoolQuery().
          should(joinSearchTerms).
          minimumShouldMatch(joinMinShouldMatch)

        if (joinBoost) {
          bq = bq.boost(boost)
        }

        Some(bq)
      } else None
    }

    private def predicateClause : Option[Query] = {
      if (predicateSearchTerms.nonEmpty) {
        var bq = BoolQuery().
          should(predicateSearchTerms).
          minimumShouldMatch(predicatesMinShouldMatch)

        if (predicateBoost) {
          bq = bq.boost(boost)
        }
        Some(bq)
      } else None
    }

    private def scalarFunClause: Option[Query] = {
      if (scalarFuncTerms.nonEmpty) {
        var bq = BoolQuery().
          should(scalarFuncTerms)

        Some(bq)
      } else None
    }


    private def gByClause: Option[Query] = {
      if (aggFuncTerms.nonEmpty || searchQFP.groupedColumns.nonEmpty) {
        var bq = BoolQuery()

        bq.should(searchQFP.searchTermsForGroupCols ++ aggFuncTerms.toSeq)

        if (gByBoost) {
          bq = bq.boost(boost)
        }

        Some(bq)
      } else None
    }

    private def orderByClause: Option[Query] = {
      if (searchQFP.orderedColumns.nonEmpty) {
        var bq = BoolQuery()

        bq.should(searchQFP.searchTermsForOrderCols)

        if (gByBoost) {
          bq = bq.boost(boost)
        }

        Some(bq)
      } else None
    }



    def build: Query = {

      var mustQueries = Seq.empty[Query]
      var shouldQueries = Seq.empty[Query]

      if (joinBoost) {
        mustQueries = mustQueries ++ joinClause.toSeq
      } else {
        shouldQueries = shouldQueries ++ joinClause.toSeq
      }

      if (gByBoost) {
        mustQueries = mustQueries ++ gByClause.toSeq
      } else {
        shouldQueries = shouldQueries ++ gByClause.toSeq
      }

      if (predicateBoost) {
        mustQueries = mustQueries ++ predicateClause.toSeq
      } else {
        shouldQueries = shouldQueries ++ predicateClause.toSeq
      }

      if (orderByBoost) {
        mustQueries = mustQueries ++ orderByClause.toSeq
      } else {
        shouldQueries = shouldQueries ++ orderByClause.toSeq
      }

      shouldQueries = shouldQueries ++ scalarFunClause.toSeq ++ Seq(BoolQuery().should(searchQFP.searchTermsForTables))

      val q = BoolQuery().must(mustQueries).should(shouldQueries)

      FirstSearchDesign.scriptScoreQuery(q, searchQFP.featureVector)
    }
  }


  override def build: Query = {
    new QB(2.0).build
  }
}
