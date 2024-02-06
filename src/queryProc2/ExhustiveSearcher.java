package queryProc2;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.io.PrintWriter;

import nkmap.bdb.RelSourceFirst;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import util.TargetList;
import util.AnswerTree;
import util.AnswerTreeSet;
import util.AnswerTreeSetComparator;
import util.ListEntry;
import util.Params;

import queryProcessing.DivTopKSearcher;

public class ExhustiveSearcher extends DivTopKSearcher {
	
	PriorityQueue<AnswerTreeSet>[] Q;
	PriorityQueue<AnswerTreeSet> Qtemp;
    
	int numOfAnswerTreeSetsCreated;
	int numOfAnswerTreeSetsRemoved;

	public ExhustiveSearcher() {
		super();
		init();
	}
	
	public ExhustiveSearcher(DirectedGraph<Integer, DefaultEdge> g) {
		super(g);
		init();
	}

	private void init() {
		Qtemp = new PriorityQueue<AnswerTreeSet>(32, new AnswerTreeSetComparator()); 			// worstScore의 내림차순으로 최대 k개 저장; root node == min-k node
	}

	@Override
	@SuppressWarnings("unchecked")
	protected boolean prepareSearch(List<String> query) {
		if (super.prepareSearch(query) == false)
			return false;
		
		Q = new PriorityQueue[Params.K];
		for (int i = 0; i < Params.K; i++) { 
			// Qi : exactly i+1개의 answer tree를 포함하는 answer set S들을 rel(S, q)의 역순으로 저장
			Q[i] = new PriorityQueue<AnswerTreeSet>(32, new AnswerTreeSetComparator()); 			
		}
		numOfAnswerTreeSetsCreated = 0;
		numOfAnswerTreeSetsRemoved = 0;
//	    numOfNKMapLookups2 = 0;
//       numOfNKMapLookupsByKeyword = maxNKMap = minNKMap = 0;
//		DCG = 0.0; 	DCG2 = 0.0; 	
		return true;
	}
	
	@Override
	public void completeSearch() {
		for (int i = 0; i < Params.K; i++) Q[i].clear();
		super.completeSearch();
	}

	//////////////////////////////////////////////////////////
	// queryProc2 feature: using new dissimilarity condition 
    private boolean checkDissCondition_float(List<AnswerTree> S, AnswerTree t) {
	    double sumDissOfSAndt = 0.0;
        for (int i = 0; i < S.size(); i++) {
            sumDissOfSAndt += computeDissimilarityByDSC(S.get(i).srcNodes, t.srcNodes);
        } 
        if (sumDissOfSAndt / S.size() >= Params.tau) 
            return true;
        return false;
    }
	//////////////////////////////////////////////////////////

	public void search(List<String> query) {
		
		if (prepareSearch(query) == false) return; // null;
		
		AnswerTree t = null;
		AnswerTreeSet S = null;
		AnswerTreeSet optAnswerSet = null;		
		int numOfATS = 0;
		
		while ((t = nextTopAnswer(query)) != null) {
	
			for (int i = Params.K-2; i >= 0; i--) {
				Qtemp.clear();				// already empty!	
				while ((S = (AnswerTreeSet)Q[i].poll()) != null) {
					numOfATS--;

					if (optAnswerSet != null &&  (S.score + (Params.K-S.answer.size())*t.score) <= optAnswerSet.score ) {
						numOfAnswerTreeSetsRemoved++; 
						break;
					}
					
					//////////////////////////////////////////////////////////
					// queryProc2 feature: using new dissimilarity condition 
					if (checkDissCondition_float(S.answer, t) == true) {
						AnswerTreeSet T = new AnswerTreeSet(S.answer, t, S.score + t.score, 0);
						Q[i+1].add(T);
						numOfAnswerTreeSetsCreated++;
						numOfATS++;
						if (numOfATS > maxQSize) maxQSize = numOfATS;
//						System.out.println("New " + T + " into Q" + (i+1));
					}				
					else numOfATSPrunnedByDissCond++;
					Qtemp.add(S);
				}
				numOfAnswerTreeSetsRemoved += Q[i].size();
				numOfATS -= Q[i].size();
				Q[i].clear();

				Q[i].addAll(Qtemp);
				numOfATS += Qtemp.size();
				Qtemp.clear();
				if (numOfATS > maxQSize) maxQSize = numOfATS;
			}
			if (optAnswerSet == null) {
				AnswerTreeSet T = new AnswerTreeSet(t);
				Q[0].add(T);
				numOfATS++;
				if (numOfATS > maxQSize) maxQSize = numOfATS;

				numOfAnswerTreeSetsCreated++;
//				System.out.println("New " + T + " into Q0");
			}
			if (Q[Params.K-1].isEmpty() == false) {
				optAnswerSet = (AnswerTreeSet)Q[Params.K-1].peek();
				numOfLocalAnswersFound++;
				logger.debug("a new local answer found: {}", optAnswerSet);

				numOfATS -= Q[Params.K-1].size();
				numOfAnswerTreeSetsRemoved += Q[Params.K-1].size();
				Q[Params.K-1].clear();

				/*
				for (int i = 0; i < Params.K-1; i++) {
					Iterator<AnswerTreeSet> it = Q[i].iterator();
					while (it.hasNext()) {
						S = it.next();
						if (optAnswerSet.answer.containsAll(S.answer) == true) {
							it.remove(); // Q[i].remove(S);
							numOfAnswerTreeSetsRemoved++;
							numOfATS--;
							logger.debug("Removed: {}", S);
						}
					}
				}
				*/
			}
			if (optAnswerSet != null) {
				if (canTerminate(optAnswerSet, Q, t) == true) {
					getResult(optAnswerSet.answer, 0); 
					return;
				}
			}
		}
		logger.debug("nextTopAnswer is null!!");
		if (optAnswerSet != null) {
			getResult(optAnswerSet.answer, 0);
		}
	}
	
	private boolean canTerminate(AnswerTreeSet answerSet, PriorityQueue<AnswerTreeSet>[] Q, AnswerTree t) {
		for (int i = 0; i < Params.K-1; i++) {
			if (Q[i].peek() != null &&
			    answerSet.score < (Q[i].peek().score + (Params.K-i-1)*t.score)) {
				return false;
			}
		}
		logger.debug("Termination condition is met by {}",  answerSet);
		return true;		
	}

	public void printResult(PrintWriter pw, List<String> q) { //, List<AnswerTree> ans) {
	
			pw.println(q);
			
			if (optimalAnswer == null) {
				pw.println("div top-k answers for q do not exist in G!");
				return;
			}
	/*
			//double DCG = 0.0d, IDCG = 0.0d;
			double logval = 0.0d;
			double[] ResultRelevs_sorted = Arrays.copyOf(ResultRelevs, ResultRelevs.length);
			Arrays.sort(ResultRelevs_sorted);
	*/			
			for (int i = 0; i < optimalAnswer.size(); i++) {
				int u = optimalAnswer.get(i).rootNodeId;
				int[] srcNodes = optimalAnswer.get(i).srcNodes;
				pw.println("srcNodes: " + Arrays.toString(srcNodes));
				printAnswerTree(u, srcNodes, pw);
				
//				SumResultRelev += ResultRelevs[i];
/*
				if (printAnswerTree(u, srcNodes, pw) == true) {
	                // reduced answer!
					SumReducedResultRelev += ResultRelevs[i];
			                reducedNodes.add(u);
	            		}
				else
					numOfNonReducedAnswers++;			
	
				// to find NDCG@K
				logval = Math.log(i+1.0+1.0)/Math.log(2.0);
				DCG += (ResultRelevs[i]/logval);
				DCG2 += ((Math.pow(2, ResultRelevs[i]/ResultRelevs[0])-1.0)/logval);
			//	IDCG += (ResultRelevs_sorted[ResultDestNodes.length-i-1]/logval);
*/
			}
//			DCG = DCG/ResultRelevs[0];
			//nDCG = DCG / IDCG;

	//////////////////////////////////////////////////////////
			pw.println("Dissims of Subsets and t:");
   	     	for (int i = 1; i < optimalAnswer.size(); i++) {
		    	double sumDiss = 0.0;
	        	for (int j = 0; j < i; j++) 
	            	sumDiss += computeDissimilarityByDSC(optimalAnswer.get(j).srcNodes, optimalAnswer.get(i).srcNodes);
				pw.println("(0~" + (i-1) + ", " + i + ") : " + sumDiss/i);
			}
	//////////////////////////////////////////////////////////
	
			pw.println("-----------------------------------------");
			pw.println("ResultDestNodes: " + Arrays.toString(ResultDestNodes));
			pw.println("ResultRelevs: " + Arrays.toString(ResultRelevs));
			pw.println("AvgResultRelev: " + avgResultRelev);
			pw.println("AvgResultDissimilarity: " + avgResultDiss);
			pw.println("Num of entries retrieved from the KNLists: " + numOfKNListsRead);
			pw.println("Num of entries retrieved from NKMaps: " + numOfNKMapLookups);
			pw.println("Num of answer trees explored: " + numOfAnswerTreesExplored);
			pw.println("Num of answer tree sets created: " + numOfAnswerTreeSetsCreated);
			pw.println("Num of answer tree sets removed: " + numOfAnswerTreeSetsRemoved);
//			pw.println("Max Num of answer tree sets in Q[]: " + maxQSize);
		}

	public void printStat(PrintWriter pw, double elapseTime) {
		pw.print(this.getClass().getSimpleName().substring(0, 8) + ", \t");
		pw.print(Params.K + ", ");

		if (ResultDestNodes == null) {
			pw.print("(no result) "); 
		}
		else {
			pw.printf("%.5f, %.5f, ", avgResultRelev, avgResultRelev/ResultRelevs[0]); 
			pw.printf("%.5f, ", avgResultDiss);
		}

		pw.print(numOfKNListsRead + ", ");
		pw.print(numOfNKMapLookups + ", ");
		pw.print(numOfAnswerTreesExplored + ", ");
		pw.print(elapseTime + ", ");

		pw.print(numOfAnswerTreeSetsCreated + ", ");
		pw.print(numOfAnswerTreeSetsRemoved + ", ");
//		pw.print(maxQSize + ", ");
		pw.print(numOfATSPrunnedByDissCond + ", ");
        pw.print(numOfLocalAnswersFound + ", ");
        /*
        if (ResultRelevs != null) {
    		pw.printf("%.2f, %.2f: ",SumResultRelev/ResultRelevs.length, 
				SumReducedResultRelev/(ResultRelevs.length-numOfNonReducedAnswers));
    		pw.printf("%.2f:: ",SumResultRelev/ResultRelevs.length/ResultRelevs[0]); 
    		pw.printf("%.2f ", SumOfScoresOfAnswerTreesExplored/numOfAnswerTreeExplored/ResultRelevs[0]);
    		pw.printf("%.2f, ", DCG); 
    		pw.printf("%.2f, ", DCG2); 
        }
 */
/*		double min = 10000.0, max = 0.0, sum = 0.0;
		for (double r : ResultRelevs) {
			if (r < min) min = r;
			if (r > max) max = r;
			sum += r;
		}
		pw.printf("(%.2f, %.2f, %.2f): ",  min, max, sum/ResultRelevs.length);
	*/	//pw.print(elapseTime + ", ");		
//        pw.printf("(%.2f, %d, %d)",
//		    numOfNKMapLookups2 / (float)numOfNKMapLookupsByKeyword, maxNKMap, minNKMap);
	        pw.println();
	}
	
	public void printStatHeader(PrintWriter pw) {
		pw.print("Top-k, ");
		pw.printf("(AvgResultRelev, AvgResultRelev/ResultRelevs[0], ");
	       	pw.printf("AvgResultDiss) ");
		pw.print("numOfKNListsRead, ");
		pw.print("numOfNKMapLookups, ");
		pw.print("numOfAnswerTreesExplored, ");

		pw.print("elapseTime, ");

		pw.print("numOfAnswerTreeSetsCreated, ");
		pw.print("numOfAnswerTreeSetsRemoved, ");
//		pw.print("maxQueueSize, ");
		pw.print("numOfATSPrunnedByDissCond, ");
        pw.print("numOfLocalAnswersFound, ");

       	pw.println();
	}
		/*
        if (ResultRelevs != null) {
    		pw.printf("(SumResultRelev/ResultRelevs.length, SumReducedResultRelev/(ResultRelevs.length-numOfNonReducedAnswers)");
    		pw.printf("(SumResultRelev/ResultRelevs.length/ResultRelevs[0])"); 
    		pw.printf("(SumOfScoresOfAnswerTreesExplored/numOfAnswerTreeExplored/ResultRelevs[0])");
        }
//		pw.print("elapseTime, ");		
        pw.printf("(numOfNKMapLookups2/(float)numOfNKMapLookupsByKeyword, maxNKMap, minNKMap)");
        */
}
