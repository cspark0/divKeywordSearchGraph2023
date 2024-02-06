package util;

public class NKNodeDataCQ<T extends CQRelQueue> extends NKNodeData<T> {
	public double sumOfFirstRels = 0;		// for CQ: sum of max rel of each query keyword found from Rel Loopup Table
	public QueueNode[] firstEntries;
	
	public NKNodeDataCQ(int numOfQueryKeywords, Class<T> clazz) throws Exception {
		super(numOfQueryKeywords, clazz);
	}
	
	@Override
	public double getWS() {
		return (q.getSumOfRels() + sumOfFirstRels);
	}
}
