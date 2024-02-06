//
// - Generate a tree of states and perform DFS on the tree using stack in findUninqueAnswer()
//
package queryProcessing.old;

//import temp.TermData;
import util.*;

import java.util.Stack;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
//import java.util.Map.Entry;
//import java.util.PriorityQueue;

import nkmap.bdb.RelSource;
import nkmap.bdb.NKMapRead;
import nkmap.bdb.NKMapReadForJmdb;
import nkmap.bdb.KeywordNode;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import org.jgrapht.util.*;
import util.State;

public class BlinkSearcherBDB_ConstRootDup extends Searcher4 { 
//	HashMap<Integer, TopKNodeData4> nodesTable = new HashMap<Integer, TopKNodeData4>(32);
//	FibonacciHeap<Integer> T;				// global top-k queue T 
	NKMapRead nkmapRead;
	
	Set<Integer> visitedNodes = new HashSet<Integer>();
	KeywordNode nk = new KeywordNode();
	float min_k;
	int[] srcNodes;
	RelSource P[][]; 

	StatePool spool;
	State s0;
    Stack<State> Q = null;
	
    CPUUtil cpuutil = new CPUUtil();
    public long accFindUniqTime = 0;
    long start, end;

	public void initDictionary() {
		String term;
		try {
			BufferedReader br = new BufferedReader(new FileReader(Params.ExpDB + "/termlist.bin"));
			while ((term = br.readLine()) != null) {
				InvertedList l = new InvertedList(term);
				dictionary.put(term, l);	// new TermData(l));
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public BlinkSearcherBDB_ConstRootDup() {
		super();
		initDictionary();	
	}

	public BlinkSearcherBDB_ConstRootDup(DirectedGraph<Integer, DefaultEdge> g) {
		super(g);
		initDictionary();	
        nkmapRead = (Params.ExpDB.equals("res/jmdb")) ? new NKMapReadForJmdb() : new NKMapRead();
        spool = new StatePool(20000);
        Q = new Stack<State>();
	    s0 = new State(-1, -1, -1, null, (float)0, (float)0, (float)0);
	}
	
	public void resetNKMapRead() {
        nkmapRead.close();
        nkmapRead.reset();
	}

	@Override
	public void completeSearch() {
//		nodesTable.clear();
		visitedNodes.clear();
        Q.clear();
        spool.clear();
		min_k = 0;
        accFindUniqTime = 0;
        srcNodes = null;
        P = null;
		super.completeSearch();
	}
		
	public void visitNode(int nodeID, List<String> query) {
		int i, j, k;
		float score_s; 
        State e, s;			
        
        nk.setDestNode(nodeID);				
		for (i = 0; i < numOfQueryKeywords; i++) {
			nk.setKeyword(query.get(i));
			P[i] = nkmapRead.searchRelSourcesInNKMapExt(nk); 

			if (P[i] == null) {
		//	 	System.out.println("There is no path from node "+nodeID+" to keyword "+query.get(i));
                                return;
                        }

	            numOfNKMapLookups += P[i].length;
//            numOfNKMapLookupsByKeyword++;
		}
        Q.push(s0);

        // generate the states of keyword level i > 0;
		while (!Q.empty()) {
			e = Q.pop();
			i = e.i+1;		// level, i.e. keyword index

			if (i < numOfQueryKeywords-1) {	// non-leaf state
			    for (j = P[i].length-1; j >= 0; j--) {	
				    // generate each child state of e
			    score_s = e.score + P[i][j].rel; 	
                    		try {
                        		s = spool.getState(i, j, P[i][j].srcNode, e, score_s, 0, 0);
				    Q.push(s);
	                    } catch (Exception ex) { ex.printStackTrace(); }		
			}
       		     }
            	else {  // leaf state
			    for (j = 0; j < P[i].length; j++) {	
				    score_s = e.score + P[i][j].rel; 	
                		    srcNodes[i] = P[i][j].srcNode; // s.i == i
		                    for (s=e, k=i-1; k >= 0; k--, s=s.sp) 
		                        srcNodes[k] = s.srcNode;
		                    processAnswerTree(nodeID, srcNodes, score_s); 
				numOfAnswerTreeExplored++;

				SumOfScoresOfAnswerTreesExplored += score_s;
                    // numOfStatesChecked++;
			    }
	            }
		}
       	 	Q.clear();
		spool.clear();
	}

    void processAnswerTree(int rootNode, int[] srcNodes, float score) {
    	TopKNodeData4 t, minT;
    	
    	if (T.size() < Params.K) {		
    		int numOfDistinctRoots = rootsOfT.size();
    		if (rootsOfT.contains(rootNode) == false) numOfDistinctRoots++;
    		if ((T.size() + 1 - numOfDistinctRoots)/(double)(Params.K - 1) > Params.rm) {
    			minT = findTreeWhichHasSharedRootAndMinimalScore(rootNode, score);
    			if (minT != null) { // a previous tree in Q is the minimal one
    				// System.out.print("T -> (node " + TNode.getData() + ", " + TNode.getKey() + ") -> ");
    				T.remove(minT); 
    				///nodesTable.remove(removedNodeId);
    				
        			t = new TopKNodeData4(rootNode, srcNodes, score);
//       			nodesTable.put(nodeData.treeId, nodeData);				
//        			System.out.println("(node " + e.nodeID + ", " + score + ") -> T");
        			T.add(t);
        			// rootsOfT.add(rootNode);		// is this needed?
    			}
    		}
    		else {
    			t = new TopKNodeData4(rootNode, srcNodes, score);
//   			nodesTable.put(destNode, nodeData);				
//    			System.out.println("(node " + e.nodeID + ", " + score + ") -> T");
    			T.add(t);
    			rootsOfT.add(rootNode);
    			if (T.size() == Params.K) min_k = (float)T.peek().score;	// increase from 0 to a min value;	
    		}
    	}
		else {
			if (score > min_k) {
				if (rootsOfT.contains(rootNode) == true && 
					(T.size() - rootsOfT.size())/(double)(Params.K - 1) > (Params.rm - 1.0/(Params.K - 1)) ) {
					minT = findTreeWhichHasSharedRootAndMinimalScore(rootNode, score);
	    			if (minT != null) { // a previous tree in Q is the minimal one
	    				// System.out.print("T -> (node " + TNode.getData() + ", " + TNode.getKey() + ") -> ");
	    				T.remove(minT); 
	    				///nodesTable.remove(removedNodeId);
	    				
	        			t = new TopKNodeData4(rootNode, srcNodes, score);
//	       				nodesTable.put(nodeData.treeId, nodeData);				
//	        			System.out.println("(node " + e.nodeID + ", " + score + ") -> T");
	        			T.add(t);
	        			// rootsOfT.add(rootNode);
	    				min_k = (float)T.peek().score;	// increase from 0 to a min value;	
	    			}
	    		}
	    		else {
	    			t = new TopKNodeData4(rootNode, srcNodes, score);
//       			nodesTable.put(destNode, nodeData);				
//        			System.out.println("(node " + e.nodeID + ", " + score + ") -> T");
        			T.add(t);
        			rootsOfT.add(rootNode);
        			
        			//System.out.print("T -> (node " + TNode.getData() + ", " + TNode.getKey() + ") -> ");
        			TopKNodeData4 removed = T.poll();	// remove T_k
    				//nodesTable.remove(removedNodeId);
    				Iterator<TopKNodeData4> it = T.iterator();
    				boolean dupExist = false;
    		    	while (it.hasNext()) {
    		    		if (it.next().rootId == removed.rootId) 
    		    			dupExist = true;  		    		    		    		
    		    	}
    		    	if (dupExist == false) 
    		    		rootsOfT.remove(removed.rootId);
    				min_k = (float)T.peek().score;	// increase from 0 to a min value;	
	    		}
			}
		}    	
//		System.out.println(T.toString());
//		System.out.println("min-k=" + min_k);
    }
    
	private class ET {
		public int treeId; 
		public float score; 
		public TopKNodeData4 tree;
		public ET(int tid, float sc, TopKNodeData4 t) {
			this.treeId = tid; this.score = sc; this.tree = t; 
		}
	}
	
	HashMap<Integer, ET> D = new HashMap<Integer, ET>(Params.K);    	

    public TopKNodeData4 findTreeWhichHasSharedRootAndMinimalScore(int rootNode, float score) {
    	float minSc = 1000;
    	TopKNodeData4 minT = null;    
    	D.put(rootNode, new ET(0, score, null));	// insert current tree into D (treeId = 0)
    	
    	Iterator<TopKNodeData4> it = T.iterator(); 
    	while (it.hasNext()) {
    		TopKNodeData4 t = it.next();
    		if (D.containsKey(t.rootId) == false) { // t is a new tree	
    			D.put(t.rootId, new ET(t.treeId, t.score, t));	// insert t into D    			
    		}
    		else {  // t has a duplicate root with e
    			ET e = D.get(t.rootId);    	    	
    			if (e != null) {	// e has never been checked 
    				if (e.score < minSc) {	// check e
    					minSc = e.score; // minTid = e.treeId;
    					if (e.treeId == 0) minT = null;	// the current tree
    					else minT = e.tree;
    					
    				}
    				D.put(t.rootId, null);  // replace e to avoid double check
    			}
    			if (t.score < minSc) {	// check t
					minSc = t.score; minT = t; // minTid = t.treeId;
				}			
    		}    		
    	}
    	D.clear();
    	return minT;	// null if cur tree is the minimal one or there is no duplicate root tree
    }
    
	public void search(List<String> query) {
		int listIndex, i, j;
		TargetList l;
		ListEntry e;
		float scoreSum;
		
		if (prepareSearch(query) == false) return;
		min_k = 0;
		P = new RelSource[numOfQueryKeywords][];
        srcNodes = new int[numOfQueryKeywords];	// reuse within the below loop

		while(true) {			
			listIndex = getNextListIndex();
			l = targetLists.get(listIndex);
			e = l.getNextEntry();
			if (e == null) { 			// all entries in this list have been read
//					System.out.println("exit since one of the target lists has been read...");
					getResult(); return;	
			}
			numOfAccInUpdatePeriod++;

			i = l.getIndex();
			curScores[i] = e.rel;
		
			if (visitedNodes.contains(e.nodeID) == false) {  // e.nodeId newly found from inverted lists and visited	
				visitNode(e.nodeID, query);
				visitedNodes.add(e.nodeID);
			}			
			
			scoreSum = 0;
			for (j = 0; j < numOfQueryKeywords; j++)
				scoreSum += curScores[j];
			if (min_k >= scoreSum) {		// Note: min_k = 0 if |Qt| < k
				// return top-k nodes in T as the query result
//				System.out.println("exit from step1...");
				getResult();  return;				
			}
		}	// end of while
	}
	
	public void printResult(List<String> q, PrintWriter pw) {
//		List<Integer> destNodes = super.getResult();
//		List<Integer> srcNodes = null;
//        List<Integer> nodes = new ArrayList<Integer>();
		pw.println(q);		
		if (TopKTrees == null) return;
		SumResultRelev = 0;
		double[] score_sorted = new double[TopKTrees.length];
		for (int i = 0; i < TopKTrees.length; i++) {
			int u = TopKTrees[i].rootId;
			int[] srcNodes = TopKTrees[i].srcNodes;
			pw.println("srcNodes: " + Arrays.toString(srcNodes));
			SumResultRelev += TopKTrees[i].score;
			score_sorted[i] = TopKTrees[i].score;

			printAnswerTree(u, srcNodes, pw);
/*			if (printAnswerTree(u, srcNodes, pw) == true) {
                nodes.add(u);
            }
			else
				numOfNonReducedAnswers++;			
*/		}
		Arrays.sort(score_sorted);
/*
		for (int i = 0; i < TopKTrees.length; i++) 
			pw.print(score_sorted[i] + ", ");
		pw.println();	
*/

		numOfDuplicateAnswers = countNumberOfDuplicateAnswers2(TopKTrees);
//		numOfNonredundantAnswers = countNumberOfUniqueAnswers2(nodes, nodesTable);

		pw.println("-----------------------------------------");
		pw.println("ResultDestNodes: "); // + Arrays.toString(ResultDestNodes));
		for (int i = 0; i < TopKTrees.length; i++) 
			pw.print(TopKTrees[i].rootId + ", ");
		pw.println();	

		pw.println("ResultRelevs: "); // + Arrays.toString(ResultRelevs));
		//double DCG = 0.0d, IDCG = 0.0d;
		double logval = 0.0d;
		for (int i = 0; i < TopKTrees.length; i++) {
			pw.print(TopKTrees[i].score + ", ");
			logval = Math.log(i+1.0+1.0)/Math.log(2.0);
			DCG += TopKTrees[i].score/logval;
			DCG2 += ((Math.pow(2, TopKTrees[i].score/TopKTrees[0].score)-1.0)/logval);
		//	IDCG += score_sorted[TopKTrees.length-i-1]/logval;
		}
		DCG = DCG/TopKTrees[0].score;
		//nDCG = DCG / IDCG;

		pw.println();	
		pw.println("Num of top-k answers: " + TopKTrees.length);
		pw.println("Num of duplicate answers: " + numOfDuplicateAnswers);
		pw.println("Num of duplicate roots in top-k answers: " + (TopKTrees.length - rootsOfT.size()));
		pw.println("root duplication ratio: " + ((TopKTrees.length - rootsOfT.size())/(float)(Params.K - 1)));
		pw.println("given max root duplication param: " + Params.rm);
		pw.println("Num of entries retrieved from the lists: " + numOfAccInUpdatePeriod);
		pw.println("Num of entries retrieved from NKMaps: " + numOfNKMapLookups);
		pw.println("Num of answer trees explored: " + numOfAnswerTreeExplored);

	/*
		pw.println("Num of redundant answers in top-k answers: " + numOfNonReducedAnswers);
		pw.println("Num of duplicate answers in top-k answers: " + numOfDuplicateAnswers);
		pw.println("Num of entries retrieved from the lists: " + numOfAccInUpdatePeriod);
		pw.println("Num of redundant candidate answers found: " + numOfNonReducedCandidates);
		pw.println("Num of reduced alternative answers found: " + numOfReducedAlternatives);
		pw.println("Num of duplicate candidate answers found: " + numOfDuplicateCandidates);
		pw.println("Num of unique alternative answers found: " + numOfUniqueAnswersFound);
	*/
//		return destNodes;
	}

	public void close() {
		nkmapRead.close();
	}
	
	Set<String> srcNodesSet = new HashSet<String>();	
	public int countNumberOfDuplicateAnswers2(TopKNodeData4[] TopKTrees) {
		for (TopKNodeData4 u : TopKTrees) {
			StringBuffer sb = new StringBuffer();
			int[] sortedNodes = Arrays.copyOf(u.srcNodes, u.srcNodes.length);
			Arrays.sort(sortedNodes);
			for (int i = 0; i < sortedNodes.length; i++)
				sb.append(sortedNodes[i]);
			srcNodesSet.add(sb.toString());		
		}
        int r = TopKTrees.length - srcNodesSet.size();
        srcNodesSet.clear();
		return r;
	}
/*
	public int countNumberOfDuplicateAnswers() {
//		Set<int[]> srcNodesSet = new HashSet<int[]>();	
		//Set<String> srcNodesSet = new HashSet<String>();	
		Set<Integer> srcNodesSet = new HashSet<Integer>();	
		for (int u : ResultDestNodes) {
			srcNodesSet.add(
                Arrays.hashCode(nodesTable.get(u).sortedSrcNodes)
            );
		}
		return ResultDestNodes.length - srcNodesSet.size();
	}

	public int countNumberOfUniqueAnswers2(List<Integer> nodes, Map<Integer, TopKNodeData2> nodesTable) {
		for (int u : nodes) {
			srcNodesSet.add(
                Arrays.hashCode(nodesTable.get(u).sortedSrcNodes)
            );
		}
		int r = srcNodesSet.size();
        srcNodesSet.clear();
        return r;
	}
*/
}
