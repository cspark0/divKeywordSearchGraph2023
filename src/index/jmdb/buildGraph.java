package index.jmdb;

import util.Params;

public class buildGraph {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Params.setExpDB("jmdb");
		JMDBGraphBuilder gb = new JMDBGraphBuilder();
		//JMDBWeightedGraphBuilder gb = new JMDBWeightedGraphBuilder();
		int v =0, e=0;

		int start = Integer.parseInt(args[0]);
		int end = Integer.parseInt(args[1]);
		System.out.println(start + ","  + end);

		System.out.println("processing mov...");
		gb.createMovies();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing actors...");
		gb.createActors();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing mov2act...");
		gb.createMov2ActRelation();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing dir...");
		gb.createDirectors();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing mov2dir...");
		gb.createMov2DirRelation();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

/*
		System.out.println("processing AkaTitles...");
		gb.createAkaTitles();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());
*/
		System.out.println("processing Genres...");
		gb.createGenres();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing mov2genres...");
		gb.createMov2Genres();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing plots...");
		gb.createPlots();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing MovieLinks...");
		gb.createMovieLinks();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing Keywords...");
		gb.createKeywords();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("processing mov2keywords...");
		gb.createMov2Keywords();
		System.out.println(gb.getVertexSetSize());
		System.out.println(gb.getEdgeSetSize());

		System.out.println("successfully finished.");
		
		gb.computeNodeNodeRel(start, end);	// create NMR.txt file	
//		gb.computeNodeNodeRel(1000001, 2000000);	// create NMR.txt file	
		// then, we must load NMR.txt file to NMR table in MySQL manually
		// before executing buildIndex.class
		System.out.println("successfully finished.");

		gb.close();
	}
}
