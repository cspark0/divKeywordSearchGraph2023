package queryProcessing;
import util.*;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class Astar1 extends AstarSearcher {
        public Astar1() {}

        public Astar1(DirectedGraph<Integer, DefaultEdge> g) {
                super(g);
        }

 //       @Override
 //       protected State heuristicSearch() { return null;}

        @Override
        protected State heuristicSearch(boolean finalRound) {
                int lastIndexOfTops = Tops.size()-1;
                double relOfLastTreeInTops = Tops.get(lastIndexOfTops).score;
                double ub_unseen = 0.0;
                State s = null; 
				State answerFound = null;
                int num_of_states_created = 0;
                int num_of_states_explored = 0;
                int numOfStates = 0;

                logger.debug("New heuristic search starts...");
                Q.clear();
                Q.add(new State());
                num_of_states_created++;

                while ((s = Q.poll()) != null) {
                        num_of_states_explored++;
                        numOfStates--;
//                      logger.debug("H.poll(): " + s);

                        if (s.answer.size() == Params.K) {
                                logger.debug("k size answer: {}", s);
                                logger.debug("s.score: {}, ub_unseen: {}", s.score, ub_unseen);
                                if (finalRound || s.score >= ub_unseen) {
                                        answerFound = s;
                                        logger.debug("optimal answer found!");
                                }
                                break;
                        }

                        boolean s_was_expanded = false;
                        for (int i = s.pos + 1; i <= lastIndexOfTops; i++) {
                                AnswerTree t = Tops.get(i);
                                double newSumDiss = checkDissCondition(s.answer, t, s.sumDiss);
                                if (newSumDiss >= 0) { // diss-condition is satisfied!
                                        State e = new State(s.answer, t, s.score + t.score, i, newSumDiss);
                                        //
                                        //
                                        // start of computeUpperBound(e);
                                        if (e.answer.size() == Params.K) {
                                                e.ub = e.score;
                                        }
                                        else {
                                                double ub = e.score;
                                                int j = e.pos + 1;
                                                int n = e.answer.size();
                                                while (j <= lastIndexOfTops && n < Params.K) { 
                                                	if (checkDissCondition(e.answer, Tops.get(j), e.sumDiss) >= 0) {
                                                        ub += Tops.get(j).score;
                                                        n++;
                                                	}
                                                	j++;
                                                }
                                                if (n < Params.K) {
                                                    ub += (Params.K - n) * relOfLastTreeInTops;
                                                }
                                                e.ub = ub;                                        
                                                /*
                                                int first = e.pos + 1;
                                                int last = e.pos + Params.K - e.answer.size();
                                                if (last <= lastIndexOfTops) {
                                                        for (int j = first; j <= last; j++)
                                                               ub += Tops.get(j).score;
                                                }
                                                else {  // last > lastIndexOfTops
                                                        for (int j = first; j <= lastIndexOfTops; j++)
                                                               ub += Tops.get(j).score;
                                                        ub += (last - lastIndexOfTops) * relOfLastTreeInTops;
                                                }                                              
                                                e.ub = ub;
                                                */
                                        }
                                        // end of computeUpperBound(e);
                                        //
                                        //
                                        Q.add(e);
                                        s_was_expanded = true;
                                        num_of_states_created++;
                                        numOfStates++;
                						if (numOfStates > maxNumOfStates)
							                maxNumOfStates = numOfStates;
//                                      logger.debug("New state: " + e);
                                }
                        }
                        if (s_was_expanded == false) {
                                double ub_unseen_with_s = s.score + (Params.K - s.answer.size()) * relOfLastTreeInTops;
                                if (ub_unseen_with_s > ub_unseen) {
                                        ub_unseen = ub_unseen_with_s;
                                        // update ub-unseen by upper bound of the score of a set of div k answer trees consisting of s.answer and unseen trees outside Tops
//                                      logger.debug("New ub_unseen: " + ub_unseen);
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
                return answerFound;
        }

}

