package util;

public class StateNew { // implements Comparable<State> {
	public int i; //, j, srcNode;
//    public State sp;
    public int[] indJ;
	public float score, ub; //, lb;
    public boolean solChecked;
//    public State next;

	public StateNew(int numOfKeywords) {
        indJ = new int[numOfKeywords];
    }
/*
	public State(State n) {
		this.next = n;
    }
	public StateNew(int i, int[] indexJ, float score, float ub, boolean ch) {
		this.i = i;
//		this.indexJ = indexJ;
		this.score = score;
		this.ub = ub;
        for (int k = 0; k < indexJ.length; k++) this.indexJ[k] = indexJ[k];
        this.solChecked = ch;
    }

*/
	public void set(int i, int[] indexJ, float score, float ub, boolean ch) {
		this.i = i;
//		this.indexJ = indexJ;
		this.score = score;
		this.ub = ub;
        if (indexJ == null) {
            for (int k = 0; k < indJ.length; k++) this.indJ[k] = 0;
        }
        else 
            for (int k = 0; k < indexJ.length; k++) this.indJ[k] = indexJ[k];
        this.solChecked = ch;
    }

/*
	public State(int i, int j, float score, float ub, float lb, int srcNode, int[] nodes) {
		this.i = i;
		this.j = j;
//		this.sp = sp;
		this.score = score;
		this.ub = ub;
		this.lb = lb;
        this.srcNodes = new int[nodes.length+1];
        int k = 0;
        for (; k < nodes.length; k++) this.srcNodes[k] = nodes[k];
        this.srcNodes[k] = srcNode;
    }

	public void set(int i, int j, float score, float ub, float lb) {
		this.i = i;
		this.j = j;
//		this.sp = sp;
		this.score = score;
		this.ub = ub;
		this.lb = lb;
        this.srcNodes = new int[0]; 
    }

	public void set(int i, int j, int srcNode, State sp,float score, float ub, float lb) { //, int srcNode, int[] nodes) {
		this.i = i;
		this.j = j;
		this.srcNode = srcNode;
		this.sp = sp;
		this.score = score;
		this.ub = ub;
		this.lb = lb;
   //     this.srcNodes = new int[nodes.length+1];
   //   int k = 0;
   //   for (; k < nodes.length; k++) this.srcNodes[k] = nodes[k];
   //   this.srcNodes[k] = srcNode;
    }
	public void set(int i, int j, boolean ch, State sp,float score, float ub, float lb) { //, int srcNode, int[] nodes) {
		this.i = i;
		this.j = j;
		this.solChecked = ch;
		this.sp = sp;
		this.score = score;
		this.ub = ub;
		this.lb = lb;
    }
*/
/*
	public State getNext() {
		return this.next;
    }
	public void setNext(State n) {
		this.next = n;
    }
*/
/*
	public State(int i, int j, State sp, float score, float ub, float lb) {
		super();
		this.i = i;
		this.j = j;
		this.sp = sp;
		this.score = score;
		this.ub = ub;
		this.lb = lb;
	}	
*/
/*
	public int compareTo(State other) {
		return Float.compare(other.ub, this.ub);	// reverse order of ub        
	}
*/

}
