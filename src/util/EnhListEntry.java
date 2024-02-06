package util;

public class EnhListEntry extends ListEntry {
	public float rel_next;
	public EnhListEntry() {
		// TODO Auto-generated constructor stub
		super();
	}
	public EnhListEntry(int nid, int fid, int sid, float r, float r_next) {
		super(nid, fid, sid, r); 
		rel_next = r_next;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EnhListEntry [nodeID=" + nodeID
				+ ", sNodeID=" + sNodeID + ", rel=" + rel + ", rel_next=" + rel_next + "]";
	}
	public static int size() {
		// TODO Auto-generated method stub
		return (Integer.SIZE+Float.SIZE)*2/8;
	}
}
