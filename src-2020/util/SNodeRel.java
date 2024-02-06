package util;

public class SNodeRel {
	int SNodeId;
	double rel;

	public SNodeRel(int snodeId, double r) {
		SNodeId = snodeId; rel = r;
	}
	
	public int getSNodeId() {
		return SNodeId;
	}

	public void setSNodeId(int sNodeId) {
		SNodeId = sNodeId;
	}

	public double getRel() {
		return rel;
	}

	public void setRel(double rel) {
		this.rel = rel;
	}
}
