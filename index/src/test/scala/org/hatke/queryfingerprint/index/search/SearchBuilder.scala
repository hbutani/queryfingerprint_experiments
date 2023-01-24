package org.hatke.queryfingerprint.index.search

import com.sksamuel.elastic4s.requests.searches.queries.Query
import org.hatke.queryfingerprint.index.{TestJoin, TestQueryFingerPrint}

trait SearchBuilder {
  def searchQFP(qfp: TestQueryFingerPrint) : SearchBuilder

  def build : Query
}

