package nkmap.bdb;

public class KeywordNode {
	private String keyword;		// keyword ¸ÕÀú!! sort by (keyword, destNode)
	private int destNode;

	public KeywordNode() {}

	public KeywordNode(int destNode2, String keyword2) {
		// TODO Auto-generated constructor stub
		destNode = destNode2; keyword = keyword2;
	}

	public int getDestNode() {
		return destNode;
	}

	public void setDestNode(int destNode) {
		this.destNode = destNode;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
	public void setDestNodeAndKeyword(String keyword, int destNode) {
		this.keyword = keyword;
		this.destNode = destNode;
	}

	public void set(String keyword, int destNode) {
		// TODO Auto-generated method stub
		this.keyword = keyword;
		this.destNode = destNode;
	}
}
