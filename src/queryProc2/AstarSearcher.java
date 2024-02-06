package queryProc2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.io.PrintWriter;

import my.jgrapht.algorithm.BellmanFordShortestPathMod;
import nkmap.bdb.RelSourceFirst;

import org.jgrapht.DirectedGraph;
//import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.graph.DefaultEdge;

import util.State;
import util.TargetList;
import util.AnswerTree;
import util.AnswerTreeComparator;
import util.ListEntry;
import util.Params;

import queryProcessing.DivTopKSearcher;

public abstract class AstarSearcher extends DivTopKSearcher {

//      FibonacciHeap<TopKNode> T;                              // global top-k queue T
        List<AnswerTree> Tops;
        PriorityQueue<State> Q;

        int numOfStatesCreated;
        int numOfStatesExplored;
        int numOfHeuristicSearchPerformed;
        int maxNumOfStates;

		State localAnswerSt;  // keep the best sub-optimal(local) div-k answer found 
        double lowerBound;   // keep the score of curAnswer 

        boolean listFinisted = false;

        protected AstarSearcher() {
                super();
                init();
        }

        protected AstarSearcher(DirectedGraph<Integer, DefaultEdge> g) {
                super(g);
                init();
        }

        protected void init() {
                Tops = new ArrayList<AnswerTree>();
                Q = new PriorityQueue<State>(
						100000,
                        new Comparator<State>() {
                                @Override
                                public int compare(State s1, State s2) {
                                        //return Float.compare(s2.ub, s1.ub);    // reverse order of ub
                                        return Double.compare(s2.ub, s1.ub);    // reverse order of ub
                                }
                        });
        }

        @Override
        protected boolean prepareSearch(List<String> query) {
                if (super.prepareSearch(query) == false)
                        return false;

                numOfStatesCreated = 0;
	            numOfStatesExplored = 0;
                numOfHeuristicSearchPerformed = 0;

				localAnswerSt = null;
                lowerBound = 0;
//          numOfNKMapLookups2 = 0;
//       numOfNKMapLookupsByKeyword = maxNKMap = minNKMap = 0;
//              DCG = 0.0;      DCG2 = 0.0;
                return true;
        }

        @Override
        public void completeSearch() {
                Tops.clear();
                Q.clear();
			    numOfStatesCreated = 0;
			    numOfStatesExplored = 0;
			    numOfHeuristicSearchPerformed = 0;

				localAnswerSt = null;
                lowerBound = 0;
                super.completeSearch();
        }

        public void search(List<String> query) {

                if (prepareSearch(query) == false) return; // null;

                AnswerTree t = null;

                while (Tops.size() < Params.K-1) {
                        if ((t = nextTopAnswer(query)) == null) {
                                logger.debug("k answer trees for q do not exist in G!");
                                return; // null;
                        }
                        Tops.add(t);
                }

                while ((t = nextTopAnswer(query)) != null) {
                        Tops.add(t);
                        // logger.debug("Tops:" + Tops);
                        // List<AnswerTree> optimalDivTopKAnswer = heuristicSearch();
                        maxNumOfStates = 0;
						logger.debug("{}th heuristic sesarch starts...", numOfHeuristicSearchPerformed+1);
                        State optimal = heuristicSearch();
                        numOfHeuristicSearchPerformed++;
                        if (maxNumOfStates > maxQSize) maxQSize = maxNumOfStates;

                        if (optimal != null) {	// optimal answer has been found
                                logger.debug("optimal answer found: {}", optimal);
                                getResult(optimal.answer, 0);
                                return; 
                        }
//                      logger.debug("null has been returned...");
                }

                if (Tops.size() < Params.K) {
                	logger.debug("k answer trees for q do not exist in G!");
                    return; 
				}

                logger.debug("all answer trees considered. final round...");
				listFinisted = true;
                State optimal = heuristicSearchInFinalRound();
                numOfHeuristicSearchPerformed++;
                if (optimal != null) {
                        logger.debug("optimal answer found: {}", optimal);
                        getResult(optimal.answer, 0);
                }
        }

	    //////////////////////////////////////////////////////////
	    // queryProc2 feature: using new dissimilarity condition 
        protected boolean checkDissCondition(List<AnswerTree> S, AnswerTree t) {
				if (S.size() == 0) return true;
				double sumDissOfSAndt = 0.0;
                for (int i = 0; i < S.size(); i++) {
                        sumDissOfSAndt += computeDissimilarityByDSC(S.get(i).srcNodes, t.srcNodes);
                }
                logger.debug("avgDissOfSAndt: {}", sumDissOfSAndt/S.size());
        		if (sumDissOfSAndt / S.size() >= Params.tau) 
        		    return true;
		        return false;
        }
	    //////////////////////////////////////////////////////////

        abstract protected State heuristicSearch();

        protected State heuristicSearchInFinalRound() {
                int lastIndexOfTops = Tops.size()-1;
                double relOfLastTreeInTops = Tops.get(lastIndexOfTops).score;
                State s = null; 
				State answerFound = null;
                int num_of_states_created = 0;
                int num_of_states_explored = 0;
                int num_of_states_in_Q = 0;

                logger.debug("New heuristic search starts...");
                Q.clear();
                Q.add(new State());
                num_of_states_created++;
                num_of_states_in_Q++;

                while ((s = Q.poll()) != null) {
                        num_of_states_explored++;
                        num_of_states_in_Q--;
//                        logger.debug("Popped state s: " + s);

                        if (s.answer.size() == Params.K) {
                                logger.debug("k size answer: {}", s);
                                logger.debug("s.score: {}", s.score);
                                answerFound = s;
                                logger.debug("optimal answer found!");
                                break;
                        }

                        boolean s_was_expanded = false;
                        for (int i = s.pos + 1; i <= lastIndexOfTops; i++) {
                                AnswerTree t = Tops.get(i);

								//////////////////////////////////////////////////////////
								// queryProc2 feature: using new dissimilarity condition 
								if (checkDissCondition(s.answer, t) == true) {

                                        State e = new State(s.answer, t, s.score + t.score, i, 0);
                                        //
                                        //
                                        // start of computeUpperBound(e);
										int n = Params.K;

                                        if (e.answer.size() == Params.K) {
                                                e.ub = e.score;
                                        }
                                        else {
                                                double ub = e.score;
                                                int j = e.pos + 1;
												List<AnswerTree> S = e.answer;
                                                n = S.size();
												//////////////////////////////////////////////
												// queryProc2 feature: different way to compute upper-bound
                                                while (j <= lastIndexOfTops && n < Params.K) { 
														AnswerTree tt = Tops.get(j);
														double sumDissOfSAndt = 0.0;
														for (int k = 0; k < S.size(); k++) {
																sumDissOfSAndt += computeDissimilarityByDSC(S.get(k).srcNodes, tt.srcNodes);
														}
														if ((sumDissOfSAndt + n - S.size()) / n >= Params.tau) {
                                                        	ub += tt.score;
	                                                        n++;
                                                		}
	                                                	j++;
                                                }
												//////////////////////////////////////////////
                                                if (n < Params.K) {
                                                    ub = 0.0;		// no answer in Tops
                                                }
                                                e.ub = ub;                                        
                                        }
                                        // end of computeUpperBound(e);
                                        //
                                        //
										if (n == Params.K) {
		                                   	Q.add(e);
											s_was_expanded = true;
											num_of_states_created++;
											num_of_states_in_Q++;
											if (num_of_states_in_Q > maxNumOfStates)
							               		maxNumOfStates = num_of_states_in_Q;
                                        	logger.debug("Added state e: " + e);
										}
                                }
								else numOfATSPrunnedByDissCond++;
                        }
                }
                logger.debug("Q.size()={} ", Q.size());
//                if (s != null) {
 //                       logger.debug("s.ub:{}, s.pos:{}", s.ub, s.pos);
  //                      logger.debug("lastIndexOfTops:{}", lastIndexOfTops);
   //             }
                logger.debug("created:{}, explored(removed):{}", num_of_states_created, num_of_states_explored);
				numOfStatesCreated += num_of_states_created;
                numOfStatesExplored += num_of_states_explored;
                return answerFound;
        }

        public void printResult(PrintWriter pw, List<String> q) { //, List<AnswerTree> ans) {

                pw.println(q);

                if (optimalAnswer == null) {
                        pw.println("Div top-k answers for q do not exist in G!");
                }
                else {
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

//                              SumResultRelev += ResultRelevs[i];
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
                        //      IDCG += (ResultRelevs_sorted[ResultDestNodes.length-i-1]/logval);
                        //      p
*/
                        }
//                      DCG = DCG/ResultRelevs[0];
                        //nDCG = DCG / IDCG;

                        pw.println("-----------------------------------------");
                        pw.println("ResultDestNodes: " + Arrays.toString(ResultDestNodes));
                        pw.println("ResultRelevs: " + Arrays.toString(ResultRelevs));
                        pw.println("AvgResultRelev: " + avgResultRelev);
                        pw.println("AvgResultDissimilarity: " + avgResultDiss);
                }

                pw.println("Num of entries retrieved from the KNLists: " + numOfKNListsRead);
                pw.println("Num of entries retrieved from NKMaps: " + numOfNKMapLookups);
                pw.println("Num of answer trees explored: " + numOfAnswerTreesExplored);
                pw.println("Num of states created: " + numOfStatesCreated);
                pw.println("Num of states explored: " + numOfStatesExplored);
                pw.println("Num of heuristic search performed: " + numOfHeuristicSearchPerformed);
//                pw.println("Max Num of answer tree sets in Q[]: " + maxQSize);
        }

        public void printStat(PrintWriter pw, double elapseTime) {
//				String s = this.getClass().getName().substring(16); 
				String s = this.getClass().getSimpleName(); 
				if (s.length() < 10) 
                	pw.print(s + ", \t");
				else
                	pw.print(s.substring(0, 9) + ", \t");
                pw.print(Params.K + ", ");

                if (optimalAnswer == null) {
                        pw.print("(no result) ");
                }
                else {
                        pw.printf("%.5f, %.5f, %.5f, ", avgResultRelev, avgResultRelev/ResultRelevs[0], avgResultDiss);
                }

/*
                pw.printf("%.2f, %.2f: ",SumResultRelev/ResultRelevs.length,
                                SumReducedResultRelev/(ResultRelevs.length-numOfNonReducedAnswers));
                pw.printf("%.2f:: ",SumResultRelev/ResultRelevs.length/ResultRelevs[0]);
                pw.printf("%.2f ", SumOfScoresOfAnswerTreesExplored/numOfAnswerTreeExplored/ResultRelevs[0]);
                pw.printf("%.2f, ", DCG);
                pw.printf("%.2f, ", DCG2);
*/
/*              double min = 10000.0, max = 0.0, sum = 0.0;
                for (double r : ResultRelevs) {
                        if (r < min) min = r;
                        if (r > max) max = r;
                        sum += r;
                }
                pw.printf("(%.2f, %.2f, %.2f): ",  min, max, sum/ResultRelevs.length);
*/
//          pw.printf("(%.2f, %d, %d)",
//                  numOfNKMapLookups2 / (double)numOfNKMapLookupsByKeyword, maxNKMap, minNKMap);
                pw.print(numOfKNListsRead + ", ");
                pw.print(numOfNKMapLookups + ", ");
                pw.print(numOfAnswerTreesExplored + ", ");
		        pw.print(elapseTime + ", ");
                pw.print(numOfStatesCreated + ", ");
                pw.print(numOfStatesExplored + ", ");
                pw.print(numOfHeuristicSearchPerformed + ", ");
//                pw.print(maxQSize + ", ");
				pw.print(numOfATSPrunnedByDissCond + ", ");
                pw.print(numOfLocalAnswersFound + ", ");
				if (listFinisted == true) pw.print("KNList ends.");
                pw.println();
        }

        public void printStatHeader(PrintWriter pw) {
                pw.print("Top-k, ");

                pw.printf("(AvgResultRelev, AvgResultRelev/ResultRelevs[0], AvgResultDiss) ");
                /*
        if (ResultRelevs != null) {
                pw.printf("(SumResultRelev/ResultRelevs.length, SumReducedResultRelev/(ResultRelevs.length-numOfNonReducedAnswers)");
                pw.printf("(SumResultRelev/ResultRelevs.length/ResultRelevs[0])");
                pw.printf("(SumOfScoresOfAnswerTreesExplored/numOfAnswerTreeExplored/ResultRelevs[0])");
        }
        */
                pw.print("numOfKNListsRead, ");
                pw.print("numOfNKMapLookups, ");
                pw.print("numOfAnswerTreesExplored, ");
                pw.print("elapseTime, ");
                pw.print("numOfStatesCreated, ");
                pw.print("numOfStatesExplored, ");
                pw.print("numOfHeuristicSearchPerformed, ");
				pw.print("numOfATSPrunnedByDissCond, ");
        		pw.print("numOfLocalAnswersFound, ");
//        pw.printf("(numOfNKMapLookups2/(double)numOfNKMapLookupsByKeyword, maxNKMap, minNKMap)");
//                pw.print("maxQSize, ");
                pw.println();
        }
}


