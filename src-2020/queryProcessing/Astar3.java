package queryProcessing;

import util.*;
import java.util.List;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class Astar3 extends AstarSearcher {

	public Astar3() {
	}

	public Astar3(DirectedGraph<Integer, DefaultEdge> g) {
		super(g);
	}
		
	@Override
	protected boolean prepareSearch(List<String> query) {
		if (super.prepareSearch(query) == false)
			return false;
		return true;
	}
	
	@Override
	protected State heuristicSearch() { return null;} 
 
	protected State heuristicSearch(boolean finalRound) {
		int lastIndexOfTops = Tops.size()-1;
		float relOfLastTreeInTops = Tops.get(lastIndexOfTops).score;
		State s = null, answerFound = null;

		System.out.println("New heuristic search starts...");
		H.clear();
		H.add(new State());
		numOfStatesCreated++;

		while ((s = H.poll()) != null) {	
			numOfStatesExplored++;
			
			if (s.answer.size() == Params.K) {  	// s is a goal state!
				answerFound = s; 
				break;			// an optimal goal state (i.e., an optimal div top-k answer in G) has been found in Tops by A* 
			}
			else if (s.pos == lastIndexOfTops) // need to consider unseen answer trees beyond Tops
				break;		// no optimal div top-k answer trees in Tops
			
			int i = s.pos + 1;
			while (i <= lastIndexOfTops) {
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
					H.add(e);
					numOfStatesCreated++;
					break;		 // at most one child of s should be created
				}
				i++;
			}
			if (i > lastIndexOfTops) i--;
			s.pos = i;
					//
					// 
					// start of computeUpperBound(s);
					float ub = s.score;
					int first = s.pos + 1;
					int last = s.pos + Params.K - s.answer.size();
					if (last <= lastIndexOfTops) {
						for (int j = first; j <= last; j++)
							ub += Tops.get(j).score;
					}
					else {	// last > lastIndexOfTops
						for (int j = first; j <= lastIndexOfTops; j++)
							ub += Tops.get(j).score;
						ub += (last - lastIndexOfTops) * relOfLastTreeInTops;
					}
					s.ub = ub;
					// end of computeUpperBound(s);
					//
					//
			H.add(s);		// s is updated and inserted into H again
		}
		if (s != null) {
			System.out.print("s.score="+s.score + ", " + "s.pos="+s.pos + ": " + lastIndexOfTops);
		}
		System.out.println();
		return answerFound;
	}
}
