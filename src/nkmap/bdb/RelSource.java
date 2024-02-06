package nkmap.bdb;

public class RelSource {
//	private int fstNode;
	public int srcNode;
	public float rel;

	@Override
	public String toString() {
		return "RelSource [rel=" + rel + ", srcNode=" + srcNode + "]";
	}

	public RelSource() {}

	public RelSource(float rel2, int srcNode2) { //, int fstNode2) {
		// TODO Auto-generated constructor stub
		rel = rel2; srcNode = srcNode2; //fstNode = fstNode2;
	}

	public RelSource(RelSource r) { //, int fstNode2) {
		// TODO Auto-generated constructor stub
		srcNode = r.getSrcNode(); rel = r.getRel(); //fstNode = fstNode2;
	}
	
	public float getRel() {
		return rel;
	}

	public void setRel(float rel) {
		this.rel = rel;
	}

	public int getSrcNode() {
		return srcNode;
	}
	
	public void setSrcNode(int srcNode) {
		this.srcNode = srcNode;
	}
	
	public void set(int srcNode, float rel) {
		this.srcNode = srcNode;
		this.rel = rel;
	}	
}
