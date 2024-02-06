package util;

//import org.jgrapht.util.FibonacciHeapNode;

public class TopKNodeData2 {	
//	public int destNode;
	public int[] srcNodes;
	public int[] sortedSrcNodes;
	public NodeScore ns;

	public TopKNodeData2(int[] srcNodes, int[] sortedSrcNodes, NodeScore ns) {
//			FibonacciHeapNode<Integer> heapNode) {
		super();
		this.srcNodes = srcNodes;
		this.sortedSrcNodes = sortedSrcNodes;
		this.ns = ns;
	}	
}
