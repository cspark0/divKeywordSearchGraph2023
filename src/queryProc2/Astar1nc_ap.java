package queryProc2;

import util.*;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import java.util.List;

public class Astar1nc_ap extends AstarNcSearcher {

		/////////////////////////////////////////////////////////////////////
		/* conditional execution of A* search by using necessary condition */
		/* aggresive pruning by using higher upper-bound of unseen answers */
		/////////////////////////////////////////////////////////////////////

        public Astar1nc_ap() {}

        public Astar1nc_ap(DirectedGraph<Integer, DefaultEdge> g) {
                super(g);
        }

        @Override
        protected State heuristicSearch(double[] score) {
				for (int i = 0; i <= Params.K; i++) score[i] = (double)0;

                int lastIndexOfTops = Tops.size()-1;
                double relOfLastTreeInTops = Tops.get(lastIndexOfTops).score;
                double ub_unseen = 0.0;
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

						//////////////////////////////////////////////
						// Astar1 feature: early stop condition
                        if (s.ub < ub_unseen) // no optimal answer exists in Tops!  
						{
							// if (s.score > score[s.answer.size()]) score[s.answer.size()] = s.score;
							break; 
						}
						//////////////////////////////////////////////

                        if (s.answer.size() == Params.K) {
                                logger.debug("k size answer: {}", s);
                                logger.debug("s.score: {}, ub_unseen: {}", s.score, ub_unseen);

                        		if (s.score >= ub_unseen) {
                                	answerFound = s;
                                	logger.debug("optimal answer found!");
								}
								else {
									// no optimal answer in Tops
									if (!s.hasTheSameAnswer(localAnswerSt)) {
										localAnswerSt = s;
										numOfLocalAnswersFound++;
                                    	logger.debug("a new local answer found!");
                                    	logger.debug("new lowerBound: " + lowerBound);
									}
									//////////////////////////////////////////////
									// Astar1nc feature
									score[Params.K] = s.score;
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

												//////////////////////////////////////////////
												// Astar1_ap feature: aggresive pruning
												/*
                                                if (n < Params.K) {
                                                    ub += (Params.K - n) * relOfLastTreeInTops;
                                                }
												*/
                                                e.ub = ub;                                        
                                        }
                                        // end of computeUpperBound(e);
                                        //
                                        //
										//////////////////////////////////////////////
										// Astar1_ap feature: aggresive pruning 
										if (n == Params.K) {
										//////////////////////////////////////////////
											//////////////////////////////////////////////
											// Astar1 feature: early stop condition
											if (e.ub >= ub_unseen) {
		                                    	Q.add(e);
												s_was_expanded = true;
												num_of_states_created++;
												num_of_states_in_Q++;
												if (num_of_states_in_Q > maxNumOfStates)
							                		maxNumOfStates = num_of_states_in_Q;
                                        		logger.debug("Added state e: " + e);
											}
										}
										//////////////////////////////////////////////
										// Astar1_ap feature: higher ub_unseen
										else {
											double ub_unseen_with_e = e.ub + (Params.K-n)*relOfLastTreeInTops;
											if (ub_unseen_with_e > ub_unseen) {
												ub_unseen = ub_unseen_with_e; 	// update ub_unseen 
												logger.debug("New ub_unseen: " + ub_unseen);
											}
											//////////////////////////////////////////////
											// Astar1nc feature
											if (e.ub > score[n]) score[n] = e.ub;
										}
										//////////////////////////////////////////////
                                }
								else numOfATSPrunnedByDissCond++;
                        }
                        if (s_was_expanded == false) {
								int n = s.answer.size();
                                double ub_unseen_with_s = s.score + (Params.K-n)*relOfLastTreeInTops;
                                if (ub_unseen_with_s > ub_unseen) {
                                        ub_unseen = ub_unseen_with_s;
                                        // update ub-unseen by upper bound of the score of a set of div k answer trees consisting of s.answer and unseen trees outside Tops
                                        logger.debug("New ub_unseen: " + ub_unseen);
                                }
								//////////////////////////////////////////////
								// Astar1nc feature
								if (s.score > score[n]) score[n] = s.score;
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
                return answerFound;
        }
}

