package util;

import org.jgrapht.util.FibonacciHeapNode;

public class NodeData<T extends RelQueue> {

	//	public int nodeID;
	public enum NodeStatus {InT, InC };	

	public T q;
	public FibonacciHeapNode<Integer> nodeHandle;	// handle to the entry in the priority queue T or C
	public NodeStatus status;
	public boolean isScoreSet;
	
	public NodeData(Class<T> clazz) throws Exception{
		// nodeID = id;
		q = clazz.newInstance();
		nodeHandle = null;
		isScoreSet = false;
	}
	
	/*public NodeData(double r) {		// with the first relevancy value
		// nodeID = id;
		q = new RelQueue(r);
		nodeHandle = null;
		isScoreSet = false;
	}*/
	
	/*public void deleteRelQueue() {
		q.clear(); q = null;
	}*/
	public void clear() {
		q.clear(); q = null;
		nodeHandle = null;
//		status = NodeStatus.Removed;
	}
	
///////////////// modified for IP&M paper //////////////////
	public double getWS() {
		return q.getSumOfRels();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "NodeData of " + nodeHandle.getData() + "[q=" + q + ", isScoreSet=" + isScoreSet + "]";
	}
}
