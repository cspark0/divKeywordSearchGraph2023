// file InventoryBinding.java

package nkmap.bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class KeywordRelBinding extends TupleBinding<KeywordRel> {

	static KeywordRel kr = new KeywordRel();
	
    // Implement this abstract method. Used to convert
    // a DatabaseEntry to an NodeKeyword object.
    public KeywordRel entryToObject(TupleInput ti) {
        kr.set(ti.readString(), ti.readFloat());
        return kr;
    }

    // Implement this abstract method. Used to convert a
    // Inventory object to a DatabaseEntry object.
    public void objectToEntry(KeywordRel kr, TupleOutput to) {
        to.writeString(kr.getKeyword());	// keyword ¸ÕÀú!!! sort by (keyword, destNode)
        to.writeFloat(kr.getRel());
    }
}
