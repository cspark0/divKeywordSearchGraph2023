package queryProcessing;
import util.*;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

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
                //Q.add(new State());
				try {
				   	s = stPool.borrowObject();
				} catch(Exception ex1) {
				    throw new RuntimeException("Unable to borrow State from stpool" + ex1.toString());
                }
                Q.add(s);
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
							//////////////////////////////////////////////
							// Astar1nc feature
							if (s.ub > score[Params.K]) score[Params.K] = s.ub;
							////////////////// 

							try {
								stPool.returnObject(s);
							} catch(Exception ex1) {
								throw new RuntimeException("Unable to return a State into stPool" + ex1.toString());
							}
							////////////////// 
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
									//if (!s.hasTheSameAnswer(localAnswerSt)) {
									if (s.score > localAnswerSt.score) {
										////////////////// 
										if (localAnswerSt != null) {
											try {
												stPool.returnObject(localAnswerSt);
											} catch(Exception ex1) {
												throw new RuntimeException("Unable to return a localAnswerSt into stPool" + ex1.toString());
											}
										}
										////////////////// 
										localAnswerSt = s;

										numOfLocalAnswersFound++;
                                    	logger.debug("a new local answer found!");
									}
									////////////////// 
									else {
										try {
											stPool.returnObject(s);
										} catch(Exception ex1) {
											throw new RuntimeException("Unable to return a State s into stPool" + ex1.toString());
										}
									}
									////////////////// 

									//////////////////////////////////////////////
									// Astar1nc feature
									if (s.score > score[Params.K]) score[Params.K] = s.score;
									//////////////////////////////////////////////
                                	logger.debug("no optimal answer");
								}
                                break;
                        }

                        boolean s_was_expanded = false;
                        for (int i = s.pos + 1; i <= lastIndexOfTops; i++) {
                                AnswerTree t = Tops.get(i);
                                double newSumDiss = checkDissCondition(s.answer, t, s.sumDiss);
                                logger.debug("i: " + i + ", newSumDiss: " + newSumDiss);
                                if (newSumDiss >= 0) { // diss-condition is satisfied!
                                        //State e = new State(s.answer, t, s.score + t.score, i, newSumDiss);
                                      	State e = null;
										try {
											e = stPool.borrowObject();
										} catch(Exception ex1) {
                                			System.out.println("i: " + i + " " + stPool.getNumIdle() + " " + stPool.getNumActive());
											throw new RuntimeException("Unable to borrow a State e from stPool" + ex1.toString());
										}
										e.setState(s.answer, t, s.score + t.score, i, newSumDiss);

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
                                                while (j <= lastIndexOfTops && n < Params.K) { 
                                                	if (checkDissCondition(e.answer, Tops.get(j), e.sumDiss) >= 0) {
                                                        ub += Tops.get(j).score;
                                                        n++;
                                                	}
                                                	j++;
                                                }
												//////////////////////////////////////////////
												// _ap feature: aggresive pruning
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
										// _ap feature: aggresive pruning 
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
											//////////////////
											else {
												//////////////////////////////////////////////
												// Astar1nc feature
												if (e.ub > score[n]) score[n] = e.ub;
												//////////////////////////////////////////////

												try {
													stPool.returnObject(e);
												} catch(Exception ex1) {
													throw new RuntimeException("Unable to return a State e into stPool" + ex1.toString());
												}
											}
											//////////////////
										}
										//////////////////////////////////////////////
										// _ap feature: higher ub_unseen
										else {	// n < Params.K
											double ub_unseen_with_e = e.ub + (Params.K-n)*relOfLastTreeInTops;
											if (ub_unseen_with_e > ub_unseen) {
												ub_unseen = ub_unseen_with_e; 	// update ub_unseen 
												logger.debug("New ub_unseen: " + ub_unseen);
											}
											//////////////////////////////////////////////
											// Astar1nc feature
											if (e.ub > score[n]) score[n] = e.ub;
											//////////////////////////////////////////////

											//////////////////
											try {
												stPool.returnObject(e);
											} catch(Exception ex1) {
												throw new RuntimeException("Unable to return a State e into stPool" + ex1.toString());
											}
											//////////////////
										}
										//////////////////////////////////////////////
                                }
								else numOfATSPrunnedByDissCond++;
                        }
						/* below is not necessary becuase such state s cannot be previously added into Q 
						   becuase of the above condition n < Params.K
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
						*/
						////////////////// 
						try {
							stPool.returnObject(s);
						} catch(Exception ex1) {
							throw new RuntimeException("Unable to return a State s into stPool" + ex1.toString());
						}
						////////////////// 
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
				/////
				returnAllStatesInQIntoPool();
				/////
                return answerFound;
        }
}

