package org.hatke.queryfingerprint.queryhistory

import com.sksamuel.elastic4s.requests.explain.Explanation
import org.hatke.queryfingerprint.model.QFPExplanation

case class ElasticSearchExplanation(elasticExp : Explanation) extends QFPExplanation {

  private def buildElasticExplainString(eExp : Explanation,
                                        depth : Int = 0,
                                        bldr : StringBuilder) : Unit = {
    bldr.append(s"${" " * (depth*2)}- ${eExp.description.replace('\n', ' ')}\n")
    eExp.details.foreach(e => buildElasticExplainString(e, depth + 1, bldr))
  }

  override def show(): String = {
    val bldr = new StringBuilder()
    buildElasticExplainString(elasticExp, 0, bldr)
    s"""from Elastic Search:
       |${bldr.toString}""".stripMargin
  }
}
