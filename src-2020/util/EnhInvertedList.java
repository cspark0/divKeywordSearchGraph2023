
package util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.Hashtable;

public class EnhInvertedList extends InvertedList {

	// for test
//	EnhListEntry[] list = new EnhListEntry[100];
//	int curPos = 0;
	static ByteBuffer buffer = null;		// override! buffer for scanning a index
	static FileChannel fc = null;			// override
	
	static RandomAccessFile raf; 
	static Hashtable<Integer, Integer> ht = new Hashtable<Integer, Integer>(32);

	static int SIZE_OF_ENH_ENTRY = (Integer.SIZE*2+Float.SIZE*2)/8;
	static int NUM_OF_ENTRIES_IN_BLOCK = Params.FILE_BLOCK_SIZE / SIZE_OF_ENH_ENTRY;	
	static int ENH_BUFFER_LIMIT = NUM_OF_ENTRIES_IN_BLOCK * SIZE_OF_ENH_ENTRY;	
	
	public EnhInvertedList(String keyword) {
		this.keyword = keyword;
		this.fileName = Params.ExpDB + "/enhancedIndex/ei-"+keyword+".ind";
//		this.fileName = "E:/res/jmdb/enhancedIndex/ei-"+keyword+".ind";
	}
	
	@Override
	public int getBufferLimit() { return ENH_BUFFER_LIMIT; }

	public void openToBuild(int numOfEntries) throws IOException {
		int fileSize = numOfEntries * SIZE_OF_ENH_ENTRY;
//		try {
			raf = new RandomAccessFile(fileName, "rw");
			raf.setLength(fileSize);			
			fc = raf.getChannel();
//			buffer = ByteBuffer.allocateDirect(Params.FILE_BLOCK_SIZE);
			buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0L, fileSize);	// Memory Mapped File
//			buffer.limit(ENH_BUFFER_LIMIT);			
		//} catch (IOException e) {
			//e.printStackTrace();
		//}		
	}
	 
/*	public EnhInvertedList(DataInputStream dis) {
		try {
			for (int i = 0; i < 100; i++) {
				list[i] = new EnhListEntry();
				list[i].nodeID = dis.readInt();
				list[i].sNodeID = dis.readInt();
				list[i].rel = dis.readFloat(); // monotonically decreasing
				list[i].rel_next = (float)0.0; // monotonically decreasing
//				System.out.println(list[i]);
			}	
//			System.out.println();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int i, j, k, nid;
		for (i = 0; i < 100; i++) {
			if (list[i].rel_next != 0.0) continue;		// already found nodeID
			nid = list[i].nodeID;
			for (j = i, k = i+1; k < 100; k++) {
				if (list[k].nodeID == nid) {
					list[j].rel_next = list[k].rel;
					j = k;
				}
			}
			list[j].rel_next = -1;
		}
		for (i = 0; i < 100; i++) {
			System.out.println(list[i]);
		}	
		System.out.println();
	}*/

	
	
	/*	 (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EnhInvertedList [keyword="+this.keyword + "]";
	}
	
		
	@Override
	public void writeEntry(int nodeID, int fNodeID, int sNodeID, float rel) throws IOException {
		// 주어진 EnhListEntry를 buffer의 current position에 저장
		buffer.putInt(nodeID);
		buffer.putInt(sNodeID);
		buffer.putFloat(rel);
		buffer.putFloat((float)0);	// next_rel
		
		int curPos = buffer.position();
		Integer prevPos = ht.put(nodeID, curPos-4);	// previous position value is returned			
		if (prevPos != null) { 
			buffer.position(prevPos.intValue());
			buffer.putFloat(rel);	// set as next_rel
		}
		buffer.position(curPos);		
	}
	
	@Override
	public void complete() {
		Enumeration<Integer> prevPos = ht.elements();	
		try {
			// flush all the entries in the hashtable;
			while (prevPos.hasMoreElements()) {
				buffer.position(prevPos.nextElement().intValue());
				buffer.putFloat((float)-1);
			}					
			((MappedByteBuffer)buffer).force();
			buffer = null;
			fc.close();
			raf.close();
			ht.clear();		
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}	
}
