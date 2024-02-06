package index.mondial;

import index.IndexBuilder;
import util.Params;

public class loadLuceneIndexToKNS {
	public static void main(String[] args) {
		Params.setExpDB("mondial");
		final String luceneIndexDir = Params.ExpDB + "/index-mondial2";
		IndexBuilder ib = new IndexBuilder();
		
		System.out.println("start processing...");
		ib.luceneIndexToDB("text", luceneIndexDir);	
		System.out.println("successfully finished.");
		
		System.out.println("build term list file...");
		ib.buildTermListFile(); 
		System.out.println("successfully finished.");		

	}
}
