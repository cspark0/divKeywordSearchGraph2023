package util;

import java.util.Arrays;

public class AnswerTree {	// Answer tree
	public int rootNodeId;		// root node id
	public float score;		// relevance score
	public int[] srcNodes;	// keyword nodes

	public AnswerTree() {}

	public AnswerTree(int nodeId, float score, int[] n, int querySize) { 
		rootNodeId = nodeId; 
		this.score = score; 
		srcNodes = new int[querySize];
		for (int i = 0; i < querySize; i++) srcNodes[i] = n[i];
	}

	@Override
	public String toString() {
		return "AnswerTree [rootNodeId=" + rootNodeId + ", score=" + score + ", srcNodes=" + Arrays.toString(srcNodes)
				+ "]";
	}

	/*
	 * public AnswerTree(AnswerTree r) { rootNodeId = r.rootNodeId; score = r.score;
	 * srcNodes = r.srcNodes; }
	 */
	

	
	/*
	 * public float getScore() { return score; }
	 * 
	 * public void setScore(float score) { this.score = score; }
	 * 
	 * public int getNodeId() { return rootNodeId; }
	 * 
	 * public void setNodeId(int nodeId) { this.rootNodeId = nodeId; }
	 * 
	 * public void set(int nodeId, float score) { this.rootNodeId = nodeId;
	 * this.score = score; }
	 */
}
