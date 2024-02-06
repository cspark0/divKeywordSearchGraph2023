package util;

public class ListEntry0 {
	public int nodeID, sNodeID;
	public float rel;
	public ListEntry0() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ListEntry0(int nid, int sid, float r) {
		nodeID = nid; sNodeID = sid; rel = r;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ListEntry [nodeID=" + nodeID + ", sNodeID=" + sNodeID
				+ ", rel=" + rel + "]";
	}
}
