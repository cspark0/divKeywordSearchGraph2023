package util;

import org.jgrapht.util.FibonacciHeap;

public class TopKNodeDataNew {	
//	public int destNode;
	public int[] srcNodes;
	public int[] sortedSrcNodes;
//	public int hashcode;
	public NodeScore ns;
    public FibonacciHeap stateQ;
    public StateNew solState;

	public TopKNodeDataNew(int[] srcNodes, int[] sortedSrcNodes, NodeScore ns, FibonacciHeap q, StateNew s) {
//			FibonacciHeapNode<Integer> heapNode) {
		super();
		this.srcNodes = srcNodes;
//		this.hashcode = hashcode;
		this.sortedSrcNodes = sortedSrcNodes;
		this.ns = ns;
        this.stateQ = q;
        this.solState = s;
	}	
}
