package util;

public class EnhNodeData<T extends EnhRelQueue> extends NodeData<T> {
	public float[] nextRel;						// stores the "rel_next" values of n recently read from each list 
	public int maxValPos;						// position of max value in nextRel[]
	public boolean foundFromAllLists;			// if at least one entry of n was read from every target lists
//	public EnhRelQueue q;	// override q in NodeData
	
	public EnhNodeData(int numOfQueryKeywords, Class<T> clazz) throws Exception {
		// TODO Auto-generated constructor stub
		super(clazz);	
/*		q = new EnhRelQueue();
		nodeHandle = null;
		isScoreSet = false;
*/		nextRel = new float[numOfQueryKeywords];
		for (int i = 0; i < numOfQueryKeywords; i++) nextRel[i] = 0;
		maxValPos = 0;
		foundFromAllLists = (numOfQueryKeywords > 1) ? false : true;
	}
	
	/*public EnhNodeData(int numOfQueryKeywords, double rel) {
		// TODO Auto-generated constructor stub
		super(rel);	
		nextRel = new double[numOfQueryKeywords];
		for (int i = 0; i < numOfQueryKeywords; i++) nextRel[i] = 0;
		maxValPos = 0;
		foundFromAllLists = (numOfQueryKeywords > 1) ? false : true;
	}*/
	public float findMaxNextScore(float[] curScores, int numOfQueryKeywords) {
		assert(this.foundFromAllLists == false);		
		////////modified for IP&M paper ////////
		float maxNextScore = nextRel[this.maxValPos];	
		for (int i = 0; i < numOfQueryKeywords; i++) {
			if (nextRel[i] == 0) { 		// entry of n is not found from list li yet
				if (curScores[i] > maxNextScore) maxNextScore = curScores[i];
			}
			/* else {		
				if (nextRel[i] > maxNextScore) maxNextScore = nextRel[i];	// if nextRel[i] == -1, ignore it	
			} */
		} 	
		return maxNextScore;	
	}
	
	public float findMaxNextScoreAndUpdate(float[] curScores, int numOfQueryKeywords, boolean changeMaxValPos) {
		assert(this.foundFromAllLists == false);		
		float maxNextScore = 0; 	////////modified for IP&M paper ////////
		boolean foundall = true;
				
		if (changeMaxValPos == true) {		////////modified for IP&M paper ////////
			int maxPosInNextRel = 0; float maxOfNextRels = 0;
			for (int i = 0; i < numOfQueryKeywords; i++) {
				if (nextRel[i] == 0) { 	// entry of n is not found from list li yet
					if (curScores[i] > maxNextScore) maxNextScore = curScores[i];
					foundall = false;
				}
				else {					
					if (nextRel[i] > maxNextScore) maxNextScore = nextRel[i];	// if nextRel[i] == -1, ignore it
					if (nextRel[i] > maxOfNextRels) {
						maxOfNextRels = nextRel[i]; maxPosInNextRel = i;
					}
				}
			}
			this.maxValPos = maxPosInNextRel;
		} 	
		////////modified for IP&M paper ////////
		else {
			maxNextScore = nextRel[this.maxValPos];	
			for (int i = 0; i < numOfQueryKeywords; i++) {
				if (nextRel[i] == 0) { 	// entry of n is not found from list li yet
					if (curScores[i] > maxNextScore) maxNextScore = curScores[i];
					foundall = false;
				}
			}
		}
		if (foundall == true) {			// entries of n are found from every target lists
//////// modified for IP&M paper ////////
//			this.maxValPos = Utils.findMaxValPos(nextRel, numOfQueryKeywords);	
			this.foundFromAllLists = true;
		}
		return maxNextScore;		// if nextRel[i] == -1 for all i, maxCur is set to 0
	}
	
	/*public double findMaxCur(double[] curScores, int numOfQueryKeywords, int curListIndex) {
		double maxCur = 0;
		if (this.foundFromAllLists == true) {
			if (curListIndex == this.maxValPos)
				this.maxValPos = Utils.findMaxValPos(nextRel, numOfQueryKeywords);
			maxCur = nextRel[maxValPos];				
		}
		else {
			boolean foundall = true;
			for (int j = 0; j < numOfQueryKeywords; j++) {
				if (nextRel[j] == 0) { 	// entry of n is not found from list li yet
					if (curScores[j] > maxCur) maxCur = curScores[j];
					foundall = false;
				}
				else {
					if (nextRel[j] > maxCur) maxCur = nextRel[j];
				}
			} 	
			if (foundall == true) {			// entries of n are found from every target lists
				this.maxValPos = Utils.findMaxValPos(nextRel, numOfQueryKeywords);
				this.foundFromAllLists = true;
			}
		}
		return maxCur;
	}*/
}
