package util;

public class QueueNode {
	//// answer tree¿« leaf ≥ÎµÂ 
	int srcNode;
	/////////////////
	float rel;
	QueueNode next;
/*	public QueueNode(float r){
		rel = r; next = null;
	}
*/	public QueueNode(float r, int sNode){
		rel = r; next = null;
		srcNode = sNode;
	}
	public int getSrcNode() {
		return srcNode;
	}
	public void setSrcNode(int sNode) {
		srcNode = sNode;
	}
	public float getRel() {
		return rel;
	}
	public QueueNode getNext() {
		return next;
	}
}