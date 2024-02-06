package util;

import org.jgrapht.util.FibonacciHeap;
import nkmap.bdb.RelSource;
import com.sleepycat.je.Cursor;

public class TopKNodeDataNewIOOpt2 {	
//	public int destNode;
	public int[] srcNodes;
	public int[] sortedSrcNodes;
//	public int hashcode;
	public NodeScore ns;
    public StateNew solState;
    public RelSource[][] P; 
    public Cursor[] curs;

	public TopKNodeDataNewIOOpt2(int[] srcNodes, int[] sortedSrcNodes, NodeScore ns, StateNew s, RelSource[][] P, Cursor[] curs) {
//			FibonacciHeapNode<Integer> heapNode) {
//		super();
//		this.hashcode = hashcode;
		this.ns = ns;
        this.solState = s;
        this.P = P;
        this.curs = curs;

        this.srcNodes = new int[srcNodes.length];
        this.sortedSrcNodes = new int[sortedSrcNodes.length];
        for (int i = 0; i < srcNodes.length; i++) {
            this.srcNodes[i] = srcNodes[i];
            this.sortedSrcNodes[i] = sortedSrcNodes[i];
        }
    }

	public void set(int[] srcNodes, int[] sortedSrcNodes, StateNew sol) { // NodeScore ns, FibonacciHeap q, RelSource[][] P, Cursor[] curs) {
//		this.ns = ns;
        this.solState = sol;
//        this.P = P;
//        this.curs = curs;

        for (int i = 0; i < srcNodes.length; i++) {
            this.srcNodes[i] = srcNodes[i];
            this.sortedSrcNodes[i] = sortedSrcNodes[i];
        }
	}	
}
