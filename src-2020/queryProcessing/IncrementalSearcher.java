package queryProcessing;

import util.*;
import java.util.List;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class IncrementalSearcher extends DivTopKAstarSearcher {
	public IncrementalSearcher() {}

	public IncrementalSearcher(DirectedGraph<Integer, DefaultEdge> g) {
		super(g);
	}

	@Override
	public void search(List<String> query) { 	// for incremental top-k search (not div top-k)
		
		if (prepareSearch(query) == false) return; // null;
		
		AnswerTree t = null;		
		while (Tops.size() < Params.K) {
			if ((t = nextTopAnswer(query)) == null) {
				System.out.println("less than K answer trees are found!");
				break; 
			}
			Tops.add(t);
		}
		
		float score = 0;
		float sumDiss = 0;
		for (int i = 0; i < Tops.size(); i++) {
			score += Tops.get(i).score;
			for (int j = 0; j < i; j++) {
				sumDiss += computeDissimilarity(Tops.get(i).srcNodes, Tops.get(j).srcNodes);				
			}
		}
		getResult(new State(Tops, score, sumDiss));
	}

	@Override
	protected State heuristicSearch() {
		return null;
	}
	
}
