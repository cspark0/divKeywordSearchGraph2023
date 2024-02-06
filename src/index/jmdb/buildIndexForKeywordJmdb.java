package index.jmdb;

import nkmap.bdb.NKMapDatabasePut;
import nkmap.bdb.NKMapDatabasePutForJmdb;
import util.Params;
import index.IndexBuilder;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Set;
import java.sql.*;


public class buildIndexForKeywordJmdb {

    /**
     * @param args
     */
    public static void main(String[] args) {

        String[] selectedKeywords = { 
/*
            "politics", "president", "government", "revenge", "conflict", "religion", "sports", "terror", "summer", "winter", "fbi", "myth", "legend", "worrior", "emperor", "peace", "dispute", "escape", "spirit", "soul", "spy", "hope", "life", "sacrifice", "rescue", "competition", "freedom",

            "ridley", "russell",
              "digital", "world", "american", "animation",
                "robot", "scifi", "fantasy", "cameron", 
                "academy", "award", "drama", "history",
                "woody", "cowboy", "animation",
                "ethan", "hunt", "tom", "cruise",
                "bale", "nolan", "thriller", "germany", "war", "trial", 
                "actress", "oscar", 
                "dancer", "hitchcock", "mystery", "wizard", "magic", "school",
                "hero", "crime", "adventure",
                "woody", "allen", "cat", "animal", "toy", 
                "disaster", "accident", "explosion", "invasion",
                "pixar", "disney", "batman", "psycho",
                "romantic", "comedy", "diaz",
                "war", "hanks", "christian", 
                "attack", "fighter", "warship", "aircraft", "spaceship", 
                "crash", "science", "fiction", "machine", "human", "love", 
                "affair", "woman", "friendship", "marriage", "soldier", 
                "soldiers", "troops", "battle", "family", "tragedy", "time", 
                "travel", "future", "music", "dance", "artist", "musician", 
                "vampire", "teenage", "girl", "romance", "murder", "violence", 
                "mystery", "magician", "sorcerer", "earth", "ocean", "sea", 
                "nature",  "race", "return", "brad", "cate", "military", 
                "planet", "mars", "moon", "play", "heroine", "collapse", 
                "blast", "combat", "king", "witch", "journey", "trip", 
                "train", "ship", "explorer", "arctic", "antarctic", "airplane",
                 "space", "earthquake", "flood", "storm", "mountain", 
                "natural", "space", "alien", "cage", "intrusion", "kill",
                "army", "suicide", "nicholas", "vietnam", "russel", "nuclear", 
                "weapon", "ghost", "monster", "fight", "police", "chase", 
                "criminal", "fugitive", "detective", "scene", "fairy", "tale", 
                "legendary", "story", "giant", "elf", "dwarf", "enemy" 
*/
/*
		 "mars", "earth", "ocean", "sea", "crash", "return", "space", "explosion"
		"hitchcock", "mystery", "thriller", "horror","suspense", "panic",
"crash", "explosion", "escape", "disaster", "rescue",
"hero","survival","destruction","gangster", "fight", "crime", "murder", "trial", "violence", "police",
"criminal", "spy", "terrorist", "chase", "deception", "betrayal",
"warrior", "war", "battle", "army", "military","killer", "ship", "soldier", "attack", "invasion", "massacre", "weapon", "aircraft", "fighter", "rocket",
"sea", "ocean", "return", "space", "earth", "moon", "mars", "star", "planet", "time", "travel", "future","spaceship",
"western", "cowboy", "sheriff", "gunman", "action", "superhero","adventure",
"history", "historical", "rome", "empire", "emperor", "king", "conquest", "epic", "independence", "colony",
"award", "academy", "oscar",
"crowe", "russell", "tom", "cruise", "hanks", "morgan", "freeman", "matt", "damon", "leonardo", "dicaprio",
"tragedy","revenge", "reality", "nonfiction", "non-fiction",
"elf", "dwarf", "fantasy", "vampire", "alien","ghost", "zombie", "monster", "devil","human","miracle",
"political", "politics", "drama", "romance","romantic", "comedy", "christmas", "sacrifice",
"musical", "love", "success", "happiness","friendship","relationship","religion", "justice", "conflict",
"animation", "family", "kids", "animal", "knight", "beast", "prince", "princess", "sport", "sf", "sci-fi", "robot", "machine", "dystopia"};
		*/
		//"noir", "wedding", "apocalypse", "past", "nuclear"};
//		"roman", "europe"};
//		"conspiracy", "corruption", "life", "crisis", "discrimination", "racism", "secret", "villain", "disclosure", "hostage", "negotiation", "trip", "terrorism", "anti-war", "twist", "emergency", "landing"};
//"marriage","fraud", "scam", "company", "business", "kidnap", "missing", "psycho", "psychopath" };
//"mission","galaxy", "universe" };
//"genocide", "rebellion"};
// "revolt", "uprising"};
 //"revolution"};
 "kingdom", "monarchy", "overthrow"};
        Params.setExpDB("jmdb");
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
        NKMapDatabasePutForJmdb.buildIndexAndNKMapForBlinks(selectedKeywords);
        
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
