package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class EnhTargetList extends TargetList {
	EnhInvertedList list;
	
	@Override
	public InvertedList getList() { return this.list; }

	public EnhTargetList(EnhInvertedList l, int i) {
		this.list = l; index = i;
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
	
	public EnhListEntry getNextEntry() {
		// this.list���� ���� �׸��� �ϳ� �о ListEntry Ÿ�� ��ü ���� ��ȯ
		// ���� ��� �׸��� �̹� ���� ��� null ��ȯ
		EnhListEntry e = null;
		try {
			if (buffer.hasRemaining() == false) {
				buffer.clear(); buffer.limit(list.getBufferLimit());
				if (fc.read(buffer) == -1) { 	// EOF
					fc.close(); fis.close();
					return null;			
				}
				buffer.flip();
			}
			e = new EnhListEntry();
			e.nodeID = buffer.getInt();
			e.sNodeID = buffer.getInt();
			e.rel = buffer.getFloat();
			e.rel_next = buffer.getFloat();
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

}
