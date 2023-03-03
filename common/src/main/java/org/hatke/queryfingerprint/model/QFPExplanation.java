package org.hatke.queryfingerprint.model;

/**
 * For QueryFingeprints that are found through Search we associate an explanation
 * Example explanations are:
 *
 *  - If the QFP is found through an Elasticsearch, the Explanation is the underlying Search's explanation.
 *  - When constructing a QFP by aggregating constituent QFPs the explanation is the decisions made in
 *    merging features from the indidivual QFPs.
 */
public interface QFPExplanation {

    String show();
}
