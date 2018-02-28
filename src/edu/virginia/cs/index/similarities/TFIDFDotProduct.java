package edu.virginia.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

public class TFIDFDotProduct extends SimilarityBase {
    /**
     * Returns a score for a single term in the document.
     *
     * @param stats
     *            Provides access to corpus-level statistics
     * @param termFreq
     * @param docLength
     */


//    AvgFieldLength()23.896053
//    DocFreq640
//    NumberOfDocuments11429
//    NumberOfFieldTokens273108
//    TotalBoost1.0
//    TotalTermFreq1045
//    ValueForNormalization1.0
    @Override
    protected float score(BasicStats stats, float termFreq, float docLength) {

        float tfidf = (float) ((1 + Math.log10(termFreq)) *
                Math.log10((1+stats.getNumberOfDocuments())/stats.getDocFreq()));


        return tfidf;
    }

    @Override
    public String toString() {
        return "TF-IDF Dot Product";
    }
}
