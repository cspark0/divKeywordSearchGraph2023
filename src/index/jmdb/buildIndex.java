package index.jmdb;

import nkmap.bdb.NKMapDatabasePut;
import util.Params;
import index.IndexBuilder;

public class buildIndex {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Params.setExpDB("jmdb");
		IndexBuilder ib = new IndexBuilder();
		
//		ib.createTables();
		
		System.out.println("start processing...");
//		ib.luceneIndexToDB("text", "res\\lusql\\index-imdb");	
//		ib.luceneIndexToBerkelyDB("text", "res\\lusql\\index-imdb");	
		System.out.println("successfully finished.");
		
		System.out.println("populate KeywordNodeNodeRel table...");
//		ib.computeKeywordNodeNodeRel();
		System.out.println("successfully finished.");
		
		System.out.println("build term list file...");
//		ib.buildTermListFileAndKNNRFiles();
//		ib.buildTermList(); 
		System.out.println("successfully finished.");		

		System.out.println("populate KeywordNodeNodeRel table...");
//		ib.buildNKMap();
		System.out.println("successfully finished.");

		System.out.println("create secondary index for nkmap...");
//		ib.create2ndIndexForNKMap();
		System.out.println("successfully finished.");

		System.out.println("Create inverted list...");
//		ib.buildBLINKSInvertedList();
		System.out.println("successfully finished.");
		
		System.out.println("create inverted index and NKMap...");
		NKMapDatabasePut.buildIndexAndNKMapForBlinks();
		System.out.println("successfully finished.");

	}
}
