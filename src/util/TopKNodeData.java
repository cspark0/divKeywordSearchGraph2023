package util;

import org.jgrapht.util.FibonacciHeapNode;

public class TopKNodeData {
//	int destNode;
	public int[] srcNodes;
	public int[] sortedSrcNodes;
	public FibonacciHeapNode<Integer> heapNode;
	
	public TopKNodeData(int[] srcNodes, int[] sortedSrcNodes,
			FibonacciHeapNode<Integer> heapNode) {
		super();
		this.srcNodes = srcNodes;
		this.sortedSrcNodes = sortedSrcNodes;
		this.heapNode = heapNode;
	}	
}
