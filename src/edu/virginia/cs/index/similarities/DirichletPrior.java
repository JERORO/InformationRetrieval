package edu.virginia.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.LMSimilarity;

public class DirichletPrior extends LMSimilarity {

    private LMSimilarity.DefaultCollectionModel model; // this would be your reference model
    private float queryLength = 0; // will be set at query time automatically
    private float totalCount = 0;

    public DirichletPrior() {
        model = new LMSimilarity.DefaultCollectionModel();
    }

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

//        totalCount += 1;
//        System.out.println("totalCount: " + totalCount);

        double miu = 20; //default 2500; [2000, 3000]
        float cwd = termFreq;
        float n = docLength;
        float alphaD = (float) (miu/(miu+n));
        float pwc = model.computeProbability(stats);
        float pswd = (float) ((cwd+miu*pwc)/(n+miu));

        float ans = (float) (Math.log10((pswd)/(alphaD*pwc))+  Math.log10(alphaD));

        return ans;

//        float mu = (float) 19.5;
//        float score = (float) (Math.log((cwd+ miu*pwc)/ ((n+miu)*alphaD*pwc)) +Math.log(alphaD));
//        return score;

    }

    @Override
    public String getName() {
        return "Dirichlet Prior";
    }

    @Override
    public String toString() {
        return getName();
    }

    public void setQueryLength(float length) {
        queryLength = length;
    }
}
