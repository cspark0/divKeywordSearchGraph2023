package index.dblp;

import nkmap.bdb.NKMapDatabasePutForDblp;
import util.Params;
import index.IndexBuilder;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Set;
import java.sql.*;


public class buildIndexForKeyword {

    public static void main(String[] args) {

        String[] selectedKeywords = { 
			"keyword", "search", "graph", 
			"embedding", "link", "prediction", 
			"query", "optimization", "semantic", "rewriting", 
			"selectivity", "estimation", "heuristic"
			};

        IndexBuilder ib = new IndexBuilder();
        
        String[] a = new String[1];
        Set<String> s = new TreeSet<String>();
        for (int i = 0; i < selectedKeywords.length; i++) 
            s.add(selectedKeywords[i]);
        Set<String> s2 = new TreeSet<String>(); 
        Iterator<String> iter = s.iterator();
        while (iter.hasNext()) {
            String ss= iter.next();
//            if (ss.compareTo("fighter") <=0 ) continue;
//            if (ss.compareTo("journey") >= 0) continue;
            s2.add(ss);
        }
        Params.setExpDB("dblp");

        String[] ks = s2.toArray(a); 
        for (int i = 0; i < ks.length; i++) 
        {   System.out.print(ks[i] + ", ");
        //    NKMapDatabasePut.deleteFromNKMap(ks[i], 100000000);
            
        }        

//        deleteTables(ks);
        System.out.println("create KeywordNodeNodeRel table for " +ks);
//        ib.computeKeywordNodeNodeRel(ks);
        System.out.println("successfully finished.");
        
        System.out.println("create inverted index and NKMap...");
        //NKMapDatabasePutForJmdb.buildIndexAndNKMapForBlinks(ks);
        //NKMapDatabasePutForJmdb.buildIndexAndNKMapForBlinks(args[0]);
        NKMapDatabasePutForDblp.buildIndexAndNKMapForBlinks(selectedKeywords);
        
        System.out.println("successfully finished.");

    }

    public static void deleteTables(String[] ks)  {
        String sql = "drop table KNNR_"; 
        Connection conn = null;
//        Params.setExpDB("jmdb");
 		try {
 			Class.forName(Params.jdbc_driver);
			conn = DriverManager.getConnection(Params.jdbc_url, Params.mysql_user, Params.mysql_pw);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
            
        try {
		   Statement stmt = null;
            for (int i = 0; i < ks.length;i++) {
			    stmt = conn.createStatement();
			    stmt.executeUpdate(sql + ks[i]); 
			    stmt.close();
            }
//			stmt = conn.createStatement();
//			stmt.executeUpdate("create index ind_nnr2_endNode on NNR2(endNode)"); 
        } catch (Exception e) {
            System.out.println("Exception when processing keyword ");
            e.printStackTrace();
        } finally {
            try { conn.close(); } catch(Exception e) {
 			e.printStackTrace();
            }
        }
    }

}
