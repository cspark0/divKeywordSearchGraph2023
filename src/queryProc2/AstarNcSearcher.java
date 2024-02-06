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

public abstract class AstarNcSearcher extends AstarSearcher {

		double[] score;
		double[] ub;

        protected AstarNcSearcher() {
                super();
                init();
        }

        protected AstarNcSearcher(DirectedGraph<Integer, DefaultEdge> g) {
                super(g);
                init();
        }

        @Override
        protected void init() {
				super.init();
				score = new double[Params.K+1];
				ub = new double[Params.K+1];
        }

        @Override
        protected boolean prepareSearch(List<String> query) {
                if (super.prepareSearch(query) == false)
                        return false;
				for (int i = 1; i <= Params.K; i++) score[i] = (double)0;
				for (int i = 1; i <= Params.K; i++) ub[i] = (double)0;
                return true;
        }

        /*
        protected void computeUpperBound(State e) {
                if (e.answer.size() == Params.K) {
                        e.ub = e.score;
                        return;
                }
                double ub = e.score;
                int j = e.pos + 1;
                int n = e.answer.size();
                int lastIndexOfTops = Tops.size() - 1;
                while (j <= lastIndexOfTops && n < Params.K) { 
                	if (checkDissCondition(e.answer, Tops.get(j), e.sumDiss) >= 0) {
                        ub += Tops.get(j).score;
                        n++;
                	}
                	j++;
                }
                if (n < Params.K) {
                    ub += (Params.K - n) * Tops.get(lastIndexOfTops).score;
                }
                e.ub = ub;                    
        }
		*/
		
		/*
		protected double getLocalAnswerScore() {
			return 0.0;
		}
		*/
        
		@Override
        public void search(List<String> query) {

                if (prepareSearch(query) == false) return; // null;

                AnswerTree t = null;

                while (Tops.size() < Params.K-1) {
                        if ((t = nextTopAnswer(query)) == null) {
                                logger.debug("k answer trees for q do not exist in G!");
                                return;
                        }
                        Tops.add(t);
                }

                // Tops.size() == Params.K-1
                if ((t = nextTopAnswer(query)) != null) {
                    	Tops.add(t);	// Tops.size == K
					
                        maxNumOfStates = 0;
						logger.debug("{}th heuristic sesarch starts...", numOfHeuristicSearchPerformed+1);
                        State optimal = heuristicSearch(score);
                        numOfHeuristicSearchPerformed++;
                        if (maxNumOfStates > maxQSize) maxQSize = maxNumOfStates;

                        if (optimal != null) {	// optimal answer has been found
                                logger.debug("optimal answer found: {}", optimal);
                                getResult(optimal.answer, 0);
                                return; 
                        }
						else {
							for (int i = 1; i <= Params.K; i++) ub[i] = score[i]; 
						}
				}
				else {
                	logger.debug("k answer trees for q do not exist in G!");
                    return; 
				}

                while ((t = nextTopAnswer(query)) != null) {
                        Tops.add(t);
                        // logger.debug("Tops:" + Tops);
                        // List<AnswerTree> optimalDivTopKAnswer = heuristicSearch();
						double relOfLastTreeInTops = Tops.get(Tops.size()-1).score;

						// compute ub_seen
						for (int i = Params.K; i >= 2; i--) {
							if (ub[i-1] > 0) {
                                double ubi2 = ub[i-1] + relOfLastTreeInTops;
                                if (ubi2 > ub[i]) ub[i] = ubi2;
							}
						}
						ub[1] = relOfLastTreeInTops;
						double ub_seen = ub[Params.K];

						// compute ub_unseen
						double ub_unseen = (double)0;
						for (int i = 1; i < Params.K; i++) { 
							double ub_unseen_i = score[i] + (Params.K-i)*relOfLastTreeInTops;
							if (ub_unseen_i > ub_unseen) ub_unseen = ub_unseen_i;
						}

						// necessary condition
//						double locAnsScore = getLocalAnswerScore();
//						if (locAnsScore == 0) {
						
						if (ub_seen >= ub_unseen) { 	// necessary condition
							logger.debug("{}th heuristic sesarch starts...", numOfHeuristicSearchPerformed+1);
							State optimal = heuristicSearch(score);
							numOfHeuristicSearchPerformed++;
							if (maxNumOfStates > maxQSize) maxQSize = maxNumOfStates;

							if (optimal != null) {	// optimal answer has been found
								logger.debug("optimal answer found: {}", optimal);
                                getResult(optimal.answer, 0);
								return; 
							}
							else {
								for (int i = 1; i <= Params.K; i++) ub[i] = score[i]; 
							}
						}
//						}
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

		@Override
        protected State heuristicSearch() { return null; }

        abstract protected State heuristicSearch(double[] score);

		@Override
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
                                                n = e.answer.size();
												//////////////////////////////////////////////
												// queryProc2 feature: different way to compute upper-bound
                                                while (j <= lastIndexOfTops && n < Params.K) { 
														List<AnswerTree> S = e.answer;
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
}

