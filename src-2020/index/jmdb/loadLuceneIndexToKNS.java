package index.jmdb;

import index.IndexBuilder;
import util.Params;

public class loadLuceneIndexToKNS {
	public static void main(String[] args) {
		Params.setExpDB("jmdb");
		final String luceneIndexDir = Params.ExpDB + "/index-jmdb";
		IndexBuilder ib = new IndexBuilder();
		
		System.out.println("start processing...");
		ib.luceneIndexToDB("text", luceneIndexDir);	
		System.out.println("successfully finished.");
		
		//System.out.println("build term list file...");
		// ib.buildTermList(); 
		//System.out.println("successfully finished.");		

	}
}
