package index.dblp;

import util.Params;

public class buildGraph {
	public static void main(String[] args) {
        String[] selectedKeywords = { 
			"keyword", "search", "graph", 
			"embedding", "link", "prediction", 
			"query", "optimization", "semantic", "rewriting", 
			"selectivity", "estimation", "heuristic"
		};

		Params.setExpDB("dblp");
		DBLPGraphBuilder gb = new DBLPGraphBuilder();
		//JMDBWeightedGraphBuilder gb = new JMDBWeightedGraphBuilder();
		int v =0, e=0;

//		int start = Integer.parseInt(args[0]);
//		int end = Integer.parseInt(args[1]);
		//System.out.println(start + ","  + end);

		System.out.println("processing papers...");
		gb.createPapers();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing authors...");
		gb.createAuthors();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing venues...");
		gb.createVenues();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing paper2authors...");
		gb.createPaper2AuthorRelation();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing paper2venues...");
		gb.createPaper2VenueRelation();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing citations...");
		gb.createCitations();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("successfully finished.");
		
		gb.computeNodeNodeRel(selectedKeywords);	// create NMR.txt file	
		//gb.computeNodeNodeRel(start, end);	// create NMR.txt file	
		// then, we must load NMR.txt file to NMR table in MySQL manually
		// before executing buildIndex.class
		System.out.println("successfully finished.");

		gb.close();
	}
}
