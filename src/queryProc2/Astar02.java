package queryProc2;
import util.*;
import java.util.List;
import java.io.PrintWriter;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class Astar02 extends AstarSearcher {

        public Astar02() {}

        public Astar02(DirectedGraph<Integer, DefaultEdge> g) {
                super(g);
        }

        @Override
        protected State heuristicSearch() {
                int lastIndexOfTops = Tops.size()-1;
                double relOfLastTreeInTops = Tops.get(lastIndexOfTops).score;
                double ub_unseen = 0.0;
                State s = null; 
				State optAnswerFound = null;
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
                                logger.debug("s.score: {}, ub_unseen: {}", s.score, ub_unseen);
								//////////////////////////////////////////////
								// Astar2 feature: keep the best local answer
								//////////////////////////////////////////////
                                if (s.score >= ub_unseen) {
									optAnswerFound = s;
									logger.debug("optimal answer found!");
                                }
                                else {	// here, s.score > localAnswer.score
									//if (!s.hasTheSameAnswer(localAnswerSt)) {
										localAnswerSt = s;
                                        lowerBound = s.score;
										numOfLocalAnswersFound++;
                                    	logger.debug("a new local answer found!");
                                    	logger.debug("new lowerBound: " + lowerBound);
									//}
									optAnswerFound = null;
                                	logger.debug("no optimal answer");
                                }
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
                                                    ub += (Params.K - n) * relOfLastTreeInTops;
                                                }
                                                e.ub = ub;                                        
                                        }
                                        // end of computeUpperBound(e);
                                        //
                                        //
									//	if (n == Params.K) {
												///////////////////////////////
												// Astar2 feature: lower-bound check
												///////////////////////////////
												if (e.ub > lowerBound) {       // it is possible to find a better div-k answer
														Q.add(e);
														s_was_expanded = true;
														num_of_states_created++;
														num_of_states_in_Q++;
														if (num_of_states_in_Q > maxNumOfStates)
															maxNumOfStates = num_of_states_in_Q;
														logger.debug("Added state e: " + e);
												}
									//	}
									/*
										else {
											if (e.ub > ub_unseen) { // update ub-unseen 
												ub_unseen = e.ub;
												logger.debug("New ub_unseen: " + e.ub);
											}
										}
									*/
                                }
								else numOfATSPrunnedByDissCond++;
                        }
                        if (s_was_expanded == false) {
                                double ub_unseen_with_s = s.score + (Params.K - s.answer.size()) * relOfLastTreeInTops;
                                if (ub_unseen_with_s > ub_unseen) {
                                        ub_unseen = ub_unseen_with_s;
                                        // update ub-unseen by upper bound of the score of a set of div k answer trees consisting of s.answer and unseen trees outside Tops
                                        logger.debug("New ub_unseen: " + ub_unseen);
                                }
                        }
                }
                logger.debug("Q.size()={} ", Q.size());
                logger.debug("ub_unseen={} ", ub_unseen);
//                if (s != null) {
 //                       logger.debug("s.ub:{}, s.pos:{}", s.ub, s.pos);
  //                      logger.debug("lastIndexOfTops:{}", lastIndexOfTops);
   //             }
                logger.debug("created:{}, explored(removed):{}", num_of_states_created, num_of_states_explored);
				numOfStatesCreated += num_of_states_created;
                numOfStatesExplored += num_of_states_explored;
				
				if (s == null) {
					if (localAnswerSt != null && localAnswerSt.score >= ub_unseen) {
							// the previous local answer became the optimal answer!
							optAnswerFound = localAnswerSt;
							logger.debug("optimal answer found!");
					}
					else { optAnswerFound = null; }
				}	
                return optAnswerFound;
        }
}

