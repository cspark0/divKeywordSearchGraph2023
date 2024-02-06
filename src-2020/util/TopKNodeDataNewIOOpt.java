package util;

import org.jgrapht.util.FibonacciHeap;
import nkmap.bdb.RelSource;
import com.sleepycat.je.Cursor;

public class TopKNodeDataNewIOOpt extends TopKNodeDataNewIOOpt2 {	
    public FibonacciHeap stateQ;

	public TopKNodeDataNewIOOpt(int[] srcNodes, int[] sortedSrcNodes, NodeScore ns, FibonacciHeap q, StateNew s, RelSource[][] P, Cursor[] curs) {
	    super(srcNodes, sortedSrcNodes, ns, s, P, curs);
        this.stateQ = q;
    }
}
