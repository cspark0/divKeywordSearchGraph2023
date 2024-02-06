package queryProcessing;

import util.*;

import java.util.List;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;


public class Astar2 extends AstarSearcher {

	float lowerBound = 0;	// keep the score of the best sub-optimal div-k answer found in Tops

	public Astar2() {
	}

	public Astar2(DirectedGraph<Integer, DefaultEdge> g) {
		super(g);
	}
		
	@Override
	protected boolean prepareSearch(List<String> query) {
		if (super.prepareSearch(query) == false)
			return false;
		lowerBound = 0;	
		return true;
	}
	
	@Override
	protected State heuristicSearch() { return null;} 
 
	@Override
	protected State heuristicSearch(boolean finalRound) {
		int lastIndexOfTops = Tops.size()-1;
		float relOfLastTreeInTops = Tops.get(lastIndexOfTops).score;
		float ub_unseen = 0;
		State s = null, answerFound = null;
		int num_of_states_created = 0;
		int num_of_states_explored = 0;
		int numOfStates = 0;

//		logger.debug("New heuristic search starts...");
		H.clear();
		H.add(new State());
		numOfStatesCreated++;
		num_of_states_created++;  

		while ((s = H.poll()) != null) {
			numOfStatesExplored++;
			num_of_states_explored++;
			numOfStates--;
			logger.debug("H.poll(): " + s.score + ", " + s.ub);
			
			if (s.answer.size() == Params.K) {
				logger.debug("k size answer: {}", s);
				logger.debug("s.score: {}, ub_unseen: {}", s.score, ub_unseen);
				if (finalRound || 
					s.score >= ub_unseen) { 
					answerFound = s; 
					logger.debug("optimal answer found!");
				}
				else {
					lowerBound = s.score; 
					logger.debug("New lowerBound: " + lowerBound); 
				}
				break; 
			}
			else if (s.pos == lastIndexOfTops) {
//				logger.debug("incomplete answer=" + s);
				//////////////////////
				// need to comment out the below line
				break;
				//////////////////////
			}

			boolean s_was_expanded = false;
			for (int i = s.pos + 1; i <= lastIndexOfTops; i++) {
				AnswerTree t = Tops.get(i);
				float newSumDiss = checkDissCondition(s.answer, t, s.sumDiss);
				if (newSumDiss >= 0) { // diss-condition is satisfied!
					// State e = new State(s.answer, t, s.score + t.score, i, newSumDiss);
					float e_ub = 0;
					// 
					// start of computeUpperBound(e);
					if (s.answer.size() + 1 == Params.K) {
						e_ub = s.score + t.score;
					}		
					else {
						float ub = s.score + t.score;
						int first = i + 1;
						int last = i + Params.K - (s.answer.size()+1);
						if (last <= lastIndexOfTops) {
							for (int j = first; j <= last; j++)
								ub += Tops.get(j).score;
						}
						else {	// last > lastIndexOfTops
							for (int j = first; j <= lastIndexOfTops; j++)
								ub += Tops.get(j).score;
							ub += (last - lastIndexOfTops) * relOfLastTreeInTops;
						}
						e_ub = ub;

					}
					// end of computeUpperBound(e);
					//
					//
							
					if (e_ub >= lowerBound) {	// it is possible to find a better div-k answer
					 	State e = new State(s.answer, t, s.score + t.score, i, newSumDiss);
						e.ub = e_ub;
						H.add(e);
						s_was_expanded = true;
						numOfStatesCreated++;
						num_of_states_created++;  
						numOfStates++;
						if (numOfStates > maxNumOfStates)
							maxNumOfStates = numOfStates;
						logger.debug("New state created: " + e.score + ", " + e.ub);
					}
					else {
						logger.debug("e.ub " + e_ub +" < lowerBound " + lowerBound + " so break!");
				       	break;		// all remaining child states has ub smaller than lowerBound!
					}
				}
			}
			if (s_was_expanded == false) {
				float ub_unseen_with_s = s.score + (Params.K - s.answer.size()) * relOfLastTreeInTops;
				// if (ub_unseen_with_s >= lowerBound &&
				if (ub_unseen_with_s > ub_unseen) {
					ub_unseen = ub_unseen_with_s;
					// update ub-unseen by upper bound of the score of a set of div k answer trees consisting of s.answer and unseen trees outside Tops
					logger.debug("New ub_unseen: " + ub_unseen); 
				}
			}
		}
		logger.debug("ub_unseen={} ", ub_unseen);
		if (s != null) {
			logger.debug("s.score:{}, s.pos:{}", s.score, s.pos);
			logger.debug("lastIndexOfTops:{}", lastIndexOfTops);
		}
		logger.debug("created={}, explored(removed)={}", num_of_states_created, num_of_states_explored);
		return answerFound;
	}
	
}
