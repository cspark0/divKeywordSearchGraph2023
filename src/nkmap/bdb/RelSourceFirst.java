package nkmap.bdb;

public class RelSourceFirst {
	public int fstNode;
	public int srcNode;
	public float rel;

	@Override
	public String toString() {
		return "RelSource [rel=" + rel + ", srcNode=" + srcNode + "]";
	}

	public RelSourceFirst() {}

	public RelSourceFirst(float rel2, int srcNode2) { //, int fstNode2) {
		// TODO Auto-generated constructor stub
		rel = rel2; srcNode = srcNode2; //fstNode = fstNode2;
	}

	public float getRel() {
		return rel;
	}

	public void setRel(float rel) {
		this.rel = rel;
	}

	public int getFstNode() {
		return fstNode;
	}

	public void setFstNode(int fstNode) {
		this.fstNode = fstNode;
	}

	public int getSrcNode() {
		return srcNode;
	}
	
	public void setSrcNode(int srcNode) {
		this.srcNode = srcNode;
	}
	
	public void set(int fstNode, int srcNode, float rel) {
		this.fstNode = fstNode;
		this.srcNode = srcNode;
		this.rel = rel;
	}	
}
