// file: ExampleDatabasePut.java

package nkmap.bdb;

//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;

import nkmap.bdb.RelSourceFirst;
import nkmap.bdb.RelSourceFirstBinding;
//import java.util.HashSet;

import util.InvertedList;
import util.Params;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
//import com.sleepycat.je.DatabaseException;
//import com.sleepycat.je.OperationStatus;

public class NKMapDatabasePutForJmdb {
//	static final String termListFile = Params.ExpDB + "/termlist.bin";

//    private static File myDbEnvPath1 = new File(Params.ExpDB + "/NKMapForBlink");
    private String myDbEnvPath1; 
    private String myDbEnvPath2; 
    
    // Encapsulates the environment and databases.
    private MyDbEnv myDbEnv1;	/// = new MyDbEnv();
    private MyDbEnv myDbEnv2;	/// = new MyDbEnv();
    
    // MySql 연결관련 변수 선언
 	Connection conn = null;
 	Statement stmt = null;
 	PreparedStatement pstmt = null, pstmt0 = null;
 	ResultSet rs = null, rs2 = null;
 		

    protected NKMapDatabasePutForJmdb() {
    	myDbEnvPath1 = Params.nkmap;
    	myDbEnv1 = new MyDbEnv();
    	myDbEnvPath2 = Params.nkmap2;
       myDbEnv2 = new MyDbEnv();
    }

 	// MySql 연결 메서드
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
 	// MySql 연결 종료 메서드
 	void disconnect() {
 		if(rs != null) {
 			try {
 				rs.close(); rs=null;
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		if(stmt != null) {
 			try {
 				stmt.close(); stmt=null;
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		} 

 		if(rs2 != null) {
 			try {
 				rs2.close(); rs2=null;
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		if(pstmt != null) {
 			try {
 				pstmt.close(); pstmt=null;
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		if(conn != null) {
 			try {
 				conn.close(); conn=null;
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}

 	public static void buildIndexAndNKMapForBlinks(String knnrTableName) {
        NKMapDatabasePutForJmdb edp = new NKMapDatabasePutForJmdb();
        try {
        	edp.myDbEnv1.setup(edp.myDbEnvPath1, // path to the environment home
                    false);      // is this environment read-only?
    	    edp.myDbEnv2.setup(edp.myDbEnvPath2, // path to the environment home
               false);      		// is this environment read-only?
        	System.out.println("loading NKMapForBlinkDB....");
//        	edp.loadIndexAndNKMapForBlinks_OnlyForReducedAnswer();
        	edp.loadIndexAndNKMapForBlinks_forReducedAndNonduplicateAnswers2(knnrTableName);
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
        } finally {
        	edp.myDbEnv1.close();
        	edp.myDbEnv2.close();
        }
        System.out.println("All done.");
    }

    private void loadIndexAndNKMapForBlinks_forReducedAndNonduplicateAnswers2(String knnrTableName) throws Exception {
    	String sql = "select keyword, destNode, srcNode, firstNode, rel " +
    				"from " + knnrTableName;
	InvertedList curList0 = null;
	DatabaseEntry theKey = new DatabaseEntry(),
		theData = new DatabaseEntry(), 
	    	firstData = new DatabaseEntry();
	    
	TupleBinding<KeywordNode> keyBinding = new KeywordNodeBinding();
	TupleBinding<RelSource> dataBinding = new RelSourceBinding();
	TupleBinding<RelSourceFirst> firstDataBinding = new RelSourceFirstBinding();
    
	Cursor cursor = null;
	int i = 0;
	    
	try {
	 Database db1 = null; // = (k.compareTo("j") < 0) ?  myDbEnv1.getNKMapBlinkDB(): myDbEnv2.getNKMapBlinkDB() ;
            
            KeywordNode nk = new KeywordNode();
            RelSource relSrc = new RelSource();            
            RelSourceFirst relSrcfirst = new RelSourceFirst();            

           	connect();
       	     	Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
           	stmt.setFetchSize(100000); 
		((com.mysql.cj.jdbc.StatementImpl)stmt).enableStreamingResults();
		rs = stmt.executeQuery(sql);				

        	System.out.println("querying table " + knnrTableName);

		int dn, fn, sn;
		float rel; 
		String k = null, oldk = null;
    	    
		while(rs.next()) {
			k = rs.getString(1);
//			if (k.compareTo("ji") >= 0) continue;

			if (++i % 10000 == 0) System.out.println(k + ": " + i);
			//k = rs.getString(1);
			dn = rs.getInt(2);
			sn = rs.getInt(3);
			fn = rs.getInt(4);
			rel = rs.getFloat(5);
				
			if (!k.equals(oldk)) {		// new keyword found
				if (curList0 != null) curList0.complete();
				curList0 = new InvertedList(k);
				curList0.openToBuild();
				System.out.println("new keyword=" + k);
	 			db1 = (k.compareTo("j") < 0) ?
            				myDbEnv1.getNKMapBlinkDB(): myDbEnv2.getNKMapBlinkDB() ;
				oldk = k;
			}
				
			nk.setDestNodeAndKeyword(k, dn);	
		    keyBinding.objectToEntry(nk, theKey);
		    relSrcfirst.set(fn, sn, rel);
		    firstDataBinding.objectToEntry(relSrcfirst, firstData);
			   
		    OperationStatus st = db1.putNoOverwrite(null, theKey, firstData);
		    if (st == OperationStatus.SUCCESS) {	// this is the first entry of theKey
		    	curList0.writeEntry(dn, fn, sn, rel); 	// insert an entry into the KNList index			    
		    }
		    else if (st == OperationStatus.KEYEXIST) {		// the first entry of theKey was already found 							    				    			    	
			cursor = db1.openCursor(null, null);	// Open a cursor using a database handle
		    	    if (cursor.getSearchKey(theKey, firstData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
/*
		            	RelSourceFirst rsf = (RelSourceFirst)firstDataBinding.entryToObject(firstData);
		            	if (rsf.getFstNode() != fn) {
			    	    	relSrc.set(sn, rel);
			    	    	dataBinding.objectToEntry(relSrc, theData);			    
				    db2.putNoOverwrite(null, theKey, theData);		// insert the sub-entry into NKMap-sub
		            	}
*/
				    	// Count the number of duplicates for theKey
			    	    if (cursor.count() < Params.numOfPathsForNKMapExt) { // need to store the entry in NKMap-ext
//			    			System.out.println("cursor.count() = " + cursor.count());
			    	    	relSrc.set(sn, rel);
			    	    	dataBinding.objectToEntry(relSrc, theData);	
					    db1.putNoDupData(null, theKey, theData); 		// add the duplicate data for theKey in NKMap-ext
//						   	System.out.println(" add entry: " + theKey + theData);
			    	    }			    	    
		    	}
		    	    cursor.close();
		    }		   			    
		}
		System.out.println("db1.count = " + db1.count()); // + ", db2.count = " + db2.count());
	
	} catch (Exception e) {
		System.out.println("Exception when processing keyword ");
		System.out.println(" add entry: " + theKey + "," + theData);
//            Exception e2= new Exception("Exception: " + i +", "+cursor.count());
            if (cursor!=null) cursor.close();
            e.printStackTrace();
 //           throw e2;

	} finally {
		if (curList0 != null) curList0.complete();
		disconnect();
	}		    	
    }

 	public static void buildIndexAndNKMapForBlinks(String[] ks) {
        NKMapDatabasePutForJmdb edp = new NKMapDatabasePutForJmdb();
        try {
            edp.myDbEnv1.setup(edp.myDbEnvPath1, // path to the environment home
                    false);      // is this environment read-only?
    	    edp.myDbEnv2.setup(edp.myDbEnvPath2, // path to the environment home
                false);      		// is this environment read-only?
            System.out.println("loading NKMapForBlinkDB....");
            for (int i = 0; i < ks.length;i++) 
                edp.loadIndexAndNKMapForBlinks_forReducedAndNonduplicateAnswers(ks[i]);

        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
        } finally {
            edp.myDbEnv1.close();
        	edp.myDbEnv2.close();
        }
        System.out.println("All done.");
    }

    private void loadIndexAndNKMapForBlinks_forReducedAndNonduplicateAnswers(String k) throws Exception {
        String sql = "select destNode, srcNode, firstNode, rel " +
                    "from KNNR_" + k;
                    // already sorted by keyword, rel desc 
        InvertedList curList0 = null;
        // DatabaseEntries used for loading records
        DatabaseEntry theKey = new DatabaseEntry(),
                    theData = new DatabaseEntry(), 
                    firstData = new DatabaseEntry();
        
        TupleBinding<KeywordNode> keyBinding = new KeywordNodeBinding();
        TupleBinding<RelSource> dataBinding = new RelSourceBinding();
        TupleBinding<RelSourceFirst> firstDataBinding = new RelSourceFirstBinding();
    
        Cursor cursor = null;
//      HashMap<Integer, Integer> ht = new HashMap<Integer, Integer>(); // destNode -> fn of the most relevant path for (currentKeyword, destNode)
        int i = 0;
        
        try {
            Database db1 = (k.compareTo("j") < 0) ?
            myDbEnv1.getNKMapBlinkDB(): myDbEnv2.getNKMapBlinkDB() ;
            //Database db2 = (k.compareTo("j") < 0) ?
            //myDbEnv1.getNKMapSubBlinkDB(): myDbEnv2.getNKMapSubBlinkDB() ;
                
            KeywordNode nk = new KeywordNode();
            RelSource relSrc = new RelSource();            
            RelSourceFirst relSrcfirst = new RelSourceFirst();            

            connect();
            Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
           stmt.setFetchSize(1000000);
            ((com.mysql.cj.jdbc.StatementImpl)stmt).enableStreamingResults();
            rs = stmt.executeQuery(sql);                

            int dn, fn, sn;
            float rel; 
            
            curList0 = new InvertedList(k);
            curList0.openToBuild();
            System.out.println("keyword=" + k);
            
            while(rs.next()) {
                if (++i % 10000 == 0) System.out.println(k + ": " + i);
                dn= rs.getInt(1);
                sn = rs.getInt(2);
                fn = rs.getInt(3);
                rel = rs.getFloat(4);                               
                
                // Convert the Vendor object to a DatabaseEntry object using our SerialBinding
                nk.setDestNodeAndKeyword(k, dn);    
                keyBinding.objectToEntry(nk, theKey);
                relSrcfirst.set(fn, sn, rel);
                firstDataBinding.objectToEntry(relSrcfirst, firstData);
               
//              System.out.println("current entry: " + theKey + firstData);
                // try to store the (first) key/data pair into the database if the key does not already appear in the database. 
                OperationStatus st = db1.putNoOverwrite(null, theKey, firstData);
                if (st == OperationStatus.SUCCESS) {    // this is the first entry of theKey
                    curList0.writeEntry(dn, fn, sn, rel);   // insert an entry into the KNList index                
                   // System.out.println("first entry inserted");
                }
                else if (st == OperationStatus.KEYEXIST) {      // the first entry of theKey was already found                                                                      
                    // Search for the first entry of theKey from NKMap      
//                  System.out.println("first entry exists");                   
                    cursor = db1.openCursor(null, null);    // Open a cursor using a database handle
                    if (cursor.getSearchKey(theKey, firstData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
//                      System.out.println("get first entry: " + theKey + firstData);
                      /*
                        RelSourceFirst rsf = (RelSourceFirst)firstDataBinding.entryToObject(firstData);
                        if (rsf.getFstNode() != fn) {
                            relSrc.set(sn, rel);
                            dataBinding.objectToEntry(relSrc, theData);     
                            db2.putNoOverwrite(null, theKey, theData);      // insert the sub-entry into NKMap-sub
                        }
                      */
                        // Count the number of duplicates for theKey
                        if (cursor.count() < Params.numOfPathsForNKMapExt) { // need to store the entry in NKMap-ext
//                          System.out.println("cursor.count() = " + cursor.count());
                            relSrc.set(sn, rel);
                            dataBinding.objectToEntry(relSrc, theData); 
                            //cursor.put(theKey, theData);         // add the duplicate data for theKey in NKMap-ext
                            db1.putNoDupData(null,theKey, theData);         // add the duplicate data for theKey in NKMap-ext
//                          System.out.println(" add entry: " + theKey + theData);
                        }                       
                    }
                    cursor.close();
                }                       
            }
            System.out.println("db1.count = " + db1.count()); // + ", db2.count = " + db2.count());

        } catch (Exception e) {
            System.out.println("Exception when processing keyword ");
            System.out.println(" add entry: " + theKey + "," + theData);
//            Exception e2= new Exception("Exception: " + i +", "+cursor.count());
            if (cursor!=null) cursor.close();
            e.printStackTrace();
//            throw e2;
        } finally {
            if (curList0 != null) curList0.complete();
            disconnect();
        }               
    }
     
    public static void deleteFromNKMap(String k, int i) {
        NKMapDatabasePutForJmdb edp = new NKMapDatabasePutForJmdb();
        try {
            System.out.println("h0");
            edp.myDbEnv1.setup(edp.myDbEnvPath1, // path to the environment home
                    false);      // is this environment read-only?
            System.out.println("h1");
                edp.deleteFromNKMap2(k, i);
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
        } finally {
            edp.myDbEnv1.close();
        }
        System.out.println("All done.");
    }

    private void deleteFromNKMap2(String k, int n) throws Exception {
        String sql = "select destNode " +
                "from KNNR_" + k + " limit " + n ;
                // already sorted by keyword, rel desc 
        DatabaseEntry theKey = new DatabaseEntry()  ;
            System.out.println(sql);
        TupleBinding<KeywordNode> keyBinding = new KeywordNodeBinding();
        int i = 0;
        Database db1 = myDbEnv1.getNKMapBlinkDB();
        Database db2 = myDbEnv1.getNKMapSubBlinkDB();
        try {
                
            KeywordNode nk = new KeywordNode();
           
            connect();
                System.out.println("hhhh");
			Statement stmt = conn.createStatement();
//           ( (com.mysql.jdbc.Statement)stmt).enableStreamingResults();
            rs = stmt.executeQuery(sql);                
            while(rs.next()) {
                if (++i % 10000 == 0) System.out.println(i);
                if (i == n) break;
                int dn = rs.getInt(1);  
                nk.setDestNodeAndKeyword(k, dn);    
                keyBinding.objectToEntry(nk, theKey);
                db1.delete(null,  theKey);
                db2.delete(null,  theKey);
            }
        
        } catch (Exception e) {
            System.out.println("Exception when processing keyword ");
            e.printStackTrace();
            throw new Exception("Exception: " + i);
        } finally {
            disconnect();
            db1.close(); db2.close();
        }
    }

    private void loadIndexAndNKMapForBlinks_OnlyForReducedAnswer() {
    	String sql = "select keyword, destNode, srcNode, firstNode, rel " +
    				"from KNNR ";
    				// already sorted by keyword, rel desc 
    	InvertedList curList0 = null;
//		HashSet<Integer> dnset = new HashSet<Integer>(1024);
//				dnset2 = new HashSet<Integer>(1024);
		// DatabaseEntries used for loading records
	    DatabaseEntry theKey = new DatabaseEntry();
	    DatabaseEntry theData = new DatabaseEntry(), nkmapData = new DatabaseEntry();
	    
	    TupleBinding<KeywordNode> keyBinding = new KeywordNodeBinding();
//	    TupleBinding<RelSource> dataBinding = new RelSourceBinding();
	    TupleBinding<RelSourceFirst> dataBinding = new RelSourceFirstBinding();
	   
		try {
        	Database db1 = myDbEnv1.getNKMapBlinkDB();
            Database db2 = myDbEnv1.getNKMapSubBlinkDB();
      			
            KeywordNode nk = new KeywordNode();
            RelSourceFirst relSrc = new RelSourceFirst();            
    		
           	connect();
			Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(1000);
			rs = stmt.executeQuery(sql);				

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
//					dnset.clear();
//					dnset2.clear();
					oldk = k;
				}
//				if (dnset.add(dn) == true) {	// new dn for the current keyword found
//					curList0.writeEntry(dn, fn, sn, rel); 	// insert into the index
//				}
				
				// insert into NKMap
				nk.setDestNodeAndKeyword(k, dn);	
				relSrc.set(fn, sn, rel);
				
				// Convert the Vendor object to a DatabaseEntry object
			    // using our SerialBinding
			    keyBinding.objectToEntry(nk, theKey);
			    dataBinding.objectToEntry(relSrc, theData);
				    
			    // Put it in the database. These puts are transactionally protected
			    // (we're using autocommit).
			    // Stores the key/data pair into the database if the key does not already appear in the database. 
			    OperationStatus st = db1.putNoOverwrite(null, theKey, theData);
			    if (st == OperationStatus.SUCCESS) {
					curList0.writeEntry(dn, fn, sn, rel); 	// insert into the index
			    }
			    else if (st == OperationStatus.KEYEXIST) {				    
					if (db1.get(null, theKey, nkmapData, LockMode.DEFAULT) != OperationStatus.SUCCESS) 
//			            System.out.println("Could not find an entry: " +
//			                nk.getDestNode() + ", " + nk.getKeyword());	
						throw new Exception("fatal error!");
					if (fn != ((RelSourceFirst)dataBinding.entryToObject(nkmapData)).getFstNode()) {
					    // Put it in the database. These puts are transactionally protected
					    // (we're using autocommit).
					    // Stores the key/data pair into the database if the key does not already appear in the database. 
						db2.putNoOverwrite(null, theKey, theData);
						// if already in db2, it does not overwrite and returns OperationStatus.KEYEXIST
					}
				}
			}
			System.out.println("db1.count = " + db1.count() + ", db2.count = " + db2.count());
	
		} catch (Exception e) {
			System.out.println("Exception when processing keyword ");
			e.printStackTrace();
		} finally {
			if (curList0 != null) curList0.complete();
//			dnset.clear();
//			dnset2.clear();
			disconnect();
		}		    	
 	}
    

/*
 	public static void buildNKMapForBlinks() {
        NKMapDatabasePut edp = new NKMapDatabasePut(Params.ExpDB + "/NKMapForBlink");
        try {
        	edp.myDbEnv.setup(edp.myDbEnvPath, // path to the environment home
                    false);      // is this environment read-only?
        	System.out.println("loading NKMapForBlinkDB....");
        	edp.loadNKMapForBlinkDB();
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
        } finally {
        	edp.myDbEnv.close();
        }
        System.out.println("All done.");
    }

    private void loadNKMapForBlinkDB()
    		throws Exception {
		String sql = "select keyword, destNode, srcNode, firstNode, rel from KNNR ";
		// Note: already ordered by keyword, destNode, rel desc in KNNR

		// Now load the data into the database. The vendor's name is the
        // key, and the data is a Vendor class object.

    	// Need a tuple binding for the Inventory class.
        TupleBinding<NodeKeyword> keyBinding = new NodeKeywordBinding();
        TupleBinding<RelSource> dataBinding = new RelSourceBinding();
        NodeKeyword nk = new NodeKeyword();
        RelSource relSrc = new RelSource();            
		
        connect();
		try {
            Database db1 = myDbEnv.getNKMapBlinkDB();
            Database db2 = myDbEnv.getNKMap2BlinkDB();
            
            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(1000);
			rs = stmt.executeQuery(sql);				

            int i = 0;
            int dn, sn, fn; 
            int olddn = -1, fmax = -1;
            float rel;
            String k = null, oldk = null;

			while(rs.next()) {
				if (++i % 1000000 == 0) System.out.println(k + ": " + i);
				k = rs.getString(1);
				dn = rs.getInt(2);
				sn = rs.getInt(3);
				fn = rs.getInt(4);
				rel = rs.getFloat(5);
				
				if (!k.equals(oldk)) {	// new keyword found
					oldk = k; olddn = -1;	     
				}
				if (dn != olddn) {	// new dn for the current keyword found
					olddn = dn;
				
					nk.setDestNodeAndKeyword(k, dn);
					relSrc.set(fn, sn, rel);
				
					// Convert the Vendor object to a DatabaseEntry object
				    // using our SerialBinding
				    keyBinding.objectToEntry(nk, theKey);
				    dataBinding.objectToEntry(relSrc, theData);
				    
				    // Put it in the database. These puts are transactionally protected
				    // (we're using autocommit).
				    // Stores the key/data pair into the database if the key does not already appear in the database. 
				    db1.putNoOverwrite(null, theKey, theData);
				    
				    fmax = fn;
				}
				else if (fmax >= 0) {	// 2nd nkmap path for dn has not been found and a new path for dn other than 1st one has found
					if (fn != fmax) {	// 2nd nkmap path is found now
						// insert into 2nd NKMap
						nk.setDestNodeAndKeyword(k, dn);
						relSrc.set(fn, sn, rel);
					    keyBinding.objectToEntry(nk, theKey);
					    dataBinding.objectToEntry(relSrc, theData);
					    db2.putNoOverwrite(null, theKey, theData);	
						fmax = -1;				    
					}
				}			   
			}
			System.out.println("db1.count = " + db1.count() + ", db2.count = " + db2.count());
		} catch (Exception e) {
			System.out.println("Exception when processing keyword ");
			e.printStackTrace();
			throw new Exception("Unexpected Error!");
		} finally {
			disconnect();
		}		
    }
/*
    public static void computeNKMap() {
        NKMapDatabasePut edp = new NKMapDatabasePut();
        
//        for (int j = 0; j < 1; j++) {  
//      	  System.out.println("Round " + j);
           
      	  try {
      		  edp.myDbEnv.setup(edp.myDbEnvPath2, // path to the environment home
      				  false);      // is this environment read-only?
      		  System.out.println("loading NKMapDB....");
//      		  edp.loadNKMapDB(j);
      		  edp.loadNKMapDB();
      	  } catch (Exception e) {
      		  System.out.println("Exception: " + e.toString());
      		  e.printStackTrace();
      	  } finally {
      		  edp.myDbEnv.close();
      	  }
        
//        }        
        System.out.println("All done.");
    }

    private void loadNKMapDB()
            throws DatabaseException {
  	
        // Now load the data into the database. The vendor's name is the
        // key, and the data is a Vendor class object.

    	// Need a tuple binding for the Inventory class.
        TupleBinding<NodeKeyword> nkBinding = new NodeKeywordBinding();
        Database db = myDbEnv.getNKMapDB();
        NodeKeyword nk = new NodeKeyword();

    	connect();
		String sql1 = "select keyword, destNode, rel from nkmap"; 	// order by destNode, keyword";
//		String sql1 = "select keyword, destNode, rel from nkmap limit 500000000, 409921929"; 	// order by destNode, keyword";	
//				+ " limit " + (j*10000000) + ", 10000000";
//		+ " where keyword < 'b'";
//		+ "where destNode>=50000000 " ;
//					+ "where destNode>=50000 " + "order by destNode, keyword ";	
	
		try {
//			stmt = conn.createStatement();
			stmt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
//			stmt.setFetchSize(10000); //Integer.MIN_VALUE);
			stmt.setFetchSize(Integer.MIN_VALUE);

			rs = stmt.executeQuery(sql1);
 
            int i = 0;
//            String s = null;
			while (rs.next()) {
				if (++i % 1000000 == 0) System.out.println(i);
//				nk.setKeyword(rs.getString(1));
//				nk.setDestNode(rs.getInt(2));
				nk.setDestNodeAndKeyword(rs.getString(1), rs.getInt(2));
				
				// Convert the Vendor object to a DatabaseEntry object
	            // using our SerialBinding
				nkBinding.objectToEntry(nk, theKey);

	            try {
	                theData.setData(Float.toString(rs.getFloat(3)).getBytes("UTF-8"));	                
//	                s = Float.toString(rs.getFloat(3));
//	                theData.setData(s.getBytes("UTF-8"));
	            } catch (IOException e) {
	    			e.printStackTrace();
	            }

	            // Put it in the database. These puts are transactionally protected
	            // (we're using autocommit).
	            db.put(null, theKey, theData);
			}		
        	System.out.println("# of entries in nkmap: " + db.count());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
    }
    */
/*	public static void main(String[] args) {
		Params.setExpDB("jmdb");

        NKMapDatabasePut edp = new NKMapDatabasePut();

		System.out.println("build nkmap in BDB...");
		edp.computeNKMap(Integer.parseInt(args[0]));
		
		System.out.println("successfully finished.");
	}
	
	public void computeNKMap(int j) {
        System.out.println("Round " + j);
           
      	  try {
      		  myDbEnv.setup(myDbEnvPath2, // path to the environment home
      				  false);      // is this environment read-only?
      		  System.out.println("loading NKMapDB....");
      		  loadNKMapDB(j);
      	  } catch (Exception e) {
      		  System.out.println("Exception: " + e.toString());
      		  e.printStackTrace();
      	  } finally {
      		  myDbEnv.close();
      	  }
        
        System.out.println("All done.");
    }*/
/*    
    public static void computeNKMapForBlink(String keyword) {
        NKMapDatabasePut edp = new NKMapDatabasePut();
        try {
        	edp.myDbEnv.setup(edp.myDbEnvPath1, // path to the environment home
                    false);      // is this environment read-only?
        	System.out.println("loading NKMapForBlinkDB....");
        	edp.loadNKMapForBlinkDB(keyword);
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
        } finally {
        	edp.myDbEnv.close();
        }
        System.out.println("All done.");
    }
    private void loadNKMapForBlinkDB(String keyword)
            throws Exception {
  	
        // Now load the data into the database. The vendor's name is the
        // key, and the data is a Vendor class object.

    	// Need a tuple binding for the Inventory class.
        TupleBinding<NodeKeyword> keyBinding = new NodeKeywordBinding();
        TupleBinding<RelSource> dataBinding = new RelSourceBinding();
        Connection con2 = null;
        try {
			con2 = DriverManager.getConnection(Params.jdbc_url,"root","anwlro");
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        connect();		
		String sql1 = "select destNode, rel from nkmap"
				+ " where keyword=?" ;
		try {
			pstmt = conn.prepareStatement(sql1);
			pstmt.setString(1, keyword);						
			rs = pstmt.executeQuery();
            Database db = myDbEnv.getNKMapBlinkDB();
            NodeKeyword nk = new NodeKeyword();
            RelSource relSrc = new RelSource();
            
    		PreparedStatement pstmt2 = con2.prepareStatement(
    			"select srcNode from KNNR_SELECTED " +
    			"where keyword = ? and destNode = ? and rel >= ? and rel < ?" +
    			"limit 1"
			);
            int i = 0;
			while (rs.next()) {
				if (++i % 1000000 == 0) System.out.println(i);
			
				int n = rs.getInt(1);
				float f = rs.getFloat(2);
				nk.setDestNodeAndKeyword(keyword, n);
				relSrc.setRel(f);				

				pstmt2.setString(1, keyword);
				pstmt2.setInt(2, n);
				
				if (f < 1) {
					pstmt2.setFloat(3, f-(float)0.000001); pstmt2.setFloat(4, f+(float)0.000001);
				}
				else if (f >= 10) {
					pstmt2.setFloat(3, f-(float)0.0001); pstmt2.setFloat(4, f+(float)0.0001);
				}
				else {
					pstmt2.setFloat(3, f-(float)0.00001); pstmt2.setFloat(4, f+(float)0.00001);
				}
				
				rs2 = pstmt2.executeQuery();
				if (rs2.next() == false)  {
					System.out.println(keyword + " " + n + " " + f);
					rs2.close();
					continue;
//					throw new Exception("Unexpected Error!");
				}
					
								
				relSrc.setSrcNode(rs2.getInt(1));
				rs2.close();
				
				// Convert the Vendor object to a DatabaseEntry object
	            // using our SerialBinding
	            keyBinding.objectToEntry(nk, theKey);
	            dataBinding.objectToEntry(relSrc, theData);
	            
	            // Put it in the database. These puts are transactionally protected
	            // (we're using autocommit).
	            db.putNoOverwrite(null, theKey, theData);
			}		
        	System.out.println(db.count());
			con2.close();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Unexpected Error!");
		}
		finally {
			disconnect();
		}
    }
*/
    /*
    public static void computeNKMap(String keyword) {
        NKMapDatabasePut edp = new NKMapDatabasePut();
        
//        for (int j = 0; j < 1; j++) {  
//      	  System.out.println("Round " + j);
           
      	  try {
      		  edp.myDbEnv.setup(edp.myDbEnvPath2, // path to the environment home
      				  false);      // is this environment read-only?
      		  System.out.println("loading NKMapDB....");
      		  edp.loadNKMapDB(keyword);
      	  } catch (Exception e) {
      		  System.out.println("Exception: " + e.toString());
      		  e.printStackTrace();
      	  } finally {
      		  edp.myDbEnv.close();
      	  }
        
//        }        
        System.out.println("All done.");
    }

    private void loadNKMapDB(String keyword)
            throws DatabaseException {
  	
        // Now load the data into the database. The vendor's name is the
        // key, and the data is a Vendor class object.

    	// Need a tuple binding for the Inventory class.
        TupleBinding<NodeKeyword> nkBinding = new NodeKeywordBinding();
        Database db = myDbEnv.getNKMapDB();
        NodeKeyword nk = new NodeKeyword();

    	connect();
		String sql1 = "select destNode, rel from nkmap" // ; 	// order by destNode, keyword";
					+ " where keyword=?";
	
		try {
			pstmt = conn.prepareStatement(sql1);
			pstmt.setString(1, keyword);
			rs = pstmt.executeQuery();
 
            int i = 0;
			while (rs.next()) {
				if (++i % 1000000 == 0) System.out.println(i);
				nk.setDestNodeAndKeyword(keyword, rs.getInt(1));
				
				// Convert the Vendor object to a DatabaseEntry object
	            // using our SerialBinding
				nkBinding.objectToEntry(nk, theKey);

	            try {
	                theData.setData(Float.toString(rs.getFloat(2)).getBytes("UTF-8"));	                
	            } catch (IOException e) {
	    			e.printStackTrace();
	            }

	            // Put it in the database. These puts are transactionally protected
	            // (we're using autocommit).
	            db.put(null, theKey, theData);
			}		
        	System.out.println("# of entries in nkmap: " + db.count());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
    }
*/
}
