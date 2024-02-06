package util;

public class NKNodeData<T extends EnhRelQueue> extends NodeData<T> {
	public float[] nextRel;						// stores the "rel_next" values of n recently read from each list 
	public int maxValPos;						// position of max value in nextRel[]
	
	public NKNodeData(int numOfQueryKeywords, Class<T> clazz) throws Exception {
		super(clazz);	
		nextRel = new float[numOfQueryKeywords];
	}
}
