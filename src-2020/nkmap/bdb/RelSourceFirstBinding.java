package nkmap.bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class RelSourceFirstBinding extends TupleBinding<RelSourceFirst> {
	static RelSourceFirst rs = new RelSourceFirst();
	
    // Implement this abstract method. Used to convert
    // a DatabaseEntry to an NodeKeyword object.
    public RelSourceFirst entryToObject(TupleInput ti) {
        int f = ti.readInt();
        int s = ti.readInt();
        float r = ti.readFloat();
        rs.set(f, s, r);
        return rs;
    }

    // Implement this abstract method. Used to convert a
    // Inventory object to a DatabaseEntry object.
    public void objectToEntry(RelSourceFirst rs, TupleOutput to) {
    	to.writeInt(rs.getFstNode());
        to.writeInt(rs.getSrcNode());
        to.writeFloat(rs.getRel());        
    }
}
