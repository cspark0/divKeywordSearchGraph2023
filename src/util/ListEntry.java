package util;

public class ListEntry {
	public int nodeID, fNodeID, sNodeID;	// destNode, firstNode, sourceNode
	public float rel;
	public ListEntry() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ListEntry(int nid, int fid, int sid, float r) {
		nodeID = nid; fNodeID = fid; sNodeID = sid; rel = r;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ListEntry [nodeID=" + nodeID + ", fNodeID=" + fNodeID  + ", sNodeID=" + sNodeID
				+ ", rel=" + rel + "]";
	}
}
