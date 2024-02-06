package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TargetList {
	InvertedList list;
	int index;			// index into the set of target lists
	
	FileInputStream fis = null;
	ByteBuffer buffer = null;		// buffer for scanning a index
	FileChannel fc = null;
	
	public InvertedList getList() { return list; }
	public int getIndex() { return index; }
	public TargetList() { }

	public TargetList(InvertedList l, int i) {
		list = l; index = i;
	}

	public boolean openToScan() {
		try {
			fis = new FileInputStream(list.getFileName());
			fc = fis.getChannel();
			buffer = ByteBuffer.allocateDirect(Params.FILE_BLOCK_SIZE);
			buffer.limit(list.getBufferLimit());
			if (fc.read(buffer) == -1) { 	// EOF
				System.out.println("index file " + list.getFileName() + " has no entry!!!");
				fc.close(); fis.close();
				return false;			
			}
			buffer.flip();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return true;
	}

	public ListEntry getNextEntry() {
		// this.list에서 다음 항목을 하나 읽어서 ListEntry 타입 객체 생성 반환
		// 만일 모든 항목을 이미 읽은 경우 null 반환
		ListEntry e = null;
		try {
			if (buffer.hasRemaining() == false) {
				buffer.clear(); buffer.limit(list.getBufferLimit());
				if (fc.read(buffer) == -1) { 	// EOF
					fc.close(); fis.close();
					return null;			
				}
				buffer.flip();
			}
			e = new ListEntry();
			e.nodeID = buffer.getInt();
			e.fNodeID = buffer.getInt();
			e.sNodeID = buffer.getInt();
			e.rel = buffer.getFloat();
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}			
		return e;
	}
	
	@Override
	public String toString() {
		return "TargetList [list=" + list + ", index=" + index + "]";
	}

    public void close() {
        try {
            fc.close(); fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
    }
}

