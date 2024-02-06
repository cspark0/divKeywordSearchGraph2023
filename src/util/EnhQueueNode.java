package util;

public class EnhQueueNode extends QueueNode {
	short listIndex;
	
	public EnhQueueNode(float r, int sNode, short index) {
		super(r, sNode);
		listIndex = index;
	}
	public short getListIndex() {
		return listIndex;
	}
}
