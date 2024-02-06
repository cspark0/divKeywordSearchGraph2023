package util;

import org.jgrapht.util.FibonacciHeap;

public class TopKNodeDataMH{	
//	public int destNode;
	public int[] srcNodes;
	public int[] sortedSrcNodes;
//	public int hashcode;
	public NodeScore ns;
    public MinHeap stateQ;
    public State solState;

	public TopKNodeDataMH(int[] srcNodes, int[] sortedSrcNodes, NodeScore ns, MinHeap q, State s) {
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
