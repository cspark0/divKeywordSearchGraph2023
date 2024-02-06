// file InventoryBinding.java

package nkmap.bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class RelSourceBinding extends TupleBinding<RelSource> {
	static RelSource rs = new RelSource();
	
    // Implement this abstract method. Used to convert
    // a DatabaseEntry to an NodeKeyword object.
    public RelSource entryToObject(TupleInput ti) {
        rs.set(ti.readInt(), ti.readFloat());
        return rs;
    }

    // Implement this abstract method. Used to convert a
    // Inventory object to a DatabaseEntry object.
    public void objectToEntry(RelSource rs, TupleOutput to) {
        to.writeInt(rs.getSrcNode());
        to.writeFloat(rs.getRel());        
    }
}
