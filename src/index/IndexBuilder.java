package index;

import util.*;

//import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
//import java.io.FileReader;
import java.io.FileWriter;
import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

//import nkmap.bdb.NodeKeyword;
//import nkmap.bdb.RelSource;

import nkmap.bdb.RelSourceFirst;
import nkmap.bdb.RelSourceFirstBinding;
import nkmap.bdb.KeywordRel;
import nkmap.bdb.KeywordRelBinding;
import nkmap.bdb.MyDbEnv;
import nkmap.bdb.KeywordNode;
import nkmap.bdb.KeywordNodeBinding;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.ParallelReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryCursor;

//import com.sleepycat.je.Database;

// 클래스 선언
public class IndexBuilder {
	
	static final String termListFile = Params.ExpDB + "/termlist.bin";
	
	// 데이터베이스 연결관련 변수 선언
	static Connection conn = null;
	PreparedStatement pstmt = null, pstmt2 = null;
	Statement stmt = null;
	ResultSet rs = null;
	DatabaseEntry theKey = null, secKey = null, theData = null;
	TupleBinding<KeywordNode> keyBinding = null;
    TupleBinding<RelSourceFirst> dataBinding = null;
    TupleBinding<KeywordRel> secKeyBinding = null;
    
    // for KNS DB
    TupleBinding<Integer> knsKeyBinding = null;
    TupleBinding<KeywordRel> knsDataBinding = null;
    
    KeywordNode kn; RelSourceFirst relSrc; KeywordRel kr;
    MyDbEnv myDbEnv = null;
    Database db1 = null, db2 = null; 
//    HashMap<String, PrintStream> knnrPs = null;
    
	public IndexBuilder() {		
		theKey = new DatabaseEntry();
	    secKey = new DatabaseEntry();
	    theData = new DatabaseEntry();
	    
	    keyBinding = new KeywordNodeBinding();
	    dataBinding = new RelSourceFirstBinding();
	    secKeyBinding = new KeywordRelBinding();
	    
	    knsKeyBinding = new IntegerBinding();
	    knsDataBinding = new KeywordRelBinding();
	    
	    kn = new KeywordNode();
	    relSrc = new RelSourceFirst();            
		kr = new KeywordRel();
		
		myDbEnv = new MyDbEnv(); 		
	}
	
	// 데이터베이스 연결 메서드
	void connect() {
		// JDBC 드라이버 로드
		try {
			Class.forName(Params.jdbc_driver);
			// 데이터베이스 연결정보를 이용해 Connection 인스턴스 확보
			conn = DriverManager.getConnection(Params.jdbc_url, Params.mysql_user, Params.mysql_pw);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// 데이터베이스 연결 종료 메서드
	void disconnect() {
		if(rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(pstmt != null) {
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} 
		if(pstmt2 != null) {
			try {
				pstmt2.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void createTables() {
		try {
			connect();
			Statement stmt = conn.createStatement();
			stmt.executeUpdate( 
				"create table NNR2			" +	// endNode로 정렬
				"select endNode, startNode, firstNode, rel " +
				"from NNR " +
				"order by endNode");
				// "order by endNode, startNode");
			System.out.println("table NNR2 was constructed.");
			stmt.close();
//			stmt = conn.createStatement();
//			stmt.executeUpdate("create index ind_nnr2_endNode on NNR2(endNode)"); 
//			System.out.println("index on NNR2(endNode) was constructed.");			
//			stmt.close();
			stmt = conn.createStatement();
			stmt.executeUpdate( 
				"create table KNS0 					" +
				"(keyword varchar(30) not null, 	" +
				" srcNode mediumint unsigned not null, " +
				" score float)						"); 
			System.out.println("table KNS0 was constructed.");
			stmt.close();
			stmt = conn.createStatement();
			/*
			stmt.executeUpdate( 
				"create table KNNR						" +
				"(" + 		//	"id bigint unsigned NOT NULL auto_increment primary key, " +
				" keyword varchar(30),					" +
				" destNode mediumint unsigned,			" +
				" srcNode mediumint unsigned,			" +
				" firstNode mediumint unsigned,			" +
				" rel float) "							
			);
			*/
			System.out.println("table KNNR was constructed.");
//			stmt.executeUpdate("create index ind_nnr_endNode on NNR(endNode)"); 
//			System.out.println("index on NNR(endNode) was constructed.");
			stmt.close();			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void luceneIndexToDB(String fieldName, String indexDirName) {	// index to field[]
		File indexDir = new File(indexDirName); 
		
		if (!indexDir.exists()) {
			System.out.println(indexDir + " does not exist.");
		}
		if (!indexDir.isDirectory()) {
			System.out.println(indexDir + " is not a directory.");
		}

		int c = 0;
		Statement stmt = null; 
		try {
			Directory fsDir = new SimpleFSDirectory(indexDir);
			IndexReader ir = ParallelReader.open(fsDir);
			IndexSearcher is = new IndexSearcher(ir);
			Similarity sim = is.getSimilarity();
			System.out.println("sim="+sim);
			TermEnum terms = ir.terms();
/*			Collection<String> s = ir.getFieldNames(IndexReader.FieldOption.INDEXED);
			Iterator<String> it = s.iterator();
			Hashtable<String, byte[]> ht = new Hashtable<String, byte[]>();
			while (it.hasNext()) {
				String fieldName = it.next();
				byte[] norms = ir.norms(fieldName);
				ht.put(fieldName, norms);
			}
*/			int numDocs = ir.numDocs();
			System.out.println("numDocs = " + numDocs);
			
			connect();
			stmt = conn.createStatement();
			stmt.executeUpdate( 
				"create table KNS0 " +
				"(keyword varchar(30) not null, " +
				" srcNode mediumint unsigned not null, " +
				" score float)"); 
			System.out.println("table KNS0 was constructed.");
			stmt.close();

			pstmt = conn.prepareStatement(
				"insert into KNS0 values(?,?,?)");	// keyword, source, score
			byte[] termFieldNorms = ir.norms(fieldName);
			System.out.println("termFiNorms: "+ termFieldNorms);
/*			byte[] termFieldNorms2 = null;
			byte[] termFieldNorms3 = null;
			if (p == 8 || p == 10 || p == 13 || p == 16)
				termFieldNorms2 = ir.norms(field[p+1]);
			if (p == 10 || p == 13)
				termFieldNorms3 = ir.norms(field[p+2]);
			System.out.println("termFiledNorms for " + field[p] + " = " + termFieldNorms); 
			System.out.println("termFiledNorms for " + field[p+1] + " = " + termFieldNorms2);
*/				
			
			// byte norms[] = ir.norms(field);
			String idName = (indexDirName.contains("mondial")) ? "Id" : "id";
			System.out.println("idName: " +idName);
			String termText = "";
			while (terms.next()) {				
				Term t = terms.term();
				if (t.field().equals(idName)) {
					continue;
				}
				termText = t.text(); 			// keyword
				if (++c%10000 == 0) {
					System.out.println("termField: " + t.field());
					System.out.println("termText: " + termText);
				}
//				if (termText.compareTo("a") < 0 || termText.compareTo("zzzzzzzz") > 0 || termText.length() >= 30 ) continue; 
				if (!termText.matches("^[a-z]{1,29}$")) continue; 
				System.out.println("term = " + termText + ", field = " + t.field());
						
				float idf = (float)(Math.log(numDocs/(double)(terms.docFreq()+1)) + 1.0);
				
				TermDocs termDocs = ir.termDocs(t);	
				while (termDocs.next()) {
					int docNum = termDocs.doc();		// node ID	
					System.out.println("docNum: " + docNum);
					int termFreq = termDocs.freq();
				
					String id = ir.document(docNum).get(idName);
					System.out.println("id = " + id);
					float tf = (float)Math.sqrt(termFreq); 
					float norm = sim.decodeNormValue(termFieldNorms[docNum]);
					//float score = tf * idf * norm;

					/////////////////////////////////////////////////////
					// to avoid the same scores, add a small random number to score
					float score = (float)(tf * idf * norm + Math.random()/100000); // + 0.000001 ~ 0.000009
					/////////////////////////////////////////////////////

/*					if (p == 8 || p == 10 || p == 13 || p == 16)
						score = score * sim.decodeNormValue(termFieldNorms2[docNum]);	// right?
					if (p == 10 || p == 13)
						score = score * sim.decodeNormValue(termFieldNorms3[docNum]);	// right?
*/
//					System.out.println(termText + "docNum = " + docNum + ", id = " + id + ", tf = " + tf + ", idf = " + idf + ", norm = " + norm + ", score = " + score);
					// insert (termText, docNum, score) into table 
//					if (insertKeywordNodeScore(termText, QUOTETEXT_PREFIX + docNum, score) == false)
//						return false;
//					count++;					
					
					// 인자로 받은 GuestBook 객체를 통해 사용자 입력값을 받아 SQL 완성후 입력 처리
					pstmt.setString(1, termText);
					pstmt.setInt(2, Integer.parseInt(id)); 
					pstmt.setFloat(3, score);
					pstmt.executeUpdate();
//					System.out.println(k + " " + src + " " + score + "was inserted.");
				}			
				termDocs.close();
			}
			terms.close();
//			conn.commit();
			System.out.println("table KNS0 was constructed.");
		} catch (Exception e) {
			System.out.println("catch: failed to build KNS0");			
			e.printStackTrace();
		}

		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("create table KNS " + 
				"select * from KNS0 order by keyword, srcNode"); 
			System.out.println("table KNS was constructed.");
			stmt.close();			
			stmt = conn.createStatement();
			stmt.executeUpdate("drop table KNS0");
			stmt.close();
			stmt = conn.createStatement();
			stmt.executeUpdate("create index ind_kns_srcNode on KNS(keyword)");			
			stmt.close();
			System.out.println("index on KNS(keyword) was constructed.");
		} catch (Exception e) {
			System.out.println("catch: failed to build KNS or index");			
			e.printStackTrace();
		} finally {
			System.out.println("table KNS0 was dropped.");
			disconnect();
		}
	}
	
/*
	public void luceneIndexToBerkelyDB(String fieldName, String indexDirName) {	// index to field[]
		File indexDir = new File(indexDirName); 
		
		try {
        	myDbEnv.setup_kns(Params.kns, // path to the environment home
                    false);      // is this environment read-only?
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
        } 
		Database db_kns = myDbEnv.getKNSDB();

		int c = 0;
		
		if (!indexDir.exists()) {
			System.out.println(indexDir + " does not exist.");
		}
		if (!indexDir.isDirectory()) {
			System.out.println(indexDir + " is not a directory.");
		}
		try {
			Directory fsDir = new SimpleFSDirectory(indexDir);
			IndexReader ir = ParallelReader.open(fsDir);
				IndexSearcher is = new IndexSearcher(ir);
					Similarity sim = is.getSimilarity();
			TermEnum terms = ir.terms();
			int numDocs = ir.numDocs();
			System.out.println("numDocs = " + numDocs);		
			
			byte[] termFieldNorms = ir.norms(fieldName);
			String termText = "";
			while (terms.next()) {				
				if (++c%10000 == 0) System.out.println(termText);
				Term t = terms.term();
				if (t.field().equals("id")) {
					continue;
				}
				termText = t.text(); 			// keyword
				if (!termText.matches("^[a-z]{1,29}$")) continue; 
						
				float idf = (float)(Math.log(numDocs/(double)(terms.docFreq()+1)) + 1.0);
				
				TermDocs termDocs = ir.termDocs(t);	
				while (termDocs.next()) {
					int docNum = termDocs.doc();		// node ID	
					int termFreq = termDocs.freq();
				
					// String id = ir.document(docNum).get("id");
					int id = Integer.parseInt(ir.document(docNum).get("id"));
					float tf = (float)Math.sqrt(termFreq); 
					float norm = sim.decodeNormValue(termFieldNorms[docNum]);
					float score = tf * idf * norm;
									
					kr.set(termText, score);					
					knsKeyBinding.objectToEntry(id, theKey);
					knsDataBinding.objectToEntry(kr, theData);
					    
				    db_kns.put(null, theKey, theData);
				}			
				termDocs.close();
			}
			terms.close();
			
			System.out.println("KNS was constructed in BDB.");
		} catch (Exception e) {
			System.out.println("catch");			
			e.printStackTrace();
		} finally {
			System.out.println("finally");				
			myDbEnv.close_kns();
		}			
	}
*/	
	
	class NNREntry {
		public int s, e, f;
		public float rel;
		public NNREntry() { }
		void set(int s, int e, int f, float rel) {
			this.s = s;
			this.e = e;
			this.f = f;
			this.rel = rel;
		}
		@Override
		public String toString() {
			return (this.s + " " + this.e + " " + this.f + " " + this.rel);
		}
	}
	class HTEnt {
		public int e, f;
		public float rv;
		public HTEnt() { }
		HTEnt(int e, int f, float rv) {
			this.e = e;
			this.f = f;
			this.rv = rv;
		}
		HTEnt(HTEnt e2) {
			this.e = e2.e;
			this.f = e2.f;
			this.rv = e2.rv;
		}
		HTEnt set(int e, int f, float rv) {
			this.e = e;
			this.f = f;
			this.rv = rv;
			return this;
		}
		HTEnt set(HTEnt e2) {
			this.e = e2.e;
			this.f = e2.f;
			this.rv = e2.rv; return this;
		}
		void set(int e, float rv) {
			this.e = e;
			this.rv = rv;			
		}
	}	
	

/*
	class HTEntPool {
		private static final int poolSize = 2000000;
		private HTEnt[] entries = new HTEnt[poolSize]; 
		private int ind = 0;
		
		HTEntPool() { 
			for (int i = 0; i < entries.length; i++) 
				entries[i] = new HTEnt();
		}
		public HTEnt get(int e, int f, float rv) {
			return entries[ind++].set(e, f, rv);
		}
		public HTEnt get(HTEnt e) {
			return entries[ind++].set(e);
		}
		public void clear() {
			ind = 0;
		}
	}
	
	class HTEntPairPool {
		private static final int poolSize = 1000000;
		private HTEnt[][] entries = new HTEnt[poolSize][2]; 
		private int ind = 0;
		
		HTEntPairPool() {
			for (int i = 0; i < entries.length; i++) 
				entries[i] = new HTEnt[2];
		}
		public HTEnt[] get() {
			return entries[ind++];
		}
		public void clear() {
			ind = 0;
		}
	}
	
	HTEntPool htEntPool = new HTEntPool();
	HTEntPairPool htEntPairPool = new HTEntPairPool();
*/
	public void buildNKMap() {	

        System.out.println("Error!!!!!!!!!!!!!!!!! in buildNKMap()");

		Scanner scan = null;
		int startNode = -1;
		NNREntry nnr = new NNREntry();
		HashMap<String, HTEnt[]> ht = new HashMap<String, HTEnt[]>();
		

		try {
        	myDbEnv.setup(Params.nkmap, // path to the environment home
                    false);      // is this environment read-only?
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
        } 
		db1 = myDbEnv.getNKMapBlinkDB();
	    db2 = myDbEnv.getNKMapSubBlinkDB(); 
			
	    
//		System.out.println("db1.count = " + db1.count() + ", db2.count = " + db2.count());
//		if (1 > 0) return ;
		try {
			scan = new Scanner(new FileInputStream(Params.nmrFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		scan.useDelimiter("[,\n]");
	
	    try {
        	myDbEnv.setup_kns(Params.kns, // path to the environment home
                    true);      // is this environment read-only?
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
        }
		Database db_kns = myDbEnv.getKNSDB();
		Cursor cursor = null;
		OperationStatus retVal = null;
		
		HTEnt[] ent = null;
		String k = null;
		float rv = (float)0;
		        
/*		connect();	
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate( 
					"create table KNNR						" +
					"(" + 		//	"id bigint unsigned NOT NULL auto_increment primary key, " +
					" keyword varchar(30),					" +
					" destNode mediumint unsigned,			" +
					" srcNode mediumint unsigned,			" +
					" firstNode mediumint unsigned,			" +
					" rel float) "							
			);
			System.out.println("table KNNR was constructed.");
			stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			disconnect();
		}
		
*/	
/*
	    connect();	
		String sql = "select keyword, score from KNS2 where srcNode = ?";
			// KNS2 was sorted by srcNode
		try {
			pstmt = conn.prepareStatement(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
*/

		int i = 0;
		for (; i <= 367490000; i++) {
			scan.nextInt();scan.nextInt();scan.nextInt();scan.nextFloat();
		}
		while (true) {
			nnr.set(scan.nextInt(),scan.nextInt(),scan.nextInt(),scan.nextFloat());
			i++;
			if (nnr.s == 927367) {
				System.out.println(i-- + "*******" + nnr.s);
				break;
			}
		}

		cursor = db_kns.openCursor(null, null);

		while (true) {
//		while (scan.hasNext()) {
//			nnr.set(scan.nextInt(),scan.nextInt(),scan.nextInt(),scan.nextFloat());
			if (++i % 10000 == 0) { 
				System.out.println();
				System.out.println(i + ": " + nnr);
			}

			if (nnr.s != startNode) {	// new startNode was read				
//				System.out.println("startNode " + nnr.s + " read");
				if (startNode >= 0) {
					insertIntoNKMapAndKNNR(startNode, ht);	
					System.out.println("startNode " + startNode + " processed");
				}
				startNode = nnr.s;				
			}
			
			try {
				
//				pstmt.setInt(1, nnr.e);
//				rs = pstmt.executeQuery();			
//				System.out.println("KNS queried with end node " + nnr.e);
//				while(rs.next()) {
				
				knsKeyBinding.objectToEntry(nnr.e, theKey);
			    retVal = cursor.getSearchKey(theKey, theData, LockMode.DEFAULT);
//			    System.out.print("count: " + cursor.count());
			    while (retVal == OperationStatus.SUCCESS) {
			    	
			    	kr = knsDataBinding.entryToObject(theData); 
			    	k = kr.getKeyword();
			    	rv = kr.getRel() * nnr.rel;
//					k = rs.getString(1);
//					rv = rs.getFloat(2)*nnr.rel;
//			    	System.out.println("e, k, rv = " +  nnr.e + " " + k + " " + rv);			            
			    
					ent = ht.get(k);
					if (ent == null) {
						ent = new HTEnt[2];
						ent[0] = new HTEnt(nnr.e, nnr.f, rv);
						ht.put(k, ent);
					}
					else if (ent[1] == null) {	// only ent[0] exists
						if (rv > ent[0].rv) { 
							if (ent[0].f == nnr.f) {	// replace ent[0] by current ent
								ent[0].set(nnr.e, rv);							
							}
							else {	
								// create ent[1] and set by ent[0]					
								ent[1] = new HTEnt(ent[0]);		
								ent[0].set(nnr.e, nnr.f, rv);	// replace ent[0] by current ent
							}
						}
						else {
							if (ent[0].f != nnr.f) {		// create ent[1] with current ent
								ent[1] = new HTEnt(nnr.e, nnr.f, rv);
							}
						}
//						ht.put(k, ent);
					}
					else {	// both ent[0] and ent[1] are not null
						if (rv > ent[0].rv) {
							if (ent[0].f == nnr.f) {	// replace ent[0] by current ent
								ent[0].set(nnr.e, rv);							
							}
							else {	
								ent[1].set(ent[0]);		// replace ent[1] by ent[0]					
								ent[0].set(nnr.e, nnr.f, rv);	// replace ent[0] by current ent
							}
						}
						else if (rv > ent[1].rv) {
							if (ent[0].f != nnr.f) {	// replace ent[1] by current ent
								ent[1].set(nnr.e, nnr.f, rv);							
							} 
						}			
						// no need to put ent[] into HashTable???
					}		      
					
		            retVal = cursor.getNextDup(theKey, theData, LockMode.DEFAULT);    
			    }
//				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}			
			if(scan.hasNext()) 
				nnr.set(scan.nextInt(),scan.nextInt(),scan.nextInt(),scan.nextFloat());
			else break;
		}
		if (startNode >= 0) insertIntoNKMapAndKNNR(startNode, ht);			
//		disconnect();

	    cursor.close();

    	myDbEnv.close();

		scan.close(); 		
		
		// close PrintStreams of knnr files
/*		Collection<PrintStream> pss = knnrPs.values();
		Iterator<PrintStream> it = pss.iterator();
		while (it.hasNext()) it.next().close();
*/			
		// read records from each KNNR file and sort them by rel desc,
		// then store them into new file --> update knnrPs with new PrintStreams
		
		
		// build inverted indexes from new KNNR files !!!
		
/*		// build index on KNNR
		connect();
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("create index ind_knnr_keyword on KNNR(keyword)"); 
			System.out.println("index on KNNR(keyword) was constructed.");			
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disconnect();
		}	
			
		// sort KNNR by (keyword, rel desc) and then	
		// build inverted indexes from KNNR !!!
		String sql2 = "select keyword, destNode, srcNode, firstNode, rel " +
						"from KNNR " +
						"order by keyword, rel desc";
				// already sorted by keyword, rel desc 
		InvertedList curList0 = null;
		connect();
		try {
	        stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        stmt.setFetchSize(1000);
			rs = stmt.executeQuery(sql2);				
	
			int i = 0, dn, fn, sn;
			float rel;
			String k = null, oldk = null;
			
			while(rs.next()) {
				if (++i % 10000 == 0) System.out.println(k + ": " + i);
				k = rs.getString(1);
				dn = rs.getInt(2);
				sn = rs.getInt(3);
				fn = rs.getInt(4);
				rel = rs.getFloat(5);
				
				if (!k.equals(oldk)) {		// new keyword found
					if (curList0 != null) curList0.complete();
					curList0 = new InvertedList(k);
					curList0.openToBuild();
					oldk = k;
				}
				curList0.writeEntry(dn, fn, sn, rel); 	// insert into the index
			}
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (curList0 != null) curList0.complete();
			disconnect();
		}
*/
	}    			
    
	public void create2ndIndexForNKMap() {
    	myDbEnv.build2ndIndexForNKMap(Params.nkmap);	// secondary index to NKMap based on (keyword, rel desc)
	}
	
    public void insertIntoNKMapAndKNNR(int startNode, HashMap<String, HTEnt[]> ht) {
//    	connect();				
/*    	String sql = "insert into KNNR (keyword, destNode, srcNode, firstNode, rel) values (?, ?, ?, ?, ?) ";
		try {
			pstmt2 = conn.prepareStatement(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
*/
    	Set<Entry<String, HTEnt[]>> s = ht.entrySet();
		Iterator<Entry<String, HTEnt[]>> it = s.iterator();
		while (it.hasNext()) {
			Entry<String, HTEnt[]> entry = it.next();
			String k = entry.getKey();
			HTEnt[] ent = entry.getValue(); // assert(ent != null);
			
			// insertIntoNKMap(startNode, k, ent);			
			try {        	
	           	// put (startNode, k) -> (ent[0].e, ent[0].f, ent[0].rv) into NKMap
				kn.setDestNodeAndKeyword(k, startNode);	
				relSrc.set(ent[0].f, ent[0].e, ent[0].rv);
					
				// Convert the Vendor object to a DatabaseEntry object
			    // using our SerialBinding
			    keyBinding.objectToEntry(kn, theKey);
			    dataBinding.objectToEntry(relSrc, theData);
				    
			    // Put it in the database. These puts are transactionally protected
			    // (we're using autocommit).
			    // Stores the key/data pair into the database if the key does not already appear in the database. 
			    db1.putNoOverwrite(null, theKey, theData);
					// != OperationStatus.KEYEXIST
				if (ent[1] != null) {
					// also put (startNode, k) -> (ent[1].e, ent[1].f, ent[1].rv) into NKMapSub
					// nk.setDestNodeAndKeyword(k, startNode);	
					relSrc.set(ent[1].f, ent[1].e, ent[1].rv);
						
					// Convert the Vendor object to a DatabaseEntry object
				    // using our SerialBinding
				    // keyBinding.objectToEntry(nk, theKey);
				    dataBinding.objectToEntry(relSrc, theData);
				    db2.putNoOverwrite(null, theKey, theData);
				}
			} catch (Exception e) {
				myDbEnv.close();
				e.printStackTrace();
			}		
			// insertIntoKNNR(startNode, k, ent[0]);
/*			try {
				pstmt2.setString(1, k);
				pstmt2.setInt(2, startNode);
				pstmt2.setInt(3, ent[0].e);
				pstmt2.setInt(4, ent[0].f);
				pstmt2.setFloat(5, ent[0].rv);
				pstmt2.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
*/		
//			PrintStream ps = knnrPs.get(k);
//			ps.printf("%d,%d,%d,%f\n", startNode, ent[0].e, ent[0].f, ent[0].rv);
		}
//		System.out.println("db1.count = " + db1.count() + ", db2.count = " + db2.count());
//		disconnect();
		System.out.println("ht size = " + ht.size() + "\n");
		s.clear();
		ht.clear();
	}
    
    public void buildBLINKSInvertedList() {
    	try {
        	myDbEnv.setup(Params.nkmap, // path to the environment home
                    true);      // is this environment read-only?
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
        } 
	    // Open the secondary database cursor. 
    	SecondaryCursor secCursor = 
    		myDbEnv.open2ndDBforNKMap().openCursor(null, null);
    	String oldk = null;
//		int i = 0;
		InvertedList curList0 = null;

        // To iterate, just call getNext() until the last database record has 
        // been read. All cursor operations return an OperationStatus, so just
        // read until we no longer see OperationStatus.SUCCESS
        try { // always want to make sure the cursor gets closed
            while (secCursor.getNext(secKey, theKey, theData,
                LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            	KeywordRel kr = (KeywordRel)secKeyBinding.entryToObject(secKey);
            	KeywordNode kn = (KeywordNode)keyBinding.entryToObject(theKey);
            	RelSourceFirst rs = (RelSourceFirst)dataBinding.entryToObject(theData);
            	String k = kr.getKeyword();
            	
//				if (++i % 10000 == 0) {
//					System.out.println(i);
					System.out.println("kr: " + kr.getKeyword() + " " + kr.getRel());
                	System.out.println("kn: " + kn.getDestNode() + " " + kn.getKeyword());
					System.out.println("rs: " + rs.getRel() + ", " + rs.getSrcNode());
//				}
           		if (!k.equals(oldk)) {		// new keyword found
    				if (curList0 != null) curList0.complete();
    				curList0 = new InvertedList(k);
    				curList0.openToBuild();
    				oldk = k;
    			}
//    			curList0.writeEntry(nk.getDestNode(), rs.getFstNode(), rs.getSrcNode(), rs.getRel()); 	// insert into the index
    		}           
        } catch (Exception e) {
            System.err.println("Error on inventory cursor:");
            System.err.println(e.toString());
            e.printStackTrace();
        } finally {
        	if (curList0 != null) curList0.complete();
    		secCursor.close();
    		myDbEnv.close();
        }
    }

    
    public boolean computeKeywordNodeNodeRel() {		
//		String sql1 = "insert into KNNR (keyword, destNode, srcNode, firstNode, rel) " +
		String sql1 = "create table KNNR " +
						"select a.keyword, b.startNode, b.endNode, b.firstNode, a.score*b.rel AS relv  " +
						"from KNS a, NMR b where a.srcNode = b.endNode " +
//						"and a.keyword < 'g' " +
//					 	"order by a.keyword, b.startNode, relv desc ";
						"order by a.keyword, relv desc ";
//		String sql2 = "insert into KNNR (destNode, keyword, rel, srcNode) " +						// keyword, destNode, srcNode, rel
//						"select srcNode, keyword, score, srcNode from KNS";
//		--> unnecessary since NNR already has a self-relation for each node, i.e. (n, n, 1.0)
	
		connect();

		try {
			pstmt = conn.prepareStatement(sql1);
			pstmt.executeUpdate();
//			pstmt.close();
//			pstmt = conn.prepareStatement(sql2);	
//			pstmt.executeUpdate();
			System.out.println("knnr table populated.");
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		finally {
			disconnect();
		}
		return true;
	}	

    public boolean computeKeywordNodeNodeRel(String[] ks) {      
	String sql1 = "create table KNNR_1" +
       		 " select a.keyword, b.startNode As destNode, b.endNode AS srcNode, b.firstNode, a.score*b.rel AS rel  " +
                        "from KNS_1" + 
			 " a, NMR b where a.srcNode = b.endNode " +
                        "and a.keyword in (";
	String sql2 = ") order by a.keyword, rel desc ";

	String k = "";
	for (int i = 0; i < ks.length-1; i++) 
		k += ("'" + ks[i] + "', ");
	k += ("'" + ks[ks.length-1] + "'");
	System.out.println(k);
/*
	String sql1 = "create table KNNR_";
        String sql2 = " select b.startNode As destNode, b.endNode AS srcNode, b.firstNode, a.score*b.rel AS rel  " +
                        "from KNS_";
	String sql3 = " a, NMR b where a.srcNode = b.endNode " +
                      //  "and a.keyword = ? " +
                        "order by rel desc ";
*/
/*
    	String sql3 = "create table KNNR1 " +
    					"select a.keyword, b.startNode As destNode, b.endNode AS srcNode, b.firstNode, a.score*b.rel AS rel  " +
                        "from KNS a, NNR2 b where a.srcNode = b.endNode " +
                        "and a.keyword = ? ";
        String sql4 = "create table KNNR2 " +
        				"select keyword, srcNode as destNode, srcNode as srcNode, srcNode as firstNode, score as rel " +
        				"from KNS where keyword = ? ";
        String sql5 = "create table KNNR_";
        String sql6 = " select * from (select * from KNNR1 UNION ALL select * from KNNR2) order by rel desc ";
 */       
	connect();

        try {
 //           for (int i = 0; i < ks.length; i++) {
                    //pstmt = conn.prepareStatement(sql1+ks[i]+sql2+ks[i]+sql3);
                    pstmt = conn.prepareStatement(sql1+k+sql2);
//                    pstmt.setString(1, ks[i]);
                    pstmt.executeUpdate();
                    System.out.println("knnr table for " + k + " was populated.");
                    pstmt.close();
            /*	
            	pstmt = conn.prepareStatement(sql3);
                pstmt.setString(1, ks[i]);
                pstmt.executeUpdate();
                pstmt = conn.prepareStatement(sql4);
                pstmt.setString(1, ks[i]);
                pstmt.executeUpdate();
                pstmt = conn.prepareStatement(sql5+ks[i]+sql6);
                pstmt.executeUpdate();
                
                pstmt = conn.prepareStatement("drop table KNNR1");
                pstmt.executeUpdate();
                pstmt = conn.prepareStatement("drop table KNNR2");
                pstmt.executeUpdate();
                
                System.out.println("knnr table for " + ks[i] + " was populated.");
                pstmt.close();
*/
  //          }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            disconnect();
        }
        return true;
    }
	/*	
	public boolean reorderKNNRintoKNNR2() {
		String sql1 = "create table KNNR2 " +
						"select keyword, destNode, srcNode, firstNode, rel " +
						"from KNNR order by keyword, rel desc ";
		connect();		
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql1);
			System.out.println("table KNNR2 was constructed.");		

    public boolean computeKeywordNodeNodeRel() {		
		String sql1 = "insert into KNNR (keyword, destNode, srcNode, firstNode, rel) " +
						"select a.keyword, b.startNode, b.endNode, b.firstNode, a.score*b.rel AS relv  " +
						"from KNS a, NNR2 b where a.srcNode = b.endNode " +
//						"and a.keyword < 'g' " +
//					 	"order by a.keyword, b.startNode, relv desc ";
						"order by a.keyword, relv desc ";
//		String sql2 = "insert into KNNR (destNode, keyword, rel, srcNode) " +						// keyword, destNode, srcNode, rel
//						"select srcNode, keyword, score, srcNode from KNS";
//		--> unnecessary since NNR already has a self-relation for each node, i.e. (n, n, 1.0)
	
		connect();

		try {
			pstmt = conn.prepareStatement(sql1);
			pstmt.executeUpdate();
//			pstmt.close();
//			pstmt = conn.prepareStatement(sql2);	
//			pstmt.executeUpdate();
			System.out.println("knnr table populated.");
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		finally {
			disconnect();
		}
		return true;
	}	
	/*	
	public boolean reorderKNNRintoKNNR2() {
		String sql1 = "create table KNNR2 " +
						"select keyword, destNode, srcNode, firstNode, rel " +
						"from KNNR order by keyword, rel desc ";
		connect();		
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql1);
			System.out.println("table KNNR2 was constructed.");		
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		finally {
			disconnect();
		}
		return true;
	}
*/	
/*	public boolean createIndexOnKNNRTable() {
		connect();
		String sql2 = "create index ind_knnr on knnr(keyword, destNode)";
//		String sql3 = "create index ind_knnr_keyword on knnr(keyword)";
		try {
			Statement stmt = conn.createStatement();
//			stmt.executeUpdate(sql3);
//			System.out.println("index on knnr(keyword) was constructed.");
//			stmt.close();
//			stmt = conn.createStatement();
			stmt.executeUpdate(sql2);
			System.out.println("index on knnr(keyword, destNode) was constructed.");
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		finally {
			disconnect();
		}
		return true;
	}
	*/
/*	public boolean computeNKMapForBlink() {
		connect();
		int destNode, srcNode;
		float rel;
		String keyword;
		
		String sql1 = "select destNode, keyword, rel, srcNode from nk_map";	
		try {
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				destNode = rs.getInt(1);
				keyword = rs.getString(2);
				rel = rs.getFloat(3);
				srcNode = rs.getInt(4);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		finally {
			disconnect();
		}
		return true;
	}*/
	
	public void buildTermListFile() {
//		knnrPs = new HashMap<String, PrintStream>(32);
		BufferedWriter bw = null;
		
		connect();		
		String sql1 = "select distinct keyword from KNS order by keyword";		
		try {
			if (new File(termListFile).isFile() == false) 
				bw = new BufferedWriter(new FileWriter(termListFile));
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				String kw = rs.getString(1);
				if (bw != null) {
					bw.write(kw); bw.newLine();
				}				
//				PrintStream ps = new PrintStream(new FileOutputStream(Params.knnrDir + kw));
//				knnrPs.put(kw, ps);
			}
			if (bw != null) bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disconnect();
		}
	}

/*
	public void buildBasicIndex() { 
		//String sql = "select keyword, destNode, srcNode, firstNode, rel" +
		//			" from KNNR order by keyword, rel desc";
		String sql = "select keyword, destNode, srcNode, firstNode, rel" +
					" from KNNR2 "; 
//					+ " where keyword > 'fukuoka' ";		// already sorted by keyword, rel desc 
		connect();
		try {
			Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(1000);
			rs = stmt.executeQuery(sql);				

			int i = 0, dn;
            String k = null, oldk = null;
    		InvertedList curList0 = null;
    		HashSet<Integer> dnset = null;

			while(rs.next()) {
				if (++i % 10000 == 0) System.out.println(k + ": " + i);
				k = rs.getString(1); dn = rs.getInt(2);
				
				if (!k.equals(oldk)) {	// new keyword found
					if (curList0 != null) curList0.complete();
					curList0 = new InvertedList(k);
					curList0.openToBuild();
					if (dnset != null) dnset.clear();
					dnset = new HashSet<Integer>(1024);
					oldk = k;
				}
				if (dnset.add(dn) == true) {	// new dn for the current keyword found
					curList0.writeEntry(dn, rs.getInt(4), rs.getInt(3), rs.getFloat(5));  // fn, sn, rel
				}				
			}
			if (curList0 != null) curList0.complete();
		} catch (Exception e) {
			System.out.println("Exception when processing keyword ");
			e.printStackTrace();
		} finally {
			disconnect();
		}		    
	}
*/
/*	
	public void buildEnhancedIndex() {
		String sql0 = "select count(*) from KNNR where keyword = ?";
		String sql = "select destNode, srcNode, rel from KNNR " +
					 "where keyword = ? order by rel desc";
		String k;
		EnhInvertedList curList = null;

		connect();
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt0 = conn.prepareStatement(sql0);
			BufferedReader bw = new BufferedReader(new FileReader(termListFile));
			while ((k = bw.readLine())!= null) 
			{	
				if (k.matches("[0-9]+")) continue;
				pstmt0.setString(1, k);
				rs = pstmt0.executeQuery();
				rs.next();
				int numOfEntries = rs.getInt(1); 
				rs.close();
				
				curList = new EnhInvertedList(k);
				curList.openToBuild(numOfEntries);
				pstmt.setString(1, k);
				rs = pstmt.executeQuery();			
				while(rs.next()) {
					curList.writeEntry(rs.getInt(1), rs.getInt(2), rs.getFloat(3));
				}
				rs.close();
				curList.complete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disconnect();
		}
	}
*/
/*
	public void buildBasicAndEnhancedIndex() {
//		String sql0 = "select count(*) from KNNR_SELECTED where keyword = ?";
//		String sql = "select destNode, srcNode, rel from KNNR_SELECTED " +
//					 "where keyword = ? order by rel desc";
		String sql0 = "select count(*) from KNNR where keyword = ?";
		String sql = "select destNode, srcNode, rel from KNNR " +
					 "where keyword = ? order by rel desc";
		String k = null;
		InvertedList curList0 = null;
//		EnhInvertedList curList1 = null;

		connect();
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt0 = conn.prepareStatement(sql0);
			BufferedReader bw = new BufferedReader(new FileReader(termListFile));
			while ((k = bw.readLine())!= null) 
			{	
				if (k.matches("[0-9]+")) continue; // || k.compareTo("ii") > 0) continue;
				pstmt0.setString(1, k);
				rs = pstmt0.executeQuery();
				rs.next();
				int numOfEntries = rs.getInt(1); 
				rs.close();
				
				curList0 = new InvertedList(k);
				curList0.openToBuild();
//				curList1 = new EnhInvertedList(k);
//				curList1.openToBuild(numOfEntries);
				pstmt.setString(1, k);
				rs = pstmt.executeQuery();			
				while(rs.next()) {
					curList0.writeEntry(rs.getInt(1), rs.getInt(2), rs.getFloat(3));
//					curList1.writeEntry(rs.getInt(1), rs.getInt(2), rs.getFloat(3));
				}
				rs.close();
				curList0.complete();
//				curList1.complete();
			}
		} catch (Exception e) {
			System.out.println("Exception when processing keyword " + k);
			e.printStackTrace();
		} finally {
			disconnect();
		}
	}
	
	public void buildBasicAndEnhancedIndex(String [] keywords) {
		String sql0 = "select count(*) from KNNR_SELECTED where keyword = ?";
		String sql = "select destNode, srcNode, rel from KNNR_SELECTED " +
					 "where keyword = ? order by rel desc";
		String k = null;
		InvertedList curList0 = null;
		EnhInvertedList curList1 = null;

		connect();
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt0 = conn.prepareStatement(sql0);
			
			for (int i = 0; i < keywords.length; i++) {
				k = keywords[i];	
				pstmt0.setString(1, k);
				rs = pstmt0.executeQuery();
				rs.next();
				int numOfEntries = rs.getInt(1); 
				rs.close();
				
				System.out.println(i + "th keyword: " + k + ", #ofEnt: " + numOfEntries);

				curList0 = new InvertedList(k);
				curList0.openToBuild();
				curList1 = new EnhInvertedList(k);
				curList1.openToBuild(numOfEntries);
				pstmt.setString(1, k);
				rs = pstmt.executeQuery();			
				while(rs.next()) {
					curList0.writeEntry(rs.getInt(1), rs.getInt(2), rs.getFloat(3));
					curList1.writeEntry(rs.getInt(1), rs.getInt(2), rs.getFloat(3));
				}
				rs.close();
				curList0.complete();
				curList1.complete();
			}
		} catch (Exception e) {
			System.out.println("Exception when processing keyword " + k);
			e.printStackTrace();
		} finally {
			disconnect();
		}
	}
*/
	public void createTablesForNKMap() {
		connect();
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate( 
				"create table NK_MAP 						" +
				"(keyword varchar(30), " +
				" destNode mediumint unsigned,				" +
				" fstNode mediumint unsigned,				" +
				" srcNode mediumint unsigned,				" +				
				" rel float)								"); 
			System.out.println("table NKMAP was constructed.");
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void populateNKMap() {
		connect();
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate( 
				"insert into nkmap" 							+
				" select keyword, destNode, max(rel) as rel" +
				" from KNNR " +
//				" from KNNR_SELECTED " +
//				" where keyword >= 's' " +
				" group by keyword, destNode");
//				" 	select destNode, keyword, max(rel) as rel" +
//				" 	from KNNR group by destNode, keyword" 		+
//				"	order by destNode, keyword				");

			System.out.println("table NKMAP was populated.");
			stmt.close();
/*			stmt = conn.createStatement();
			stmt.executeUpdate("create index ind_nkmap_keyword_destNode on nkmap(keyword, destNode)");
			System.out.println("index on nkmap(keyword, destNode) was constructed.");
			stmt.close();
*//*		
			stmt = conn.createStatement();
			stmt.executeUpdate( 
				"create table knnr_selected_ids" +
				"	select min(a.id) as id" +
				"	from knnr a, nkmap b" +
				"   where a.destNode = b.destNode" +
				"		and a.keyword = b.keyword" +
				"		and a.rel = b.rel" +
				"	group by a.destNode, a.keyword, a.rel	"); 
			System.out.println("table KNNR_SELECTED_IDS was constructed.");
			stmt.close();
			stmt = conn.createStatement();
			stmt.executeUpdate("create index ind_knnr_selected_ids on knnr_selected_ids(id)");
			System.out.println("index on knnr_selected_ids(id) was constructed.");
			stmt.close();
			
			stmt = conn.createStatement();
			stmt.executeUpdate( 
				"create table nk_map" +
				"	select a.destNode, a.keyword, a.rel, a.srcNode" +
				"   from knnr a, knnr_selected_ids b" +
				"   where a.id = b.id" +
				"	order by destNode, keyword;		"); 
			System.out.println("table NK_MAP was constructed.");
			stmt.close();			
*/		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
/*
	public void populateNK_Map() {
		connect();
		String sql1 = "select keyword, destNode, rel from nkmap"; 	// order by destNode, keyword";
//	        String sql1 = "select destNode, keyword, rel, srcNode from nk_map order by destNode, keyword";
//					+ "where destNode>=50000000" ;
			try {
				Statement stmt = conn.createStatement();
				rs = stmt.executeQuery(sql1);
	            
	    		pstmt = conn.prepareStatement(
	    			"select srcNode from knnr " +
	    			"where keyword = ? and destNode = ? and rel = ? " +
	    			"limit 1"
				);
	            int i = 0;
				while (rs.next()) {
					if (++i % 10000 == 0) System.out.println(i);
					nk.setDestNode(rs.getInt(1));
					nk.setKeyword(rs.getString(2));
					relSrc.setRel(rs.getFloat(3));				
//					relSrc.setSrcNode(rs.getInt(4));

					pstmt.setInt(1, rs.getInt(1));
					pstmt.setString(2, rs.getString(2));
					pstmt.setFloat(3, rs.getFloat(3));
					rs2 = pstmt.executeQuery();
					if (rs2.next() == false)  
						throw new Exception("Unexpected Error!");
					
					relSrc.setSrcNode(rs2.getInt(1));
								
					// Convert the Vendor object to a DatabaseEntry object
		            // using our SerialBinding
		            keyBinding.objectToEntry(nk, theKey);
		            dataBinding.objectToEntry(relSrc, theData);
		            
		            // Put it in the database. These puts are transactionally protected
		            // (we're using autocommit).
		            db.putNoOverwrite(null, theKey, theData);
				}		
	        	System.out.println(db.count());

			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				disconnect();
			}
	}
*/
/*	public void buildEnhancedIndex() {
		String sql = "select destNode, srcNode, rel " +
		"from KNNR " +
		"where keyword = ? order by rel desc";
		
		ByteBuffer buffer = ByteBuffer.allocateDirect(Params.FILE_BLOCK_SIZE);
		Hashtable<Integer, ListEntry> ht = new Hashtable<Integer, ListEntry>(32); 
		connect();
		try {
			String k; 
			EnhInvertedList curList = null;
			BufferedReader bw = new BufferedReader(new FileReader(termListFile));
			pstmt = conn.prepareStatement(sql);
			while ((k = bw.readLine())!= null) {			
				pstmt.setString(1, k);
				rs = pstmt.executeQuery();			
				curList = new EnhInvertedList(k);
				curList.openToBuild(buffer, ht);
				while(rs.next()) {
					curList.writeEntry(rs.getInt(1), rs.getInt(2), rs.getFloat(3));
				}
				curList.complete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disconnect();
		}
	}*/
}
