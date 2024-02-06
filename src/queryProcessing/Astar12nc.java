package queryProcessing;
import util.*;
import java.util.List;
import java.io.PrintWriter;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class Astar12nc extends AstarNcSearcher {

        public Astar12nc() {}

        public Astar12nc(DirectedGraph<Integer, DefaultEdge> g) {
                super(g);
        }

/*
        @Override
		protected double getLocalAnswerScore() {
				return lowerBound;
		}
*/
        @Override
        protected State heuristicSearch(double[] score) {
				for (int i = 0; i <= Params.K; i++) score[i] = (double)0;

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
                                else {	
									// here, s.score > localAnswer.score
								//	if (!s.hasTheSameAnswer(localAnswerSt)) {
										localAnswerSt = s;
                                        lowerBound = s.score;
										numOfLocalAnswersFound++;
                                    	logger.debug("a new local answer found!");
                                    	logger.debug("new lowerBound: " + lowerBound);
								//	}
									optAnswerFound = null;
									score[Params.K] = s.score;
                                	logger.debug("no optimal answer");
                                }
								break;
                        }
						
						//////////////////////////////////////////////
						// Astar1 feature: early stop condition
                        if (s.ub < ub_unseen) // no optimal answer exists in Tops!  
						{
							if (s.score > score[s.answer.size()]) score[s.answer.size()] = s.score;
							break; 
						}
						//////////////////////////////////////////////

                        boolean s_was_expanded = false;
                        for (int i = s.pos + 1; i <= lastIndexOfTops; i++) {
                                AnswerTree t = Tops.get(i);
                                double newSumDiss = checkDissCondition(s.answer, t, s.sumDiss);
                                if (newSumDiss >= 0) { // diss-condition is satisfied!
                                        State e = new State(s.answer, t, s.score + t.score, i, newSumDiss);
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
												//////////////////////////////////////////
											    // Astar12nc feature
                                                if (n < Params.K) {
                                                    ub += (Params.K - n) * relOfLastTreeInTops;
                                                }
												//////////////////////////////////////////
                                                e.ub = ub;                                        
                                        }
                                        // end of computeUpperBound(e);
                                        //
                                        //
								//		if (n == Params.K) {
												///////////////////////////////
												// Astar2 feature: lower-bound check
												///////////////////////////////
												if (e.ub > lowerBound) {       // it is possible to find a better div-k answer
													//////////////////////////////////////////////
													// Astar1 feature: early stop condition
													//////////////////////////////////////////////
													//if (e.answer.size() == Params.K || e.ub >= ub_unseen) {
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
								//		}
								/*
										else {
											//////////////////////////////////////////////
											// Astar12nc feature
											double ub_unseen_with_e = e.ub + (Params.K-n)*relOfLastTreeInTops;
											if (ub_unseen_with_e > ub_unseen) {
												ub_unseen = ub_unseen_with_e; 	// update ub_unseen 
												logger.debug("New ub_unseen: " + ub_unseen);
											}
											if (e.ub > score[n]) score[n] = e.ub;
										}
								*/
                                }
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
								// Astar12nc feature
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
				/*
				if (localAnswer != null && localAnswer.score >= ub_unseen) {
					// the previous local answer became the optimal answer!
					logger.debug("optimal answer found!");
                	return localAnswer;
				}
				*/
                return optAnswerFound;
        }

}

