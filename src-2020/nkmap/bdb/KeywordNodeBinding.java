// file InventoryBinding.java

package nkmap.bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class KeywordNodeBinding extends TupleBinding<KeywordNode> {

	static KeywordNode nk = new KeywordNode();
	
    // Implement this abstract method. Used to convert
    // a DatabaseEntry to an NodeKeyword object.
    public KeywordNode entryToObject(TupleInput ti) {
        //int destNode = ti.readInt();
        //String keyword = ti.readString();
        //NodeKeyword nk = new NodeKeyword(destNode, keyword);
    	nk.setDestNodeAndKeyword(ti.readString(), ti.readInt());
        return nk;
    }

    // Implement this abstract method. Used to convert a
    // Inventory object to a DatabaseEntry object.
    public void objectToEntry(KeywordNode nk, TupleOutput to) {
        to.writeString(nk.getKeyword());	// keyword ¸ÕÀú!!! sort by (keyword, destNode)
        to.writeInt(nk.getDestNode());
    }
}
