package nkmap.bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class KeywordRelDescKeyCreator implements SecondaryKeyCreator {
	private TupleBinding<KeywordNode> nkbinding;
	private TupleBinding<RelSourceFirst> rsbinding;
	private TupleBinding<KeywordRel> krbinding;
	
    public KeywordRelDescKeyCreator() { 
    	nkbinding = new KeywordNodeBinding();
    	rsbinding = new RelSourceFirstBinding();
    	krbinding = new KeywordRelBinding();
    }        

    public boolean createSecondaryKey(SecondaryDatabase secDb,
                                      DatabaseEntry keyEntry, 
                                      DatabaseEntry dataEntry,
                                      DatabaseEntry resultEntry) {

    	KeywordNode nk = (KeywordNode)nkbinding.entryToObject(keyEntry);
    	RelSourceFirst rs = (RelSourceFirst)rsbinding.entryToObject(dataEntry);
        KeywordRel kr = new KeywordRel(nk.getKeyword(), rs.getRel());
        krbinding.objectToEntry(kr, resultEntry);
        
//      resultEntry.setData(kr.getBytes("UTF-8"));
        return true;
    }
}
