package util;

import org.jgrapht.util.FibonacciHeapNode;

public class IntegerWithBS {
	public Integer nodeId;
	public float maxCur;
	public FibonacciHeapNode<Integer> origin;		// the node in T/C from which this T2Node is derived
	public FibonacciHeapNode<IntegerWithBS> next;

	public IntegerWithBS(Integer nid, float m, FibonacciHeapNode<Integer> or) {
		nodeId = nid; 
		maxCur = m;
		origin = or;
		next = null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "";
	}
}
