package org.hatke.queryfingerprint.queryhistory

import com.sksamuel.elastic4s.requests.searches.queries.Query
import com.sksamuel.elastic4s.requests.searches.queries.compound.BoolQuery
import com.sksamuel.elastic4s.requests.searches.term.TermQuery
import org.hatke.queryfingerprint.model.{FunctionApplication, Join, Predicate}

trait IndexableElem[T] {

  def elem : T

  def indexElements: Seq[String]

  def searchQuery: Query

}

object IndexableElem {

  import Utils._


  implicit def PredicateIdxElem(p : Predicate) : IndexableElem[Predicate] = new IndexableElem[Predicate] {
    override def elem: Predicate = p

    override def indexElements: Seq[String] = {
      Seq(p.getColumn,
        s"${p.getColumn} ${p.getOperator}"
      ) ++ asScala(p.getFunctionName).map(f => s"${p.getColumn} ${p.getOperator} ${f}").toSeq
    }

    def searchQuery: Query = {
      val termQs = for (iElem <- indexElements) yield {
        TermQuery("predicates", iElem)
      }

      BoolQuery().should(termQs)
    }
  }

  implicit def JoinIdxElem(j : Join) : IndexableElem[Join] = new IndexableElem[Join] {
    override def elem: Join = j

    override def indexElements: Seq[String] = {
      Seq(
        s"${j.getLeftTable} ${j.getRightTable}",
        s"${j.getLeftTable} ${j.getRightTable} ${j.getType}",
        s"${j.getLeftTable} ${j.getRightTable} ${j.getType} ${j.getLeftColumn} ${j.getRightColumn}"
      )
    }

    override def searchQuery: Query = {
      val termQs = for (iElem <- indexElements) yield {
        TermQuery("joins", iElem)
      }

      BoolQuery().should(termQs)
    }
  }

  implicit def FuncAppIdxElem(fA : FunctionApplication) = new IndexableElem[FunctionApplication] {
    override def elem: FunctionApplication = fA

    override def indexElements: Seq[String] = {
      Seq(s"${fA.getFunctionName} ${fA.getColumn }")
    }

    override def searchQuery: Query = {

      val termQs = for (iElem <- indexElements) yield {
        TermQuery("functionApplications", iElem)
      }

      BoolQuery().should(termQs)

    }
  }
}
