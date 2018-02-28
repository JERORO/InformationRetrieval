package edu.virginia.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

public class OkapiBM25 extends SimilarityBase {
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
        float k1= (float) 1.2;  //default 1.5; [1.2, 2]
        float k2= 750;  //default 750; (0,1000]
        float b = (float) 0.75;  //default 1.0; [0.75, 1.2]

        long N = stats.getNumberOfDocuments();
        long df = stats.getDocFreq();
        float cwd = termFreq;
        float cwq = 1; //assume that the query term frequency is always one
        float n = docLength;
        float navg = stats.getAvgFieldLength();

        double p1 = Math.log((N-df+0.5)/(df+0.5));
        float p2 = (k1+1)*cwd/(k1*(1-b+b*n/navg)+cwd);
        float p3 = (k2+1)*cwq/(k2+cwq);

        float bm25 = (float) (p1*p2*p3);
        float r = bm25 * cwq * cwd/(b/(navg*n+b+cwd));
        return r;
    }

    @Override
    public String toString() {
        return "Okapi BM25";
    }

}
