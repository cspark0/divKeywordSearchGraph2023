package util;

class QueueNodePool {
	protected QueueNode head;
	public QueueNodePool(){head = null;}
	
/*	public QueueNode createQueueNode(float r) {
		if (head == null) 
			return new QueueNode(r);
		else {
			QueueNode n = head; head = head.next;
			n.rel = r; n.next = null;
			return n;
		}
	}
*/	public QueueNode createQueueNode(float r, int sNode) {
		if (head == null) 
			return new QueueNode(r, sNode);
		else {
			QueueNode n = head; head = head.next;
			n.rel = r; n.srcNode = sNode; n.next = null;
			return n;
		}
	}
	public void returnQueueNodes(QueueNode first) {
		QueueNode n = first;	// first != null
		while (n.next != null) n = n.next;
		n.next = head;
		head = first;
	}
}