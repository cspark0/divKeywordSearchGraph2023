package nkmap.bdb;

import java.util.Comparator;

import com.sleepycat.je.DatabaseEntry;

public class SecKeyComparator implements Comparator<byte[]> {
		static KeywordRelBinding secKeyBinding = new KeywordRelBinding();

	    public SecKeyComparator() {}

		@Override
		public int compare(byte[] k1, byte[] k2) {
	        KeywordRel kr1 = (KeywordRel)secKeyBinding.entryToObject(new DatabaseEntry(k1)); 
	        KeywordRel kr2 = (KeywordRel)secKeyBinding.entryToObject(new DatabaseEntry(k2)); 
	        int i = kr1.getKeyword().compareTo(kr2.getKeyword());
	        if (i != 0) return i;
	        return Float.compare(kr2.getRel(), kr1.getRel());	// descending order!!        
		}
}
