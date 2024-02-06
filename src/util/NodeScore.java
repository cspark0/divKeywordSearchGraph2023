package util;

public class NodeScore {	// Answer tree
	public int nodeId;		// root node id
	public float score;		// relevance score
	public int[] srcNodes;	// keyword nodes

	@Override
	public String toString() {
		return "RelSource [score=" + score + ", srcNode=" + nodeId + "]";
	}

	public NodeScore() {}

	public NodeScore(int nodeId2, float score2) { //, int fstNode2) {
		score = score2; nodeId = nodeId2; //fstNode = fstNode2;
	}

	public NodeScore(NodeScore r) { //, int fstNode2) {
		nodeId = r.getNodeId(); score = r.getScore(); //fstNode = fstNode2;
	}
	
	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public int getNodeId() {
		return nodeId;
	}
	
	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	
	public void set(int nodeId, float score) {
		this.nodeId = nodeId;
		this.score = score;
	}	
}
