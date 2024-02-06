package util;

import java.util.Arrays;

//import org.jgrapht.util.FibonacciHeapNode;

public class TopKNodeData4 {
	private static int idGen = 0;
	
	public int treeId;
	public int rootId;
	public int[] srcNodes;
	public float score;
	//	public NodeScore ns;

	public TopKNodeData4(int rootId, int[] srcNodes, float score) {
		super();
		this.treeId = ++idGen;
		this.rootId = rootId;
		this.srcNodes = Arrays.copyOf(srcNodes, srcNodes.length);
		this.score = score;
		//this.ns = ns;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TopKNodeData4 other = (TopKNodeData4) obj;
		if (treeId != other.treeId)
			return false;
		return true;
	}	
}
