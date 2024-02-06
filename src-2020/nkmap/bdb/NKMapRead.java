// file ExampleInventoryRead

package nkmap.bdb;

//import java.io.File;
//import java.io.UnsupportedEncodingException;

import nkmap.bdb.RelSource;
import nkmap.bdb.RelSourceFirst;
import nkmap.bdb.RelSourceFirstBinding;
import nkmap.bdb.KeywordNode;
import util.Params;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
//import com.sleepycat.je.EnvironmentMutableConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class NKMapRead {

    public static String myDbEnvPath1;
    
    // Encapsulates the database environment and databases.
    public static MyDbEnv myDbEnv1;
    public static CursorConfig curConfig = new CursorConfig();

    public static TupleBinding<KeywordNode> keyBinding = new KeywordNodeBinding();
    public static TupleBinding<RelSourceFirst> firstDataBinding = new RelSourceFirstBinding();
    public static TupleBinding<RelSource> dataBinding = new RelSourceBinding();
    
    public NKMapRead() {
    	myDbEnvPath1 = Params.nkmap1;
        myDbEnv1 = new MyDbEnv();
    	myDbEnv1.setup(myDbEnvPath1, // path to the environment home
                true);      		// is this environment read-only?
        curConfig.setReadUncommitted(true);
    }
    
    public void reset() {
        myDbEnv1 = new MyDbEnv();
    	myDbEnv1.setup(myDbEnvPath1, // path to the environment home
                true);      		// is this environment read-only?
    }
    
/*
    public static void main(String args[]) {
		Params.setExpDB("jmdb");
//		NKMapRead eir = new NKMapRead("res/jmdb/NKMap");
//    	NKMapRead eir2 = new NKMapRead("res/mondial/NKMapForBlink");
    	//NKMapRead eir2 = new NKMapRead("res/dblp/NKMapForBlink");

        try {
        	System.out.println("1");	//EnvironmentMutableConfig.getCachePercent());
        	/*
//      	eir.showAllRecords();
    		NodeKeywon:u:wqrd nk = new NodeKeyword();
        	nk.setDestNode(20119); nk.setKeyword("islands");
//        	System.out.println(eir.searchRel(nk));
        	System.out.println(eir2.searchRelSource(nk));

        	nk.setDestNode(758447); nk.setKeyword("volcanic");
//        	System.out.println(eir.searchRel(nk));
        	System.out.println(eir2.searchRelSource(nk));
        	
        	nk.setDestNode(4077757); nk.setKeyword("heroin");
//        	System.out.println(eir.searchRel(nk));
        	System.out.println(eir2.searchRelSource(nk));

        } catch (DatabaseException dbe) {
            System.err.println("ExampleRead: " + dbe.toString());
            dbe.printStackTrace();
        } finally {
            myDbEnv1.close();
        }
        System.out.println("All done.");
    }

    public void showAllRecords() throws DatabaseException {
        // Get a cursor
        Cursor cursor = myDbEnv1.getNKMapBlinkDB().openCursor(null, null);

        // DatabaseEntry objects used for reading records
        DatabaseEntry foundKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();

        KeywordNode nk = null;
        
        try { // always want to make sure the cursor gets closed
            while (cursor.getNext(foundKey, foundData,
                        LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            	RelSource rs = (RelSource)dataBinding.entryToObject(foundData);
            	nk = (KeywordNode)keyBinding.entryToObject(foundKey);
            	
                System.out.print(nk.getDestNode() + ", " + nk.getKeyword() + ":");
                System.out.print(" " + rs.getRel());
                System.out.println(", " + rs.getSrcNode());
            }
            
//            System.out.println(searchRel(nk));
            System.out.println(searchRelSource(nk));
            
        } catch (Exception e) {
            System.err.println("Error on inventory cursor:");
            System.err.println(e.toString());
            e.printStackTrace();
        } finally {
            cursor.close();
        }
    }
*/
    public RelSource searchRelSource(KeywordNode nk) {
    	// DatabaseEntry objects used for reading records
        DatabaseEntry searchKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
        Database db = myDbEnv1.getNKMapBlinkDB();
        keyBinding.objectToEntry(nk, searchKey);
 //       System.out.println(nk.getDestNode() + ", " + nk.getKeyword());
        		
        if (db.get(null, searchKey, foundData, LockMode.DEFAULT) != OperationStatus.SUCCESS) {
//            System.out.println("Could not find an entry: " +
//                nk.getDestNode() + ", " + nk.getKeyword());	
            return null;
        } else {
        	return (RelSource)dataBinding.entryToObject(foundData);
        } 
    }
    
    public RelSourceFirst searchRelSourceFirst(KeywordNode nk) {
    	// DatabaseEntry objects used for reading records
        DatabaseEntry searchKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
        Database db = myDbEnv1.getNKMapBlinkDB();
        keyBinding.objectToEntry(nk, searchKey);
 //       System.out.println(nk.getDestNode() + ", " + nk.getKeyword());
        		
        if (db.get(null, searchKey, foundData, LockMode.DEFAULT) != OperationStatus.SUCCESS) {
//            System.out.println("Could not find an entry: " +
//                nk.getDestNode() + ", " + nk.getKeyword());	
            return null;
        } else {
        	return (RelSourceFirst)firstDataBinding.entryToObject(foundData);
        } 
    }
    
    public RelSource[] searchRelSourcesInNKMapExt(KeywordNode nk) {
    	// DatabaseEntry objects used for reading records
        DatabaseEntry searchKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
        Database db = myDbEnv1.getNKMapBlinkDB();
        keyBinding.objectToEntry(nk, searchKey);
 //       System.out.println(nk.getDestNode() + ", " + nk.getKeyword());
        RelSource[] RSs = null;
        Cursor cursor = null;
        
        try {            
            // Open a cursor using a database handle
            cursor = db.openCursor(null, curConfig);

            // Position the cursor
            OperationStatus retVal = cursor.getSearchKey(searchKey, foundData, LockMode.DEFAULT);      
            assert(retVal == OperationStatus.SUCCESS);
            RelSourceFirst e =(RelSourceFirst)firstDataBinding.entryToObject(foundData);
        	RSs = new RelSource[cursor.count()];
        	RSs[0] = new RelSource(e.getRel(), e.getSrcNode());
        	int i = 1;
            while (cursor.getNextDup(searchKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) 
                RSs[i++] = new RelSource((RelSource)dataBinding.entryToObject(foundData));
        } catch (Exception e) {
            // Exception handling goes here
        } finally {
           // Make sure to close the cursor
        	cursor.close();
        }
        return RSs;	
    }

    public RelSource searchRelSourceIn2ndNKMap(KeywordNode nk) {

    	// DatabaseEntry objects used for reading records
        DatabaseEntry searchKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
        Database db = myDbEnv1.getNKMapSubBlinkDB();
        keyBinding.objectToEntry(nk, searchKey);
 //       System.out.println(nk.getDestNode() + ", " + nk.getKeyword());
        		
        if (db.get(null, searchKey, foundData,
                LockMode.DEFAULT) != OperationStatus.SUCCESS) {
//            System.out.println("Could not find an entry: " +
//                nk.getDestNode() + ", " + nk.getKeyword());	
            return null;
        } else {
        	return (RelSource)dataBinding.entryToObject(foundData);
        } 
    }

    /*
    public float searchRel(NodeKeyword nk) {

    	// DatabaseEntry objects used for reading records
        DatabaseEntry searchKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
        Database db = myDbEnv.getNKMapDB();
        keyBinding.objectToEntry(nk, searchKey);

        if (db.get(null, searchKey, foundData,
                LockMode.DEFAULT) != OperationStatus.SUCCESS) {
//            System.out.println("Could not find an entry: " +
//                nk.getDestNode() + ", " + nk.getKeyword());	
            return (float)0.0;
        } else {
        	float rel = (float)0;
			try {
//				System.out.println(foundData.getData());
				rel = Float.parseFloat(new String(foundData.getData(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	return rel; 
        } 
    }
    */
    
    public void close() { myDbEnv1.close(); }

    public Cursor getCursorForSearchNKMapExt(String keyword) {
        Database db = myDbEnv1.getNKMapBlinkDB();
        Cursor c = null;
        try {            
            // Open a cursor using a database handle
            c = db.openCursor(null, curConfig);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
        return c;
    }

    public RelSource searchNKMapExtForFirstData(KeywordNode nk, Cursor cursor) {
        DatabaseEntry searchKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
    	// DatabaseEntry objects used for reading records
        keyBinding.objectToEntry(nk, searchKey);
        RelSourceFirst e = null;
        try {            
            // Position the cursor
            OperationStatus retVal = cursor.getSearchKey(searchKey, foundData, LockMode.DEFAULT);      
            assert(retVal == OperationStatus.SUCCESS);
            e =(RelSourceFirst)firstDataBinding.entryToObject(foundData);
        	//RSs = new RelSource[cursor.count()];
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
        return new RelSource(e.getRel(), e.getSrcNode());
    }

    public RelSource searchNKMapExtForNextData(KeywordNode nk, Cursor cursor) {
        DatabaseEntry searchKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();

        keyBinding.objectToEntry(nk, searchKey);
        RelSource rs = null;
        try {            
            OperationStatus retVal = cursor.getNextDup(searchKey, foundData, LockMode.DEFAULT); //  == OperationStatus.SUCCESS
            assert(retVal == OperationStatus.SUCCESS);
            rs = new RelSource((RelSource)dataBinding.entryToObject(foundData));
        } catch (Exception e) {
            // Exception handling goes here
 			e.printStackTrace();
        } 
        return rs;
    }
}
