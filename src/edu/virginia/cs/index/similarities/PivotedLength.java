package edu.virginia.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

public class PivotedLength extends SimilarityBase {
    /**
     * Returns a score for a single term in the document.
     *
     * @param stats
     *            Provides access to corpus-level statistics
     * @param termFreq
     * @param docLength
     */
    @Override
    protected float score(BasicStats stats, float termFreq, float docLength) {
        double s = 0.75;

        long N = stats.getNumberOfDocuments();
        long df = stats.getDocFreq();
        float cwd = termFreq;
        float cwq = 1; //assume that the query term frequency is always one
        float n = docLength;
        float navg = stats.getAvgFieldLength();

        double p1 = (1+Math.log(1+Math.log(cwd)))/(1-s+s*n/navg);
        float ans = (float) (p1 * cwq * Math.log((N+1)/df));

        return ans;
    }

    @Override
    public String toString() {
        return "Pivoted Length Normalization";
    }

}
