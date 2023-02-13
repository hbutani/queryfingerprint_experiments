package org.hatke.queryfingerprint.search

import com.sksamuel.elastic4s.requests.searches.queries.Query
import org.hatke.queryfingerprint.model.{Queryfingerprint => QFP}

trait SearchBuilder {

  def searchQFP(qfp: QFP): SearchBuilder

  def build: Query

}
