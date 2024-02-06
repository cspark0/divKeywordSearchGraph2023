
package util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class InvertedList {

	// for test
//	ListEntry[] list = new ListEntry[100];
//	int curPos = 0;
//	DataOutputStream dos = null;
	
	String keyword = null, fileName = null;
	static FileOutputStream fos = null;
//	static FileInputStream fis = null;
	static ByteBuffer buffer = null;		// buffer for scanning a index
	static FileChannel fc = null;
	static int SIZE_OF_ENTRY = (Integer.SIZE*3+Float.SIZE)/8;
	static int NUM_OF_ENTRIES_IN_BLOCK = Params.FILE_BLOCK_SIZE / SIZE_OF_ENTRY;	
	static int BUFFER_LIMIT = NUM_OF_ENTRIES_IN_BLOCK * SIZE_OF_ENTRY;
	
	public InvertedList() { }

	public InvertedList(String keyword) {
		this.keyword = keyword;
		this.fileName = Params.ExpDB + "/basicIndex/bi-"+keyword+".ind";
//		this.fileName = "E:/res/jmdb/basicIndex/bi-"+keyword+".ind";
	}
	
	public String getKeyword() { return keyword; }
	public String getFileName() { return fileName; }
	public int getBufferLimit() { return BUFFER_LIMIT; }

	public void openToBuild() throws IOException {
//		try {
			fos = new FileOutputStream(fileName);
			fc = fos.getChannel();
			buffer = ByteBuffer.allocateDirect(Params.FILE_BLOCK_SIZE);
			buffer.limit(BUFFER_LIMIT);			
		//} catch (IOException e) {
			//e.printStackTrace();
//		}		
	}
		
	/*public InvertedList(DataInputStream dis) {
		try {
			for (int i = 0; i < 100; i++) {
				list[i] = new ListEntry();
				list[i].nodeID = dis.readInt();
				list[i].sNodeID = dis.readInt();
				list[i].rel = dis.readFloat(); // monotonically decreasing
//				System.out.println(list[i]);
			}	
//			System.out.println();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}*/
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
//		return "InvertedList [list=" + Arrays.toString(list) + "]";
		return "InvertedList [keyword="+this.keyword + "]";
	}

	public void writeEntry(int nodeID, int fNodeID, int sNodeID, float rel) throws IOException {
//	public void writeEntry(int nodeID, int sNodeID, float rel) throws IOException {
		// 주어진 ListEntry를 list에 연속적으로 저장
//		try {
			if (buffer.hasRemaining() == false) {
				buffer.flip();
				fc.write(buffer);
				buffer.rewind();
			}
			buffer.putInt(nodeID);
			buffer.putInt(fNodeID);
			buffer.putInt(sNodeID);
			buffer.putFloat(rel);
//		} catch (Exception ex) {
			// TODO Auto-generated catch block
//			ex.printStackTrace();
//		}
	}
	
	public void complete() {
		try {
			if (buffer.position() > 0) {
				buffer.flip();
				fc.write(buffer);
				buffer.clear();
			}
			buffer = null;
			fc.close();
			fos.close();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}
}
