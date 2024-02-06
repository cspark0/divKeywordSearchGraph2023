package index.mondial;

import nkmap.bdb.NKMapDatabasePut;
import util.Params;
import index.IndexBuilder;

public class buildIndex {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Params.setExpDB("mondial");
		IndexBuilder ib = new IndexBuilder();
		// country, politics, city, province,  religion, continent, lake, river, sea, organization, mountain

		// field[8-9]: for organization, field[10-12]: for mountain, field[13-15]: for island, field[16-17]: for lake 
        // country, politics,    city,    province, religion, continent, river,  sea,    organization,           mountain,                  island,          lake
		//static final int prefix[]   = {20000,  60000,        40000,   30000,    70000,    10000,     90000,  100000, 5000, 0,                0, 0, 0,                   110000, 0, 0,    8000, 0};
//		String field[] = {"name", "government", "name",  "name",   "name",   "name",    "name", "name", "name","abbreviation", "name","mountains","type", "name","islands","type", "name","type"};
		
//		ib.createTables();
		
		System.out.println("start processing...");
		ib.luceneIndexToDB("text", Params.ExpDB + "/index-mondial");	
		System.out.println("successfully finished.");
		
		// here, we must create index on kns(srcNode) and nnr(endNode)  
		
		System.out.println("populate KeywordNodeNodeRel table...");
		ib.computeKeywordNodeNodeRel();
		System.out.println("successfully finished.");
		
		// here, we must create index on kns(keyword) and knnr(keyword)

		System.out.println("build term list file...");
		ib.buildTermListFile();
		System.out.println("successfully finished.");		
		
//		System.out.println("create KNNR2 table...");
//		//ib.reorderKNNRintoKNNR2();
//		System.out.println("successfully finished.");
		
		System.out.println("create inverted index and NKMap...");
		NKMapDatabasePut.buildIndexAndNKMapForBlinks();
		System.out.println("successfully finished.");

		/*		System.out.println("build basic index...");
		ib.buildBasicIndex();			
		System.out.println("successfully finished.");
		
		System.out.println("build enhanced index...");
////		ib.buildEnhancedIndex();
		System.out.println("successfully finished.");
	
		NKMapDatabasePut.buildNKMapForBlinks();
*/	}
}
