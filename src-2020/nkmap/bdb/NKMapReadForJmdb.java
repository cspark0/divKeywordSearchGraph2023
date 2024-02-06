// file ExampleInventoryRead

package nkmap.bdb;

//import java.io.File;
//import java.io.UnsupportedEncodingException;

import nkmap.bdb.RelSourceFirst;
import nkmap.bdb.RelSourceFirstBinding;
import util.Params;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
//import com.sleepycat.je.EnvironmentMutableConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class NKMapReadForJmdb extends NKMapRead {

    public static String myDbEnvPath2;
    
    // Encapsulates the database environment and databases.
    public static MyDbEnv myDbEnv2;

    public NKMapReadForJmdb() {
        super();
    	myDbEnvPath2 = Params.nkmap2;
        myDbEnv2 = new MyDbEnv();
    	myDbEnv2.setup(myDbEnvPath2, // path to the environment home
                true);      		// is this environment read-only?
    }
    
    @Override
    public void reset() {
        super.reset();
        myDbEnv2 = new MyDbEnv();
    	myDbEnv2.setup(myDbEnvPath2, // path to the environment home
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
    		NodeKeyword nk = new NodeKeyword();
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
            myDbEnv.close();
        }
        System.out.println("All done.");
    }
*/

/*
    public void showAllRecords() throws DatabaseException {
        // Get a cursor
        Cursor cursor = myDbEnv.getNKMapBlinkDB().openCursor(null, null);

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
    @Override
    public RelSource searchRelSource(KeywordNode nk) {
    	// DatabaseEntry objects used for reading records
        DatabaseEntry searchKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
        //Database db = myDbEnv.getNKMapBlinkDB();
        //Database db = (Params.ExpDB.equals("res/mondial") || nk.getKeyword().compareTo("j") < 0) ?
        Database db = (nk.getKeyword().compareTo("j") < 0) ?
            myDbEnv1.getNKMapBlinkDB(): myDbEnv2.getNKMapBlinkDB() ;
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
    
    @Override
    public RelSourceFirst searchRelSourceFirst(KeywordNode nk) {
    	// DatabaseEntry objects used for reading records
        DatabaseEntry searchKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
        //Database db = myDbEnv.getNKMapBlinkDB();
        //Database db = (Params.ExpDB.equals("res/mondial") || nk.getKeyword().compareTo("j") < 0) ?
        Database db = (nk.getKeyword().compareTo("j") < 0) ?
            myDbEnv1.getNKMapBlinkDB(): myDbEnv2.getNKMapBlinkDB() ;
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
    
    @Override
    public RelSource[] searchRelSourcesInNKMapExt(KeywordNode nk) {
    	// DatabaseEntry objects used for reading records
        DatabaseEntry searchKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
        //Database db = myDbEnv.getNKMapBlinkDB();
//        Database db = (Params.ExpDB.equals("res/mondial") || nk.getKeyword().compareTo("j") < 0) ?
        Database db = (nk.getKeyword().compareTo("j") < 0) ?
            myDbEnv1.getNKMapBlinkDB(): myDbEnv2.getNKMapBlinkDB() ;
        keyBinding.objectToEntry(nk, searchKey);
 //       System.out.println(nk.getDestNode() + ", " + nk.getKeyword());
        RelSource[] RSs = null;
        Cursor cursor = null;
        
        try {            
            // Open a cursor using a database handle
            cursor = db.openCursor(null, null);

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

    @Override
    public RelSource searchRelSourceIn2ndNKMap(KeywordNode nk) {

    	// DatabaseEntry objects used for reading records
        DatabaseEntry searchKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
        //Database db = myDbEnv.getNKMapSubBlinkDB();
        //Database db = (Params.ExpDB.equals("res/mondial") || nk.getKeyword().compareTo("j") < 0) ?
        Database db = (nk.getKeyword().compareTo("j") < 0) ?
            myDbEnv1.getNKMapSubBlinkDB(): myDbEnv2.getNKMapSubBlinkDB() ;
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
    
    @Override
    public void close() { 
        super.close(); 
        myDbEnv2.close(); 
    }

}
