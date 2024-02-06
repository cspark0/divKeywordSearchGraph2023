package nkmap.bdb;

import java.util.Comparator;

import com.sleepycat.je.DatabaseEntry;

public class NKMapExtDataComparator implements Comparator<byte[]> {
	static RelSourceBinding dataBinding = new RelSourceBinding();
	static RelSourceFirstBinding firstDataBinding = new RelSourceFirstBinding();

    public NKMapExtDataComparator() {}

	@Override
	public int compare(byte[] d1, byte[] d2) {
		float rel1 = (d1.length == 8) ?
        	((RelSource)dataBinding.entryToObject(new DatabaseEntry(d1))).getRel() : 
        	((RelSourceFirst)firstDataBinding.entryToObject(new DatabaseEntry(d1))).getRel();
			
        float rel2 = (d2.length == 8) ?
            	((RelSource)dataBinding.entryToObject(new DatabaseEntry(d2))).getRel() : 
            	((RelSourceFirst)firstDataBinding.entryToObject(new DatabaseEntry(d2))).getRel();
            	
        if (d1.length == d2.length) return Float.compare(rel2, rel1); // descending order!!   
        if (d1.length > d2.length) return -1;	// d1 should be the first entry in NKMapExt
        return 1;
	}
}
