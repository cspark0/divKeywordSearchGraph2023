// file MyDbEnv.java

package nkmap.bdb;

import java.io.File;

//import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;

public class MyDbEnv {

    private Environment env_nkmap = null, env_nkmap_sub = null, env_kns = null; //, env_nkmap_ext=null;

    // The databases that our application uses
//    private Database nkMapBlinkDb;
//    private Database nkMapDb;
    private Database nkmap = null, nkmap_sub = null, kns = null; // , nkmap_ext;
//    private Database classCatalog_nkmap, classCatalog_nkmap_sub, classCatalog_kns; //, classCatalog_nkmap_ext;
	private SecondaryDatabase secondary_for_nkmap = null;

    // Needed for object serialization
//    private StoredClassCatalog classCatalog, classCatalog2;

    // Our constructor does nothing
    public MyDbEnv() {}

    // The setup() method opens all our databases and the environment
    // for us.
    public void setup(String envHome, boolean readOnly)
        throws DatabaseException {


        EnvironmentConfig myEnvConfig = new EnvironmentConfig();
        DatabaseConfig myDbConfig = new DatabaseConfig();

        // If the environment is read-only, then
        // make the databases read-only too.
        myEnvConfig.setReadOnly(readOnly);
        myDbConfig.setReadOnly(readOnly);

        // If the environment is opened for write, then we want to be
        // able to create the environment and databases if
        // they do not exist.
        myEnvConfig.setAllowCreate(!readOnly);
        myDbConfig.setAllowCreate(!readOnly);

        myEnvConfig.setCachePercent(20);

        // To store more than one path infos most relevant to a (node, keyword) pair
        myDbConfig.setDuplicateComparator(NKMapExtDataComparator.class);
       	myDbConfig.setSortedDuplicates(true);
       	
//        nkmap_ext = env_nkmap_ext.openDatabase(null, "nkmap_ext", myDbConfig);
//        classCatalog_nkmap_ext = env_nkmap_ext.openDatabase(null, "ClassCatalogDB_nkmap_ext", myDbConfig);

        // Allow transactions if we are writing to the database
//        myEnvConfig.setTransactional(!readOnly);
//        myDbConfig.setTransactional(!readOnly);

        // Open the environment
        env_nkmap = new Environment(new File(envHome), myEnvConfig);
        env_nkmap_sub = new Environment(new File(envHome + "/sub"), myEnvConfig);
//        env_nkmap_ext = new Environment(new File(envHome + "/ext"), myEnvConfig);

        // Now open, or create and open, our databases
        // Open the vendors and inventory databases
        nkmap = env_nkmap.openDatabase(null, "nkmap", myDbConfig);
        nkmap_sub = env_nkmap_sub.openDatabase(null, "nkmap_sub", myDbConfig);
     	
        // Open the class catalog db. This is used to
        // optimize class serialization.
//        classCatalog_nkmap = env_nkmap.openDatabase(null, "ClassCatalogDB_nkmap", myDbConfig);
 //       classCatalog_nkmap_sub = env_nkmap_sub.openDatabase(null, "ClassCatalogDB_nkmap_sub", myDbConfig);

        // Create our class catalog
//        classCatalog = new StoredClassCatalog(classCatalogDb);
//        classCatalog2 = new StoredClassCatalog(classCatalogDb2);

        EnvironmentConfig myEnvConfig2 = env_nkmap.getConfig();
        System.out.println("maxCachePercent: " + myEnvConfig2.getCachePercent());
  //      myEnvConfig.setCacheSize(100000000);
        System.out.println("CacheSize: " + myEnvConfig2.getCacheSize());
        System.out.println("NodeMaxEntries: " + nkmap.getConfig().getNodeMaxEntries());
    }

    public void setup_kns(String envHome, boolean readOnly)
    		throws DatabaseException {

        EnvironmentConfig myEnvConfig = new EnvironmentConfig();
        DatabaseConfig myDbConfig = new DatabaseConfig();

        // If the environment is read-only, then
        // make the databases read-only too.
        myEnvConfig.setReadOnly(readOnly);
        myDbConfig.setReadOnly(readOnly);

        // If the environment is opened for write, then we want to be
        // able to create the environment and databases if
        // they do not exist.
        myEnvConfig.setAllowCreate(!readOnly);
        myDbConfig.setAllowCreate(!readOnly);
        myDbConfig.setSortedDuplicates(true);
        
        myEnvConfig.setCachePercent(20);

        // Open the environment
        env_kns = new Environment(new File(envHome), myEnvConfig);

        // Now open, or create and open, our databases
        // Open the vendors and inventory databases
        kns = env_kns.openDatabase(null, "kns", myDbConfig);

        // Open the class catalog db. This is used to
        // optimize class serialization.
//        classCatalog_kns = env_kns.openDatabase(null, "ClassCatalogDB_kns", myDbConfig);
       
    }

    public void build2ndIndexForNKMap(String envHome) throws DatabaseException {
        EnvironmentConfig myEnvConfig = new EnvironmentConfig();
        DatabaseConfig myDbConfig = new DatabaseConfig();

        // If the environment is opened for write, then we want to be
        // able to create the environment and databases if
        // they do not exist.
        myEnvConfig.setAllowCreate(true);
        myDbConfig.setAllowCreate(true);

       // Open the environment
        env_nkmap = new Environment(new File(envHome), myEnvConfig);

        // Now open, or create and open, our databases
        // Open the vendors and inventory databases
        nkmap = env_nkmap.openDatabase(null, "nkmap", myDbConfig);

        // Open the class catalog db. This is used to
        // optimize class serialization.
  //      classCatalog_nkmap = env_nkmap.openDatabase(null, "ClassCatalogDB_nkmap", myDbConfig);	
    	
    	SecondaryConfig mySecConfig = new SecondaryConfig();
    	mySecConfig.setAllowCreate(true);
    	// Duplicates are frequently required for secondary databases.
    	mySecConfig.setSortedDuplicates(true);

 //   	private TupleBinding<KeywordRel> krbinding = new KeywordRelBinding();
 //   	TupleBinding<NodeKeyword> nkbinding = new NodeKeywordBinding();
 //   	TupleBinding<RelSource> rsbinding = new RelSourceBinding();
    	
	    // Open the secondary.
	    // Key creators are described in the next section.
	    KeywordRelDescKeyCreator keyCreator = new KeywordRelDescKeyCreator();

	    // Get a secondary object and set the key creator on it.
	    mySecConfig.setKeyCreator(keyCreator);
	 
	    // Set a secondary key comparator
	    mySecConfig.setBtreeComparator(SecKeyComparator.class);
	    
	    // Perform the actual open
	    secondary_for_nkmap = env_nkmap.openSecondaryDatabase(null, "sec_nkmap", nkmap, mySecConfig); 
	    secondary_for_nkmap.close(); secondary_for_nkmap = null;
	    nkmap.close(); nkmap = null;
	    env_nkmap.close(); env_nkmap = null;
    }
    
    
    // getter methods
    public Database getNKMapBlinkDB() {
        return nkmap; 
    }
    
    public Database getNKMapSubBlinkDB() {
        return nkmap_sub; 	
    }
   
    public Database getKNSDB() {
        return kns; 	
    }
    
    public SecondaryDatabase open2ndDBforNKMap() {
		SecondaryConfig mySecConfig = new SecondaryConfig();
    	// Duplicates are frequently required for secondary databases.
    	mySecConfig.setSortedDuplicates(true);

 //   	private TupleBinding<KeywordRel> krbinding = new KeywordRelBinding();
 //   	TupleBinding<NodeKeyword> nkbinding = new NodeKeywordBinding();
 //   	TupleBinding<RelSource> rsbinding = new RelSourceBinding();
    	
	    // Open the secondary.
	    // Key creators are described in the next section.
	    KeywordRelDescKeyCreator keyCreator = new KeywordRelDescKeyCreator();

	    // Get a secondary object and set the key creator on it.
	    mySecConfig.setKeyCreator(keyCreator);

	    // Set a secondary key comparator
	    mySecConfig.setBtreeComparator(SecKeyComparator.class);
	    
	    // Perform the actual open
	    secondary_for_nkmap = env_nkmap.openSecondaryDatabase(null, "sec_nkmap", nkmap, mySecConfig);
        return secondary_for_nkmap; 	
    }
/*    
    // Needed for things like beginning transactions
    public Environment getEnv() {
        return myEnv;
    }  

    public StoredClassCatalog getClassCatalog() {
        return classCatalog;
    }
*/
    //Close the environment
    public void close() {
        if (env_nkmap != null) {
            try {
                //Close the secondary before closing the primaries
            	// nkMapDb.close();
                // nkMapBlinkDb.close();
            	if (secondary_for_nkmap != null) {
            		secondary_for_nkmap.close();
            		secondary_for_nkmap = null;
            	}
            	nkmap.close(); nkmap = null;
 //               classCatalog_nkmap.close();

                // Finally, close the environment.
                env_nkmap.close(); env_nkmap = null;
            } catch(DatabaseException dbe) {
                System.err.println("Error closing env_nkmap : " +
                                    dbe.toString());
               System.exit(-1);
            }
        }
        
        if (env_nkmap_sub != null) {
            try {
                nkmap_sub.close(); nkmap_sub = null;
 //               classCatalog_nkmap_sub.close();

                // Finally, close the environment.
                env_nkmap_sub.close(); env_nkmap_sub = null;
            } catch(DatabaseException dbe) {
                System.err.println("Error closing env_nkmap_sub: " +
                                    dbe.toString());
               System.exit(-1);
            }
        }
    }
    
    public void close_kns() {
    	if (env_kns != null) {
            try {
                //Close the secondary before closing the primaries
            	// nkMapDb.close();
                // nkMapBlinkDb.close();
            	kns.close(); kns = null;
 //               classCatalog_kns.close();

                // Finally, close the environment.
                env_kns.close(); env_kns = null;
            } catch(DatabaseException dbe) {
                System.err.println("Error closing KNSDB: " +
                                    dbe.toString());
               System.exit(-1);
            }
        }    	
    }
}

