//
// - Generate minimal # of states by storing NKMap entry indexes in each state
// - Maintaining P and Curs for each n to avoid repeatitive read of NKMap
// - But not maintaining Priority Q for each n and the states of previous exec of findUninqueAnswer()
//
package queryProcessing.old;

//import temp.TermData;
import util.*;
//import util.StateNew;

import java.util.Stack;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;

import nkmap.bdb.RelSource;
import nkmap.bdb.NKMapRead;
import nkmap.bdb.NKMapReadForJmdbOpt;
import nkmap.bdb.KeywordNode;
import nkmap.bdb.RelSourceFirst;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.util.*;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;
import org.jgrapht.util.FibonacciHeapNodePool;

import com.sleepycat.je.Cursor;

public class BlinkSearcherBDB_NewIOOpt2 extends Searcher3 {

	HashMap<Integer, TopKNodeDataNewIOOpt2> nodesTable = new HashMap<Integer, TopKNodeDataNewIOOpt2>(32);
//	FibonacciHeap<Integer> T;				// global top-k queue T 
	NKMapRead nkmapRead;
	
	Set<Integer> visitedNodes = new HashSet<Integer>();
//	MinHeap Q = new MinHeap(1000);
	FibonacciHeap<StateNew> Q = new FibonacciHeap<StateNew>(); 	 // priority queue for state space search
//	List<State> C = new ArrayList<State>();		// closed set
	KeywordNode nk = new KeywordNode();
	float min_k;
    int gNextDupId;    // store return value from findUniqueAnswer()
	float[] nkmapEntRels; float[] sum_best; //float[] sum_worst;
    int[] srcNodes;
	int[] sortedNodes;
    int[] in;
    int[] indJ;
//	RelSource P[][]; 
	StatePoolNew spool;
	FibonacciHeapNodePool<StateNew> pool;
	StateNew s0;
   // State bestSolution = new State(-1, -1, null, (float)0, Float.MAX_VALUE, (float)0);
//	FibonacciHeapNode<State> node_s0;
	
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

	public BlinkSearcherBDB_NewIOOpt2() {
		super();
		initDictionary();	
	}

	public BlinkSearcherBDB_NewIOOpt2(DirectedGraph<Integer, DefaultEdge> g) throws Exception {
		super(g);
		initDictionary();	
        nkmapRead = (Params.ExpDB.equals("res/jmdb")) ? new NKMapReadForJmdbOpt() : new NKMapRead();
//	    s0 = new State(-1, -1, -1, null, (float)0, Float.MAX_VALUE, (float)0);
//	    s0 = new StateNew(-1, new int[0], (float)0, Float.MAX_VALUE);
//      spool = new StatePool(12500);
//      pool = new FibonacciHeapNodePool<State>(12500, spool);
        spool = new StatePoolNew(10000, 4);
        pool = new FibonacciHeapNodePool<StateNew>(10000);
	}
	
	public void resetNKMapRead() {
        nkmapRead.close();
        nkmapRead.reset();
	}

	@Override
	public void completeSearch() {

        // close Q and database cursors opened
		Set<Entry<Integer, TopKNodeDataNewIOOpt2>> eSet = nodesTable.entrySet();
		Iterator<Entry<Integer, TopKNodeDataNewIOOpt2>> iter = eSet.iterator();
		while (iter.hasNext()) {		// iterate all top-k nodes in Qt
			Entry<Integer, TopKNodeDataNewIOOpt2> e = iter.next();
            Cursor[] curs = e.getValue().curs;
            if (curs != null) {       // then curs != null either
                for (int i = 0; i < numOfQueryKeywords; i++) curs[i].close();
            }
		}
		nodesTable.clear();
		visitedNodes.clear();
		Q.clear(); //C.clear();
        pool.clear();
        spool.clear();
		min_k = 0;

		super.completeSearch();
	}
		
	public void visitNode(int[] srcNodes, ListEntry e, List<String> query, int curInd) {
		float score = (float)0;		
		boolean areAllfNodesTheSame = true;
		int i;
		
		nk.setDestNode(e.nodeID);
		for (i = 0; i < numOfQueryKeywords; i++) {	// look up the first NKMap
			if (i == curInd) {
				srcNodes[i] = e.sNodeID;	// n.addSrcNode(e.sNodeID);
				score += (nkmapEntRels[i] = e.rel);
				continue;
			}
			nk.setKeyword(query.get(i));
			RelSourceFirst rsf = nkmapRead.searchRelSourceFirst(nk); numOfNKMapLookups++;
			if (rsf == null) { 
//				System.out.println("There is no path from node "+node+" to keyword "+query.get(i));
//				srcNodes.clear();
				return;
			}
			if (rsf.getFstNode() != e.fNodeID) areAllfNodesTheSame = false;
			srcNodes[i] = rsf.srcNode;	// n.addSrcNode(rsf.getSrcNode());
			score += (nkmapEntRels[i] = rsf.rel);
		}

        if (score <= min_k) return;

		if (areAllfNodesTheSame == true) {	// have to look up the second NKMap
			
		// **** findReducedAnswer Algorithm *******************************
//			System.out.println("++++++ non-reduced answer found!! ++++++");
			numOfNonReducedCandidates++;
			int newSrcNode = 0, minIndex = -1;
			float scoreDiff, minScoreDiff =(float)Float.MAX_VALUE;		
//			float newRel = (float)0;
			for (i = 0; i < numOfQueryKeywords; i++) {	
				// look up the second NKMap
				nk.setKeyword(query.get(i));
				RelSource new_rs = nkmapRead.searchRelSourceIn2ndNKMap(nk); numOfNKMapLookups++;
				if (new_rs == null) continue;				
				
				// check the score of new combination of src paths and find the best 
				// newScore = score - nkmapEntRels[i] + new_rs.rel;
				scoreDiff = nkmapEntRels[i] - new_rs.rel;
				if (scoreDiff < minScoreDiff) {
					minScoreDiff = scoreDiff;
					minIndex = i;
					newSrcNode = new_rs.srcNode;
//					newRel = new_rs.rel;
				}
			}
			if (minIndex == -1) {	// no alternative reduced answer tree
//				srcNodes.clear();
				return;
			}
			numOfReducedAlternatives++;
			srcNodes[minIndex] = newSrcNode;	// replace srcNodeID at minIndex
//			System.out.println("++++++****** instead of score " + score 
//				+ ", a reduced answer found with score " + (score -= minScoreDiff));
		// **** findReducedAnswer Algorithm *******************************
		}
//		insertIntoTopKQueue(e.nodeID, srcNodes, score);
		
        if (score <= min_k) return;

		// duplication check 
		int curId = e.nodeID, removedId, dupId; 
		float curScore = score, newScore, dupScore = (float)0;
        StateNew sol = null, prevSol;
        Cursor[] curs = null;
        RelSource[][] P = null;
    	TopKNodeDataNewIOOpt2 nodeData = null, dupNodeData = null;
        NodeScore ns = null;
		//sortSrcNodes(srcNodes, sortedNodes);	// sort srcNodesIDs
        for (i = 0; i < srcNodes.length; i++) sortedNodes[i] = srcNodes[i];
		Arrays.sort(sortedNodes);
		dupId = checkDuplicateAnswer(curId, sortedNodes, curScore);
        if (dupId == 0) {   // no duplication found!
            // insert curId node
            ns = new NodeScore(curId, curScore);
            insertIntoTopKQueue(new TopKNodeDataNewIOOpt2(srcNodes, sortedNodes, ns, sol, P, curs));
            return;
        }

        if (dupId == curId) {	// curId is a duplicate answer
//            System.out.println("A duplicate answer found, rooted at " + curId);
            numOfDuplicateCandidates++;

            // first try: sol == null
            P = new RelSource[numOfQueryKeywords][];
            curs = new Cursor[numOfQueryKeywords];
            sol = findUniqueAnswer(curId, P, curs, /*curScore,*/ null, query, srcNodes, sortedNodes);

            if (sol == null) return;    // no unique answer for curId

            // unique and superior answer found! srcNodes were changed.
//            System.out.println("A unique answer found for " + curId);
            numOfUniqueAnswersFound++;
            curScore = sol.ub; // > min_k

            if (gNextDupId == 0) { // no duplication found!!
                // insert curId node
                ns = new NodeScore(curId, curScore);
                insertIntoTopKQueue(new TopKNodeDataNewIOOpt2(srcNodes, sortedNodes, ns, sol, P, curs));
                return;
            }
            dupId = gNextDupId;        // returned from findUniqueAnswer()
//            System.out.println("A duplicate answer found, rooted at " + dupId);
            numOfDuplicateCandidates++;
        }
        else {
//            System.out.println("A duplicate answer found, rooted at " + dupId);
            numOfDuplicateCandidates++;
        }

        // curId is not a duplicate answer, but dupId is a duplicate answer!
        // remove dup node first
        dupNodeData = nodesTable.remove(dupId);
	    T.remove(dupNodeData.ns);

        // insert curId node
        ns = new NodeScore(curId, curScore);
        insertIntoTopKQueue(new TopKNodeDataNewIOOpt2(srcNodes, sortedNodes, ns, sol, P, curs));

        while (true) {

            if (dupNodeData.ns.score <= min_k) {
                if (dupNodeData.curs != null) {     
                    for (i = 0; i < numOfQueryKeywords; i++) 
                        dupNodeData.curs[i].close();
                }
                break;
            }

            if (dupNodeData.solState == null) {        // first try
		        dupNodeData.P = new RelSource[numOfQueryKeywords][];
                dupNodeData.curs = new Cursor[numOfQueryKeywords];
            }
    		sol = findUniqueAnswer(dupId, dupNodeData.P, dupNodeData.curs, /*ns.score,*/ dupNodeData.solState, query, srcNodes, sortedNodes);
			//if (newScore == (float)-1)		// no unique answer for dupId
            if (sol == null) break;
			// else {		// unique answer found! srcNodes were changed.
//			System.out.println("A unique answer found for " + dupId);
			numOfUniqueAnswersFound++;
//			curId = dupId; curScore = sol.ub;
			// }
            if (gNextDupId == 0) { // no duplication found!!
                // insert dupId node
                dupNodeData.ns.setScore(sol.ub);
                dupNodeData.set(srcNodes, sortedNodes, sol);
                insertIntoTopKQueue(dupNodeData);
                break;
            }

            // another duplication found!!
//            System.out.println("A duplicate answer found, rooted at " + gNextDupId);
            numOfDuplicateCandidates++;

            // remove next Dup Node first
			nodeData = nodesTable.remove(gNextDupId);
			T.remove(nodeData.ns);

            // insert dupId node
            dupNodeData.ns.setScore(sol.ub);
            dupNodeData.set(srcNodes, sortedNodes, sol);
            insertIntoTopKQueue(dupNodeData);

            dupId = gNextDupId;        // returned from findUniqueAnswer()
            dupNodeData = nodeData;
        }
	}
	
	public void insertIntoTopKQueue(TopKNodeDataNewIOOpt2 nodeData) {
	//public int insertIntoTopKQueue(NodeScore ns, int[] srcNodes, int[] sortedSrcNodes, FibonacciHeap<StateNew> stateQ, StateNew solState, RelSource[][] P, Cursor[] curs) {
		// return 0 or destNodeID removed from Q
//			FibonacciHeapNode<Integer> newTNode = new FibonacciHeapNode<Integer>(destNode);
			T.add(nodeData.ns);	
			nodesTable.put(nodeData.ns.nodeId, nodeData);
//			System.out.println("(node " + e.nodeID + ", " + score + ") -> T");

			if (T.size() == Params.K) {		// T becomes now full
				min_k = (float)T.peek().score;	// increase from 0 to a min value;
			}
			else if (T.size() == Params.K + 1) {	// T was already full, thus should remove min node	
//				System.out.print("T -> (node " + TNode.getData() + ", " + TNode.getKey() + ") -> ");
				NodeScore removed = T.poll();        // delete min-k node
				min_k = (float)T.peek().score;	// increased

                // there is a removed min-k node which is not dupicated --> remove entry from nodesTable
                TopKNodeDataNewIOOpt2 n = nodesTable.remove(removed.nodeId);

                if (n.curs != null) {
                    for (int i = 0; i < numOfQueryKeywords; i++) n.curs[i].close();
                }
//			    return ns2.nodeId;   // return removed nodeData
			}
//			System.out.println(T.toString());
//			System.out.println("min-k=" + min_k);
//        return -1;
	}	
	
	//int checkDuplicateAnswer(int destNode, int[] srcNodes, float rel) {
	int checkDuplicateAnswer(int destNode, int[] sortedSrcNodes, float rel) {
		// return root id of a duplicate and inferior answer; 0 if no duplicate answer
//		int[] theSortedNodes = sortSrcNodes(srcNodes);	// sort srcNodesIDs
		
		Set<Entry<Integer, TopKNodeDataNewIOOpt2>> eSet = nodesTable.entrySet();
		Iterator<Entry<Integer, TopKNodeDataNewIOOpt2>> iter = eSet.iterator();
		while (iter.hasNext()) {		// iterate all top-k nodes in Qt
			Entry<Integer, TopKNodeDataNewIOOpt2> e = iter.next();
		    //if (Arrays.equals(e.getValue().sortedSrcNodes, theSortedNodes) == true) {
		    if (Arrays.equals(e.getValue().sortedSrcNodes, sortedSrcNodes) == true) {
				// a top-k node e found which has the same srcNodes to this one
				if (rel <= e.getValue().ns.score)	// relevance is no greater than e
					return destNode;	// This is a duplicate answer!!!
				else 	// e is a duplicate answer. 
					return e.getKey();	// destNodeID of the other duplicate answer
			}
		}
		return 0;		// There is no duplicate answer
	}
	
	public StateNew findUniqueAnswer(int destNode, RelSource[][] P, Cursor[] curs, /*float prevScore,*/ StateNew prevSol, List<String> query, int[] srcNodes, int[] sortedSrcNodes) {  // srcNodes and sortedSrcNodes will be returned 
		int i, j, k, dupId;
//		boolean bestSolutionFound = false;
		//RelSource P[][] = new RelSource[l][];

		float score_s, ub_s;  //, sc, prevsc2, prevsc3, prevsc4;
//        bestSolution.set(-1, -1, null, (float)0, Float.MAX_VALUE, (float)0);
        StateNew bestSol = null, e, s = null;
        RelSource rs = null;
        
        if (prevSol != null && prevSol.ub <= min_k) {
//            Q.clear();
            for (i = 0; i < curs.length; i++) curs[i].close();
            return null;
        }

//		float UB = prevScore; 
		float LB = min_k;    

        if (prevSol == null) { // first try to find a unique answer

            nk.setDestNode(destNode);
            for (i = 0; i < numOfQueryKeywords; i++) {
                curs[i] = nkmapRead.getCursorForSearchNKMapExt(query.get(i));
			    nk.setKeyword(query.get(i));
                rs = nkmapRead.searchNKMapExtForFirstData(nk, curs[i]);
                P[i] = new RelSource[curs[i].count()];
                P[i][0] = rs;
                for (j = 1; j < P[i].length; j++) P[i][j] = null; 
                numOfNKMapLookups++;
            }
        }

        for (i = numOfQueryKeywords-1; i > 0; i--) {
             sum_best[i-1] = sum_best[i] + P[i][0].rel;
     //      sum_worst[k-1] = sum_worst[k] + P[k][P[k].length-1].rel;
        }

            // build the first state 
//          score_s = P[0][0].rel; 	
        ub_s = P[0][0].rel + sum_best[0]; 	
//      lb_s = score_s + sum_worst[0];
        try {   // solChecked = true; s is always not a unique answer!
             s = spool.getState(0, null, P[0][0].rel, ub_s, true);
             Q.insert(pool.getFibonacciHeapNode(s), -ub_s);
        } catch (Exception ex) { ex.printStackTrace(); }

		while (!Q.isEmpty()) {
			FibonacciHeapNode<StateNew> n = Q.removeMin();
			e = n.getData();
//			e = Q.poll();
			if (e.ub <= LB) { 
//    			pool.returnFibonacciHeapNode(n);
 //               spool.returnState(e);
                break;
            }

            // here e.ub > LB
            
            // here, e is already checked and not a unique solution!
            i = e.i; 
            while (true) {
                // make the next sibling node of e
                j = e.indJ[i]+1;
                if (j < P[i].length) {

                    if (P[i][j]==null) {
			            nk.setKeyword(query.get(i));
                        P[i][j] = nkmapRead.searchNKMapExtForNextData(nk, curs[i]);
                        numOfNKMapLookups++;
                    }

                    score_s = e.score - P[i][j-1].rel + P[i][j].rel;
                    ub_s = score_s + sum_best[i];
                    if (ub_s > LB) {
                        if (prevSol != null && ub_s > prevSol.ub) {
                            try {
                                s = spool.getState(i, e.indJ, score_s, ub_s);
                                s.indJ[i]++;
                                Q.insert(pool.getFibonacciHeapNode(s), -ub_s);
                            } catch (Exception ex) { ex.printStackTrace(); }
                        }
                        else {
                            k = numOfQueryKeywords - 1;
                            for (; k > i; k--) 
                                sortedSrcNodes[k] = P[k][0].srcNode;
                            sortedSrcNodes[i] = P[i][j].srcNode;  // k == e.i
                            for (k--; k >= 0; k--)    // k == e.i
                                sortedSrcNodes[k] = P[k][e.indJ[k]].srcNode;
                            Arrays.sort(sortedSrcNodes);
                            numOfStatesChecked++;
                   
                            dupId = checkDuplicateAnswer(destNode, sortedSrcNodes, ub_s);
                            if (dupId != destNode) {    // this is a new best unique solution state!
                                try {
                                   bestSol = spool.getState(i, e.indJ, score_s, ub_s);
                                   bestSol.indJ[i]++;
                                } catch (Exception ex) { ex.printStackTrace(); }
                                LB = ub_s;
                                gNextDupId = dupId;    // for returning dupId
                            }
                            else {  // this is not a unique answer!
                                try {
                                   s = spool.getState(i, e.indJ, score_s, ub_s);
                                   s.indJ[i]++;
                                   Q.insert(pool.getFibonacciHeapNode(s), -ub_s);
                                } catch (Exception ex) { ex.printStackTrace(); }
                            }
                        }
                    }
                }

                if (++i == numOfQueryKeywords) break; 
//              if (i < numOfQueryKeywords) { // e was a non-leaf state
                // make the first child node of e, i.e. copy of e
                e.score += P[i][0].rel;
                e.i++;
//              lb_s = score_s + sum_worst[i];
            }
        }

        if (bestSol != null) {
            k = numOfQueryKeywords - 1;
            for (; k > bestSol.i; k--) 
                srcNodes[k] = sortedSrcNodes[k] = P[k][0].srcNode;
            for (; k >= 0; k--)    // k == e.i
                srcNodes[k] = sortedSrcNodes[k] = P[k][bestSol.indJ[k]].srcNode;
            Arrays.sort(sortedSrcNodes);
        }
		else { // no duplicate-free answer
            for (i = 0; i < numOfQueryKeywords; i++) curs[i].close();
        }
        Q.clear();
    	return bestSol; 
	}

/*
	int isSolutionState(int destNode, int srcNode, State sp, RelSource[][] P, float score) {
		int i = numOfQueryKeywords - 1; 
		sortedSrcNodes[i--] = srcNode;
		for (State s = sp; i >= 0; s = s.sp, i--) // from the parent state, move up to init state
			sortedSrcNodes[i] = P[i][s.j].getSrcNode();	// s.i == i
//		if (checkRedundantAnswer(destNode, sortedSrcNodes) == true) 
//			return false;
        return checkDuplicateAnswer(destNode, sortedSrcNodes, score);
    }
*/
	int isSolutionState(int destNode, int[] sortedSrcNodes, float score) {
//		if (checkRedundantAnswer(destNode, sortedSrcNodes) == true) 
//			return false;
        return checkDuplicateAnswer(destNode, sortedSrcNodes, score);
    }

	public void search(List<String> query) {
//		FibonacciHeapNode<Integer> TNode = null;	
//		double maxCurScore = 1.0;
//		int maxValPos = 0;						// index of an element in curScores[] which has maximum value 
		int listIndex, i, j;
		TargetList l;
		ListEntry e;
		float scoreSum;
		
		if (prepareSearch(query) == false) return;
		min_k = 0;
		srcNodes = new int[numOfQueryKeywords];	// reuse within the below loop
		sortedNodes = new int[numOfQueryKeywords];	// for reuse in isSolutionState()
	    nkmapEntRels = new float[numOfQueryKeywords];
        sum_best = new float[numOfQueryKeywords];
//        sum_worst  = new float[numOfQueryKeywords];
        sum_best[numOfQueryKeywords-1] = 0;
       // sum_worst[numOfQueryKeywords-1] = 0;
//		P = new RelSource[numOfQueryKeywords][];
        in = new int[numOfQueryKeywords];
        indJ = new int[numOfQueryKeywords];

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
//			if (i == maxValPos)
//				maxValPos = Utils.findMaxValPos(curScores, numOfQueryKeywords);
//			maxCurScore = curScores[maxValPos];
			
			if (visitedNodes.contains(e.nodeID) == true)  // e.nodeId already found from inverted lists and visited
				continue;
/*			
			if (nodesTable.containsKey(e.nodeID) == true) { // e.nodeId already found from inverted lists
				continue;
//				n = nodesTable.get(e.nodeID);
//				if (n == null) continue;	// already removed from the candidate queue C
			}
			else {	// e.nodeId first found from the list
				n = new BlinkNodeData(numOfQueryKeywords);
				nodesTable.put(e.nodeID, n);
			}
*/			
			visitNode(srcNodes, e, query, i);
			visitedNodes.add(e.nodeID);
/*		
			double min_k = 0;
			if (score >= 0) { // node of e.nodeID has paths to all the keywords.
//				System.out.print(".");
//				System.out.print(numOfListAccess + "th entry read from list " + l.getIndex());
//				System.out.println(n);
//				System.out.println("Score=" + score + ", maxCurScore=" + maxCurScore);

				if (score > min_k) {	
					FibonacciHeapNode<Integer> newTNode = new FibonacciHeapNode<Integer>(e.nodeID);
					T.insert(newTNode, score);	
					
					int[] sortedNodes = sortSrcNodes(srcNodes);
					TopKNodeData nodeData = new TopKNodeData(srcNodes, sortedNodes, newTNode);
					nodesTable.put(e.nodeID, nodeData);
					
//					System.out.println("(node " + e.nodeID + ", " + score + ") -> T");

					if (T.size() == Params.K) {		// T becomes now full
						min_k = T.min().getKey();	// increase from 0 to a min value;
					}
					else if (T.size() == Params.K + 1) {	// T was already full, thus should remove min node	
//						System.out.print("T -> (node " + TNode.getData() + ", " + TNode.getKey() + ") -> ");
						T.removeMin();
						min_k = T.min().getKey();	// increased
					}
//					System.out.println(T.toString());
//					System.out.println("min-k=" + min_k);
				}
				else nodesTable.put(e.nodeID, null); 		// remove
			}
//			else {
//				nodesTable.put(e.nodeID, null); 		// remove
//			}
*/						
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
	
	public void sortSrcNodes(int[] nodes, int[] sortedNodes) {
        for (int i = 0; i < nodes.length; i++) sortedNodes[i] = nodes[i];
		Arrays.sort(sortedNodes);
	}
	
	public void printResult(List<String> q, PrintWriter pw) {

        List<Integer> nodes = new ArrayList<Integer>();

		pw.println(q);		
		if (ResultDestNodes == null) return;
		for (int i = 0; i < ResultDestNodes.length; i++) {
			int u = ResultDestNodes[i];
			int[] srcNodes = nodesTable.get(u).srcNodes;
			pw.println("srcNodes: " + Arrays.toString(srcNodes));
			SumResultRelev += ResultRelevs[i];
			
			if (printAnswerTree(u, srcNodes, pw) == true) {
				SumReducedResultRelev += ResultRelevs[i];
                nodes.add(u);
            }
			else
				numOfNonReducedAnswers++;			
		}
		numOfDuplicateAnswers = countNumberOfDuplicateAnswers(ResultDestNodes, nodesTable);
		numOfNonredundantAnswers = countNumberOfUniqueAnswers(nodes, nodesTable);
		pw.println("-----------------------------------------");
		pw.println("ResultDestNodes: " + Arrays.toString(ResultDestNodes));
		pw.println("ResultRelevs: " + Arrays.toString(ResultRelevs));
		pw.println("Num of top-k answers: " + ResultDestNodes.length);
		pw.println("Num of non-reduced answers in top-k answers: " + numOfNonReducedAnswers);
		pw.println("Num of duplicate answers in top-k answers: " + numOfDuplicateAnswers);
		pw.println("Num of non-redundant answers: " + numOfNonredundantAnswers);
		pw.println("Num of entries retrieved from the lists: " + numOfAccInUpdatePeriod);
		pw.println("Num of redundant candidate answers found: " + numOfNonReducedCandidates);
		pw.println("Num of reduced alternative answers found: " + numOfReducedAlternatives);
		pw.println("Num of duplicate candidate answers found: " + numOfDuplicateCandidates);
		pw.println("Num of unique alternative answers found: " + numOfUniqueAnswersFound);
		
//		return destNodes;
	}
	
	public int countNumberOfDuplicateAnswers(int[] nodes, Map<Integer, TopKNodeDataNewIOOpt2> nodesTable) {
		for (int u : nodes) {
			srcNodesSet.add(
                Arrays.hashCode(nodesTable.get(u).sortedSrcNodes)
            );
		}
		int r = nodes.length - srcNodesSet.size();
        srcNodesSet.clear();
        return r;
	}

	public int countNumberOfUniqueAnswers(List<Integer> nodes, Map<Integer, TopKNodeDataNewIOOpt2> nodesTable) {
		for (int u : nodes) {
			srcNodesSet.add(
                Arrays.hashCode(nodesTable.get(u).sortedSrcNodes)
            );
		}
		int r = srcNodesSet.size();
        srcNodesSet.clear();
        return r;
	}
	public void close() {
		nkmapRead.close();
	}
}
