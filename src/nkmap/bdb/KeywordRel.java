package nkmap.bdb;

public class KeywordRel {
	private String keyword;		// keyword ¸ÕÀú!! sort by (keyword, destNode)
	private float rel;
	
	public KeywordRel(String keyword, float rel) {
		super();
		this.keyword = keyword;
		this.rel = rel;
	}
	public KeywordRel() {
		// TODO Auto-generated constructor stub
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public float getRel() {
		return rel;
	}
	public void setRel(float rel) {
		this.rel = rel;
	}
	public void set(String readString, float readFloat) {
		// TODO Auto-generated method stub
		this.keyword = readString;
		this.rel = readFloat;
	}	
}
