package edu.virginia.cs.evaluator;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import edu.virginia.cs.index.similarities.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;

import edu.virginia.cs.index.ResultDoc;
import edu.virginia.cs.index.Searcher;

import static java.lang.Math.log;



public class Evaluate {
	/**
	 * Format for judgements.txt is:
	 * 
	 * line 0: <query 1 text> line 1: <space-delimited list of relevant URLs>
	 * line 2: <query 2 text> line 3: <space-delimited list of relevant URLs>
	 * ...
	 * Please keep all these constants!
	 */

	Searcher _searcher = null;
	ArrayList<Double> MAP_list = new ArrayList<>();
	ArrayList<Double> P10_list = new ArrayList<>();
	ArrayList<Double> MRR_list = new ArrayList<>();
	ArrayList<Double> NDCG_list = new ArrayList<>();

//	Writer writer = new FileWriter("TFIDFMAP.csv");

	public static void setSimilarity(Searcher searcher, String method) {
		if(method == null)
			return;
		else if(method.equals("--dp"))
			searcher.setSimilarity(new DirichletPrior());
		else if(method.equals("--jm"))
			searcher.setSimilarity(new JelinekMercer());
		else if(method.equals("--ok"))
			searcher.setSimilarity(new OkapiBM25());
		else if(method.equals("--pl"))
			searcher.setSimilarity(new PivotedLength());
		else if(method.equals("--tfidf"))
			searcher.setSimilarity(new TFIDFDotProduct());
		else if(method.equals("--bdp"))
			searcher.setSimilarity(new BooleanDotProduct());
		else
		{
			System.out.println("[Error]Unknown retrieval function specified!");
			printUsage();
			System.exit(1);
		}
	}

	public static void printUsage() {
		System.out.println("To specify a ranking function, make your last argument one of the following:");
		System.out.println("\t--dp\tDirichlet Prior");
		System.out.println("\t--jm\tJelinek-Mercer");
		System.out.println("\t--ok\tOkapi BM25");
		System.out.println("\t--pl\tPivoted Length Normalization");
		System.out.println("\t--tfidf\tTFIDF Dot Product");
		System.out.println("\t--bdp\tBoolean Dot Product");
	}

	//Please implement P@K, MRR and NDCG accordingly
	public void evaluate(String method, String indexPath, String judgeFile) throws IOException {		
		_searcher = new Searcher(indexPath);		
		setSimilarity(_searcher, method);
		
		BufferedReader br = new BufferedReader(new FileReader(judgeFile));
		String line = null, judgement = null;
		int k = 10;
		double meanAvgPrec = 0.0, p_k = 0.0, mRR = 0.0, nDCG = 0.0;
		double numQueries = 0.0;

		while ((line = br.readLine()) != null) {
			//System.out.println("line: " + line);

			judgement = br.readLine();
			//System.out.println("judgeent: " + judgement);
			
			//compute corresponding AP
			meanAvgPrec += AvgPrec(line, judgement);
			//compute corresponding P@K
			p_k += Prec(line, judgement, k);
			//compute corresponding MRR
			mRR += RR(line, judgement);
			//compute corresponding NDCG
			nDCG += NDCG(line, judgement, k);
			
			++numQueries;
		}
		br.close();

		System.out.println("\nMAP: " + meanAvgPrec / numQueries);//this is the final MAP performance of your selected ranker
		System.out.println("\nP@" + k + ": " + p_k / numQueries);//this is the final P@K performance of your selected ranker
		System.out.println("\nMRR: " + mRR / numQueries);//this is the final MRR performance of your selected ranker
		System.out.println("\nNDCG: " + nDCG / numQueries); //this is the final NDCG performance of your selected ranker
//
//		System.out.println("MAP_list: "+MAP_list);
//		System.out.println("P@10_list: "+P10_list);
//		System.out.println("MRR_list: "+MRR_list);
//		System.out.println("NDCG_list: "+NDCG_list);
//
//		Writer writer = new FileWriter("dp.csv");
//		for (double d : MAP_list) {
//			writer.append(Double.toString(d))
//					.append("\r\n");
//		}
//		writer.close();
//
//		Writer writer1 = new FileWriter("TFIDFP10.csv");
//		for (double d : P10_list) {
//			writer1.append(Double.toString(d))
//					.append("\r\n");
//		}
//		writer1.close();
//
//		Writer writer2 = new FileWriter("TFIDFMRR.csv");
//		for (double d : MRR_list) {
//			writer2.append(Double.toString(d))
//					.append("\r\n");
//		}
//		writer2.close();
//
//		Writer writer3 = new FileWriter("TFIDFNDCG.csv");
//		for (double d : NDCG_list) {
//			writer3.append(Double.toString(d))
//					.append("\r\n");
//		}
//		writer3.close();

	}

	//new
	double AvgPrec(String query, String docString) throws IOException {
		ArrayList<ResultDoc> results = _searcher.search(query).getDocs();

		System.out.println("query: "+query);

		System.out.println("Result Size:"+results.size());
		if (results.size() == 0)
			return 0; // no result returned

		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.split(" ")));
		int i = 1;
		double avgp = 0.0;
		double numRel = 0;
		for (ResultDoc rdoc : results) {
			if (relDocs.contains(rdoc.title())) {
				numRel ++;
				avgp += numRel/i;
				System.out.print("  ");
			} else {
				System.out.print("X ");
			}
			System.out.println(i + ". " + rdoc.title());
			++i;
		}

		//compute average precision here
		if (numRel==0){
			avgp = 0;
		}else{
			avgp = avgp/relDocs.size();
		}

		MAP_list.add(avgp);
		System.out.println("Average Precision: " + avgp);
		return avgp;
	}

	double Prec(String query, String docString, int k) {
		double p_k = 0;
		ArrayList<ResultDoc> results = _searcher.search(query).getDocs();
		if (results.size() == 0)
			return 0; // no result returned

		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.split(" ")));
		double numRel = 0;
		for (int i = 0; i < results.size() && i < k; i++) {
			ResultDoc rdoc = results.get(i);
			if (relDocs.contains(rdoc.title())) {
				numRel++;
			}
		}
		p_k = numRel/k;
		System.out.println("Precision@k: " + p_k);
		return p_k;
	}

	double RR(String query, String docString) {
		ArrayList<ResultDoc> results = _searcher.search(query).getDocs();
		if (results.size() == 0)
			return 0; // no result returned

		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.split(" ")));
		for (int i = 0; i < results.size(); i++) {
			ResultDoc rdoc = results.get(i);
			if (relDocs.contains(rdoc.title())) {
				return 1.0/(i+1);
			}
		}
		return 0;
	}

	double NDCG(String query, String docString, int k) {
		double dcg = 0;
		double idcg = 0;
		ArrayList<ResultDoc> results = _searcher.search(query).getDocs();
		if (results.size() == 0)
			return 0; // no result returned

		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.split(" ")));
		double numRel = 0;
		for (int i = 0; i < results.size() && i < k; i++) {
			ResultDoc rdoc = results.get(i);
			if (relDocs.contains(rdoc.title())) {
				numRel++;
				dcg += 1.0/(Math.log(i+2)/Math.log(2));
				System.out.print("  ");
			} else {
				System.out.print("X ");
			}
			System.out.println(i+1 + ". " + rdoc.title());
		}

		for (int i = 0; i < k && i< relDocs.size(); i++){
			idcg += 1.0/(Math.log(i+2)/Math.log(2));
		}
		if (idcg == 0){
			return 0;
		}
		return dcg/idcg;
	}

}