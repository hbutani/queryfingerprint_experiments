package org.hatke.queryfingerprint.search
import com.sksamuel.elastic4s.handlers.searches.queries.QueryBuilderFn
import com.sksamuel.elastic4s.requests.searches.queries.{Query, RawQuery}
import com.sksamuel.elastic4s.requests.searches.queries.compound.BoolQuery
import com.sksamuel.elastic4s.requests.searches.term.TermQuery
import org.hatke.queryfingerprint.index.IndexableElem.{FuncAppIdxElem, JoinIdxElem, PredicateIdxElem}
import org.hatke.queryfingerprint.model.{Queryfingerprint => QFP}
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

  import scala.jdk.CollectionConverters._

  private var searchQFP: QFP = null

  def searchQFP(qfp: QFP): SearchBuilder = {
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

    lazy val numJoins = searchQFP.getJoins.size
    lazy val numGBys = searchQFP.getGroupedColumns.size
    lazy val numAggCals = aggFuncTerms.size
    lazy val numPredicates = searchQFP.getPredicates.size
    lazy val numOrderBys = searchQFP.getOrderedColumns.size

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


    lazy val predicateSearchTerms = for (j <- searchQFP.getPredicates.asScala) yield {
      j.searchQuery
    }

    lazy val predicatesMinShouldMatch : Int = if (predicateBoost) minShouldMatch(searchQFP.getJoins.size) else 0

    lazy val joinSearchTerms = for (j <- searchQFP.getJoins.asScala) yield {
      j.searchQuery
    }

    lazy val joinMinShouldMatch = minShouldMatch(searchQFP.getJoins.size)

    lazy val (scalarFuncTerms : Set[Query], aggFuncTerms : Set[Query]) = {
      var scalarTerms = Set.empty[Query]
      var aggTerms = Set.empty[Query]

      for (fA <- searchQFP.getFunctionApplications.asScala) {
        val q = fA.searchQuery

        if (fA.isAggregate) {
          aggTerms = aggTerms + q
        } else {
          scalarTerms = scalarTerms + q
        }
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
      if (aggFuncTerms.nonEmpty || !searchQFP.getGroupedColumns.isEmpty) {
        var bq = BoolQuery()

        bq.should(searchTermsForGroupCols(searchQFP) ++ aggFuncTerms.toSeq)

        if (gByBoost) {
          bq = bq.boost(boost)
        }

        Some(bq)
      } else None
    }

    private def orderByClause: Option[Query] = {
      if (!searchQFP.getOrderedColumns.isEmpty) {
        var bq = BoolQuery()

        bq.should(searchTermsForOrderCols(searchQFP))

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

      shouldQueries = shouldQueries ++ scalarFunClause.toSeq ++ Seq(BoolQuery().should(searchTermsForTables(searchQFP)))

      val q = BoolQuery().must(mustQueries).should(shouldQueries)

      FirstSearchDesign.scriptScoreQuery(q, searchQFP.getFeatureVector)
    }
  }


  override def build: Query = {
    new QB(2.0).build
  }
}