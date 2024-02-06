package queryProcessing;

import util.*;

import java.util.List;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;


public class Astar2m extends AstarSearcher {

	float lowerBound = 0;	// keep the score of the best sub-optimal div-k answer found in Tops

	public Astar2m() {
	}

	public Astar2m(DirectedGraph<Integer, DefaultEdge> g) {
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

		logger.debug("New heuristic search starts...");
		H.clear();
		H.add(new State());
		numOfStatesCreated++;

		while ((s = H.poll()) != null) {
			numOfStatesExplored++;
//			logger.debug("H.poll(): " + s.score + ", " + s.ub);
			
			if (s.answer.size() == Params.K) {
				if (s.score >= ub_unseen) {
					answerFound = s; 
				}
				else {
					lowerBound = s.score; 
//					logger.debug("New lowerBound: " + lowerBound); 
				}
				break; 
			}
			// else if (s.pos == lastIndexOfTops) break;
			///
			//************************************************ 
			//* NOTE: Remove of the above line is the only difference of Astar2m and Astar2! 
			//************************************************ 
			//

			boolean s_was_expanded = false;
			for (int i = s.pos + 1; i <= lastIndexOfTops; i++) {
				AnswerTree t = Tops.get(i);
				float newSumDiss = checkDissCondition(s.answer, t, s.sumDiss);
				if (newSumDiss >= 0) { // diss-condition is satisfied!
					State e = new State(s.answer, t, s.score + t.score, i, newSumDiss);
					//
					// 
					// start of computeUpperBound(e);
					if (e.answer.size() == Params.K) {
						e.ub = e.score;
					}		
					else {
						float ub = e.score;
						int first = e.pos + 1;
						int last = e.pos + Params.K - e.answer.size();
						if (last <= lastIndexOfTops) {
							for (int j = first; j <= last; j++)
								ub += Tops.get(j).score;
						}
						else {	// last > lastIndexOfTops
							for (int j = first; j <= lastIndexOfTops; j++)
								ub += Tops.get(j).score;
							ub += (last - lastIndexOfTops) * relOfLastTreeInTops;
						}
						e.ub = ub;
					}
					// end of computeUpperBound(e);
					//
					//
					if (e.ub >= lowerBound) {	// it is possible to find a better div-k answer
						H.add(e);
						s_was_expanded = true;
						numOfStatesCreated++;
//						logger.debug("New state created: " + e.score + ", " + e.ub);
					}
					else {
//						logger.debug("e.ub " + e.ub +" < lowerBound " + lowerBound + " so break!");
					       	break;		// all remaining child states has ub smaller than lowerBound!
					}
				}
			}
			if (!finalRound && s_was_expanded == false) {
				float ub_unseen_with_s = s.score + (Params.K - s.answer.size()) * relOfLastTreeInTops;
				if (ub_unseen < ub_unseen_with_s) {
					ub_unseen = ub_unseen_with_s;
					// update ub-unseen by upper bound of the score of a set of div k answer trees consisting of s.answer and unseen trees outside Tops
//					logger.debug("New ub_unseen: " + ub_unseen); 
				}
			}
		}
		logger.debug("ub_unseen:{}", ub_unseen);
		if (s != null) {
			logger.debug("s.score:{}, s.pos:{}", s.score, s.pos);
			logger.debug("lastIndexOfTops:{}", lastIndexOfTops);
		}
		return answerFound;
	}

}
