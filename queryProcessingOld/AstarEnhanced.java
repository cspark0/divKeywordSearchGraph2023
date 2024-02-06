package queryProcessing;
import util.*;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class AstarEnhanced extends AstarSearcher {
	
	List<State> St = new ArrayList<State>();	// a list of reusable states
	State curAnswerState = null;	// recently found (local optimal) answer tree
	boolean optimalAnswerFound = false;
    
	public AstarEnhanced() {}

        public AstarEnhanced(DirectedGraph<Integer, DefaultEdge> g) {
                super(g);
        }
    	
        @Override
        protected boolean prepareSearch(List<String> query) {
                if (super.prepareSearch(query) == false)
                        return false;
			    St.clear();
			    curAnswerState = null;	
			    optimalAnswerFound = false;
                return true;
        }

        @Override
    	public void search(List<String> query) {

            if (prepareSearch(query) == false) return;

            AnswerTree t = null;

            while (Tops.size() < Params.K-1) {
                    if ((t = nextTopAnswer(query)) == null) {
                            logger.debug("k answer trees for q do not exist in G!");
                            return; // null;
                    }
                    Tops.add(t);
            }

            int lastIndexOfTops = Params.K-2;
            while ((t = nextTopAnswer(query)) != null) {
                    Tops.add(t);
            		lastIndexOfTops++;
            		double relOfLastTreeInTops = t.score;
                    logger.debug("Tops:" + Tops);

                    maxNumOfStates = 0;
                    State optimal = null;
                    if (curAnswerState == null)
                    	optimal = heuristicSearch(lastIndexOfTops, relOfLastTreeInTops);
                    else
                    	optimal = heuristicSearchWithReuse(lastIndexOfTops, relOfLastTreeInTops);
                    
                    numOfHeuristicSearchPerformed++;
                    if (maxNumOfStates > maxQSize) maxQSize = maxNumOfStates;

                    if (optimal != null) {	// optimal answer has been found
                            logger.debug("optimal answer found: {}", optimal);
                            // find!!!
                            getResult(optimal.answer, optimal.sumDiss);
                            return; 
                    }
//                  logger.debug("null has been returned...");
            }
            logger.debug("all answer trees considered...");
            if (curAnswerState != null) {
                    logger.debug("optimal answer found: {}", curAnswerState);
                    getResult(curAnswerState.answer, curAnswerState.sumDiss);
            }
            logger.debug("no answer found!");
        }

        protected State heuristicSearch(int lastIndexOfTops, double relOfLastTreeInTops) {
                double ub_unseen = 0.0;
                State s = null, optAnswerState = null;
                int num_of_states_created = 0;
                int num_of_states_explored = 0;
                int numOfStates = 0;

                logger.debug("New heuristic search starts...");
                Q.clear();
                Q.add(new State());
                numOfStatesCreated++;
                num_of_states_created++;

                St.clear();

                while ((s = Q.poll()) != null) {
                        numOfStatesExplored++;
                        num_of_states_explored++;
                        numOfStates--;
//                      logger.debug("Q.poll(): " + s);

                        if (s.answer.size() == Params.K) {
                                logger.debug("k size answer: {}", s);
                                logger.debug("s.score: {}, ub_unseen: {}", s.score, ub_unseen);
                                if (s.score >= ub_unseen) {
                                	optAnswerState = s;
                                    logger.debug("optimal answer found!");
                                }
                                else {
                            		optAnswerState = null;
                                	curAnswerState = s;
                                	logger.debug("a new local answer found!");
                                }
                                logger.debug("ub_unseen={} ", ub_unseen);
                                logger.debug("created:{}, explored(removed):{}", num_of_states_created, num_of_states_explored);
                                logger.debug("maxQSize:{}", maxNumOfStates);
                                return optAnswerState;
                        }
						/*
                        else if (s.pos == lastIndexOfTops) {
							 // no k-size answer better than s exists in Tops!  
                             break; 
                        }
						*/

                        boolean s_derives_a_child_state = false;
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
                                        }
                                        // end of computeUpperBound(e);
                                        //
                                        //
                                        Q.add(e);
                                        s_derives_a_child_state = true;
                                        numOfStatesCreated++;
                                        num_of_states_created++;
                                        numOfStates++;
                                        if (numOfStates > maxNumOfStates) maxNumOfStates = numOfStates;
//                                      logger.debug("New state: " + e);
                                }
                        }
                        if (s_derives_a_child_state == false) {
                                double ub_unseen_with_s = s.score + (Params.K - s.answer.size()) * relOfLastTreeInTops;
                                if (ub_unseen_with_s > ub_unseen) {
                                        ub_unseen = ub_unseen_with_s;
                                        // update ub-unseen by upper bound of the score of a set of div k answer trees consisting of s.answer and unseen trees outside Tops
//                                      logger.debug("New ub_unseen: " + ub_unseen);
                                }
                                s.pos = lastIndexOfTops;	// all trees in Tops have been considered for deriving states from s
                                St.add(s);
                        }
                }
                logger.debug("Q is empty and no local answer found.");
                logger.debug("ub_unseen={} ", ub_unseen);
                logger.debug("created:{}, explored(removed):{}", num_of_states_created, num_of_states_explored);
                logger.debug("maxQSize:{}", maxNumOfStates);
                
                St.clear();
                curAnswerState = null;
                return null;
        }
        
        protected State heuristicSearchWithReuse(int lastIndexOfTops, double relOfLastTreeInTops) {
            double ub_unseen = 0.0;
            State s = null, optAnswerState = null;
            int num_of_states_created = 0;
            int num_of_states_explored = 0;
            int numOfStates = 0;

            logger.debug("New heuristic search reusing states starts...");
            Q.clear();

            double lowerBound = curAnswerState.score;   // keep the score of the best sub-optimal div-k answer found in Tops
            for (State st : St) {
            	st.ub = st.score + (Params.K-st.answer.size())*relOfLastTreeInTops;
            	if (st.ub > lowerBound) {
            		Q.add(st);            
            	}
            }
	        maxNumOfStates = numOfStates = Q.size();

            St.clear();
            
            while ((s = Q.poll()) != null) {
                    numOfStatesExplored++;
                    num_of_states_explored++;
                    numOfStates--;
//                  logger.debug("Q.poll(): " + s);

                    if (s.answer.size() == Params.K) {
                            logger.debug("k size answer found: {}", s);
                            logger.debug("s.score: {}, ub_unseen: {}", s.score, ub_unseen);

                            curAnswerState = s;	// a new better answer found and update curAnswerState
                            break;
                    }
                    
                    if (s.pos == lastIndexOfTops-1) {
                    	AnswerTree t = Tops.get(lastIndexOfTops);
                        double newSumDiss = checkDissCondition(s.answer, t, s.sumDiss);
                        if (newSumDiss >= 0) { // diss-condition is satisfied!
	                        State e = new State(s.answer, t, s.score + t.score, lastIndexOfTops, newSumDiss);
	                        e.ub = s.ub;
	                        Q.add(e);
	                        
	                        numOfStatesCreated++;
                            num_of_states_created++;
                            numOfStates++;
                            if (numOfStates > maxNumOfStates) maxNumOfStates = numOfStates;
                        }
                        else {	
                            if (s.ub > ub_unseen) ub_unseen = s.ub;
                            s.pos = lastIndexOfTops;	// all trees in Tops have been considered for deriving states from s
                            St.add(s);
                        }
                    }
                    else {	// s.pos == lastIndexOfTops
                        if (s.ub > ub_unseen) ub_unseen = s.ub;
                        St.add(s);
                    }
            }    
            
            if (curAnswerState.score >= ub_unseen) {
            	optAnswerState = curAnswerState;
                logger.debug("optimal answer found!");
            }
            else {
        		optAnswerState = null;
            	logger.debug("a new local answer found!");
            }
            logger.debug("ub_unseen={} ", ub_unseen);
            logger.debug("created:{}, explored(removed):{}", num_of_states_created, num_of_states_explored);
            logger.debug("maxQSize:{}", maxNumOfStates);
            return optAnswerState;
        }
        
        @Override
        protected State heuristicSearch(boolean finalRound) { return null; }
 
        @Override
        public void completeSearch() {
            St.clear();
			curAnswerState = null;	
            super.completeSearch();
		}
}

