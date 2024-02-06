package util;

class EnhQueueNodePool extends QueueNodePool {
//	protected EnhQueueNode head;
	public EnhQueueNodePool(){ super(); }

	public EnhQueueNode createQueueNode(float r, int sNode, short index) {
		if (head == null) 
			return new EnhQueueNode(r, sNode, index);
		else {
			EnhQueueNode n = (EnhQueueNode)head; head = head.next;
			n.rel = r; n.srcNode = sNode; n.listIndex = index; n.next = null;
			return n;
		}
	}
}