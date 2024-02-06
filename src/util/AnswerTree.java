package util;

import java.util.Arrays;
import java.util.HashSet;

public class AnswerTree {	// Answer tree
	public int rootNodeId;		// root node id
	public float score;		// relevance score
	public int[] srcNodes;	// keyword nodes

	public AnswerTree() {}

	public AnswerTree(int nodeId, float score, int[] keywordNodes) { //, int querySize) { 
		this.rootNodeId = nodeId; 
		this.score = score; 
		this.srcNodes = new int[keywordNodes.length];
		for (int i = 0; i < keywordNodes.length; i++) this.srcNodes[i] = keywordNodes[i];
	}

/*
	public void setAnswerTree(int nodeId, float score, int[] keywordNodes) { //, int querySize) { 
		this.rootNodeId = nodeId; 
		this.score = score; 
		this.srcNodes = new int[keywordNodes.length];
		for (int i = 0; i < keywordNodes.length; i++) this.srcNodes[i] = keywordNodes[i];
	}
*/

	private static HashSet<Integer> s1 = new HashSet<Integer>(10);
	private static HashSet<Integer> s2 = new HashSet<Integer>(10);
	private static HashSet<Integer> intersect = new HashSet<Integer>(10);

	public double computeDissimilarityByJaccardDistWith(AnswerTree other) {
		int[] n1 = this.srcNodes;
		int[] n2 = other.srcNodes;

		// use Jaccard distance 
        for(int i = 0; i < n1.length; i++) {
        // assume n1.length == n2.length
       		s1.add(Integer.valueOf(n1[i]));
       		s2.add(Integer.valueOf(n2[i]));
       	}
       	intersect.addAll(s1);	// copy of s1
        intersect.retainAll(s2);    // <- s1 intersect s2
        s1.addAll(s2);             // s1 <- s1 union s2
		double dissim = (s1.size()-intersect.size())/(double)s1.size();
        s1.clear(); intersect.clear(); s2.clear();
		return dissim;
	}

    public double computeDissimilarityByDSCWith(AnswerTree other) {
		int[] n1 = this.srcNodes;
		int[] n2 = other.srcNodes;

				// use Dice similarity coefficient (DSC)
        for(int i = 0; i < n1.length; i++) {
       		s1.add(Integer.valueOf(n1[i]));
       		s2.add(Integer.valueOf(n2[i]));
       	}
       	intersect.addAll(s1);	// copy of s1
        intersect.retainAll(s2);    // <- s1 intersect s2
        double dissim = (s1.size()+s2.size()-2*intersect.size())/(double)(s1.size()+s2.size());
        s1.clear(); intersect.clear(); s2.clear();
		return dissim;
    }

	@Override
	public String toString() {
		return "AnswerTree [rootNodeId=" + rootNodeId + ", score=" + score + ", srcNodes=" + Arrays.toString(srcNodes)
				+ "]";
	}

	@Override
	public boolean equals(Object other) {
		AnswerTree at = (AnswerTree)other;
		if (this.rootNodeId != at.rootNodeId) return false;
		if (this.srcNodes.length != at.srcNodes.length) return false;
		for (int i = 0; i < this.srcNodes.length; i++) 
			if (this.srcNodes[i] != at.srcNodes[i]) return false;
		return true;
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

