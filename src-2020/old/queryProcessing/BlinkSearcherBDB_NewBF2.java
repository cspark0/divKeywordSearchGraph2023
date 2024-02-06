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
import java.util.Map.Entry;
import java.util.PriorityQueue;

import nkmap.bdb.RelSource;
import nkmap.bdb.NKMapRead;
import nkmap.bdb.NKMapReadForJmdb;
import nkmap.bdb.KeywordNode;
import nkmap.bdb.RelSourceFirst;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import org.jgrapht.util.*;
import util.State;

public class BlinkSearcherBDB_NewBF2 extends Searcher3 { 
	HashMap<Integer, TopKNodeData2> nodesTable = new HashMap<Integer, TopKNodeData2>(32);
//	FibonacciHeap<Integer> T;				// global top-k queue T 
	NKMapRead nkmapRead;
	
	Set<Integer> visitedNodes = new HashSet<Integer>();
	List<State> C = new ArrayList<State>();		// closed set
	KeywordNode nk = new KeywordNode();
	float min_k;
	int[] srcNodes_temp;
    int globalDupId;    // store return value from findUniqueAnswer()
	float[] nkmapEntRels; 
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

	public BlinkSearcherBDB_NewBF2() {
		super();
		initDictionary();	
	}

	public BlinkSearcherBDB_NewBF2(DirectedGraph<Integer, DefaultEdge> g) {
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
		nodesTable.clear();
		visitedNodes.clear();
		C.clear();
        Q.clear();
        spool.clear();
		min_k = 0;
        accFindUniqTime = 0;
		super.completeSearch();
	}
		
	public void visitNode(int[] srcNodes, ListEntry e, List<String> query, int curInd) {
		float score = (float)0;		
		boolean areAllfNodesTheSame = true;
		int i;
		
		nk.setDestNode(e.nodeID);
		for (i = 0; i < numOfQueryKeywords; i++) {	// look up the first NKMap
			if (i == curInd) {
//				System.out.println(curInd);
	//			System.out.println(e);
	//			System.out.println(nkmapEntRels);
				
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
			srcNodes[i] = rsf.getSrcNode();	// n.addSrcNode(rsf.getSrcNode());
			score += (nkmapEntRels[i] = rsf.getRel());
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
				// newScore = score - nkmapEntRels[i] + new_rs.getRel();
				scoreDiff = nkmapEntRels[i] - new_rs.getRel();
				if (scoreDiff < minScoreDiff) {
					minScoreDiff = scoreDiff;
					minIndex = i;
					newSrcNode = new_rs.getSrcNode();
//					newRel = new_rs.getRel();
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
		float curScore = score, newScore, dupScore;
	    TopKNodeData2 dupNodeData = null;

		dupId = checkDuplicateAnswer(curId, srcNodes, curScore);
        if (dupId == 0) {   // no duplication found!
            // insert curId node
			insertIntoTopKQueue(curId, srcNodes, curScore);
            return;
        }
        if (dupId == curId) {	// curId is a duplicate answer
//            System.out.println("A duplicate answer found, rooted at " + curId);
            numOfDuplicateCandidates++;

            start = cpuutil.getCpuTime();
			newScore = findUniqueAnswer(curId, curScore, query, srcNodes);	
            end = cpuutil.getCpuTime();
            accFindUniqTime += (end - start);

			if (newScore == (float)-1)		// no unique answer for curId
				return;
			// else {		// unique and superior answer found! srcNodes were changed.
//			System.out.println("A unique answer found for " + curId);
			numOfUniqueAnswersFound++;
			curScore = newScore;
            if (globalDupId == 0) { // no duplication found!!
                // insert curId node
		        insertIntoTopKQueue(curId, srcNodes, curScore);
                return;
            }
            dupId = globalDupId;        // returned from findUniqueAnswer()
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
		dupScore = (float)dupNodeData.ns.score;
	    T.remove(dupNodeData.ns);

        // insert curId node
		insertIntoTopKQueue(curId, srcNodes, curScore);

        if (dupScore <= min_k) return;

		while (true) {
            start = cpuutil.getCpuTime();
			newScore = findUniqueAnswer(dupId, dupScore, query, srcNodes);
            end = cpuutil.getCpuTime();
           accFindUniqTime += (end - start);

			if (newScore == (float)-1)	{	// no unique answer for dupId
				break;
			}
			// unique answer found! srcNodes were changed.
//			System.out.println("A unique answer found for " + dupId);
			numOfUniqueAnswersFound++;
            if (globalDupId == 0) { // no duplication found!!
                // insert curId node
			    insertIntoTopKQueue(dupId, srcNodes, newScore);
                break;
            }

            // another duplication found!!
//            System.out.println("A duplicate answer found, rooted at " + globalDupId);
            numOfDuplicateCandidates++;
            // remove next Dup Node first
			dupNodeData = nodesTable.remove(globalDupId);
		    dupScore = (float)dupNodeData.ns.score;
			T.remove(dupNodeData.ns);

            // insert dupId node
			insertIntoTopKQueue(dupId, srcNodes, newScore);

            if (dupNodeData.ns.score <= min_k) break;

            dupId = globalDupId;        // returned from findUniqueAnswer()
		}
	}
	
	public int insertIntoTopKQueue(int destNode, int[] srcNodes, float score) {
		// return 0 or destNodeID removed from Q
		if (score > min_k) {	
//			FibonacciHeapNode<Integer> newTNode = new FibonacciHeapNode<Integer>(destNode);
			NodeScore ns = new NodeScore(destNode, score);
			T.add(ns);	
			
			int[] sourceNodes = Arrays.copyOf(srcNodes, srcNodes.length);
			int[] sortedNodes = sortSrcNodes(srcNodes);
			TopKNodeData2 nodeData = new TopKNodeData2(sourceNodes, sortedNodes, ns);
			nodesTable.put(destNode, nodeData);				
//			System.out.println("(node " + e.nodeID + ", " + score + ") -> T");

			if (T.size() == Params.K) {		// T becomes now full
				min_k = (float)T.peek().score;	// increase from 0 to a min value;
				return 0;
			}
			else if (T.size() == Params.K + 1) {	// T was already full, thus should remove min node	
//				System.out.print("T -> (node " + TNode.getData() + ", " + TNode.getKey() + ") -> ");
				int removedNodeId = T.poll().nodeId;
				nodesTable.remove(removedNodeId);
				min_k = (float)T.peek().score;	// increased
				return removedNodeId;
			}
//			System.out.println(T.toString());
//			System.out.println("min-k=" + min_k);
		}
		return 0;
	}	
	
	int checkDuplicateAnswer(int destNode, int[] srcNodes, float rel) {
		// return root id of a duplicate and inferior answer; 0 if no duplicate answer
		int[] theSortedNodes = sortSrcNodes(srcNodes);	// sort srcNodesIDs
		
		Set<Entry<Integer, TopKNodeData2>> eSet = nodesTable.entrySet();
		Iterator<Entry<Integer, TopKNodeData2>> iter = eSet.iterator();
		while (iter.hasNext()) {		// iterate all top-k nodes in Qt
			Entry<Integer, TopKNodeData2> e = iter.next();
			if (Arrays.equals(e.getValue().sortedSrcNodes, theSortedNodes) == true) {
				// a top-k node e found which has the same srcNodes to this one
				if (rel <= e.getValue().ns.score)	// relevance is no greater than e
					return destNode;	// This is a duplicate answer!!!
				else 	// e is a duplicate answer. 
					return e.getKey();	// destNodeID of the other duplicate answer
			}
		}
		return 0;		// There is no duplicate answer
	}
	
	public float findUniqueAnswer(int destNode, float prevScore, List<String> query, int[] srcNodes) {
		int i, j, k, dupId;
        int bestSol_srcNode = 0;
		float bestScore = min_k, score_s; 
        State bestSol_sp = null, e, s;

		nk.setDestNode(destNode);
		for (i = 0; i < numOfQueryKeywords; i++) {
			nk.setKeyword(query.get(i));
			P[i] = nkmapRead.searchRelSourcesInNKMapExt(nk); 
            numOfNKMapLookups += P[i].length;
            numOfNKMapLookups2 += P[i].length;
            
            numOfNKMapLookupsByKeyword++;
            if (maxNKMap < P[i].length) maxNKMap = P[i].length;
            if (minNKMap > P[i].length) minNKMap = P[i].length;
		}

        Q.push(s0);

        // generate the states of keyword level i > 0;
		while (!Q.empty()) {
			e = Q.pop();
			i = e.i+1;		// level, i.e. keyword index

			if (i < numOfQueryKeywords-1) {	// non-leaf state
			    for (j = 0; j < P[i].length; j++) {	
				    // generate each child state of e
				    score_s = e.score + P[i][j].rel; 	
                    try {
                        s = spool.getState(i, j, P[i][j].srcNode, e, score_s, 0, 0);
					    Q.push(s);
                    } catch (Exception ex) { ex.printStackTrace(); }		
				}
            }
            else { // (i == numOfQueryKeywords-1) { // leaf state
			    for (j = 0; j < P[i].length; j++) {	
				    score_s = e.score + P[i][j].rel; 	
                    if (score_s > bestScore && score_s <= prevScore) {
                        srcNodes_temp[i] = P[i][j].srcNode; // s.i == i
                        for (s=e, k=i-1; k >= 0; k--, s=s.sp) 
                            srcNodes_temp[k] = s.srcNode;
                        numOfStatesChecked++;
                    	dupId = isSolutionState(destNode, srcNodes_temp, score_s);
                        if (dupId != destNode) {
                            bestScore = score_s; 
                            bestSol_srcNode = P[i][j].srcNode;
                            bestSol_sp = e; 
                            globalDupId = dupId;
                            break;
                        }
                    }
                }
            }
		}

        Q.clear();
        spool.clear();

        if (bestSol_sp != null) {
            i = numOfQueryKeywords-1;
		    srcNodes[i] = bestSol_srcNode;	// s.i == i
			for (s=bestSol_sp, i--; i >= 0; i--, s=s.sp) 
				srcNodes[i] = s.srcNode;	
		    return bestScore;		
        }
		else { // no duplicate-free answer
		    return -1; //(float)-1;		
        }
    
	}

	int isSolutionState(int destNode, int srcNodes[], float score) {
		return checkDuplicateAnswer(destNode, srcNodes, score);
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
		int[] srcNodes = new int[numOfQueryKeywords];	// reuse within the below loop
		srcNodes_temp = new int[numOfQueryKeywords];	// for reuse in isSolutionState()
	    nkmapEntRels = new float[numOfQueryKeywords];
		P = new RelSource[numOfQueryKeywords][];
		
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
	
	public int[] sortSrcNodes(int[] nodes) {
		int[] sortedNodes = // new int[bn.getSrcNodes().length];
				Arrays.copyOf(nodes, nodes.length);
		Arrays.sort(sortedNodes);
		return sortedNodes;
	}
	
	public void printResult(List<String> q, PrintWriter pw) {
//		List<Integer> destNodes = super.getResult();
//		List<Integer> srcNodes = null;
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
		numOfDuplicateAnswers = countNumberOfDuplicateAnswers2(ResultDestNodes, nodesTable);
		numOfNonredundantAnswers = countNumberOfUniqueAnswers2(nodes, nodesTable);
		pw.println("-----------------------------------------");
		pw.println("ResultDestNodes: " + Arrays.toString(ResultDestNodes));
		pw.println("ResultRelevs: " + Arrays.toString(ResultRelevs));
		pw.println("Num of top-k answers: " + ResultDestNodes.length);
		pw.println("Num of redundant answers in top-k answers: " + numOfNonReducedAnswers);
		pw.println("Num of duplicate answers in top-k answers: " + numOfDuplicateAnswers);
		pw.println("Num of entries retrieved from the lists: " + numOfAccInUpdatePeriod);
		pw.println("Num of redundant candidate answers found: " + numOfNonReducedCandidates);
		pw.println("Num of reduced alternative answers found: " + numOfReducedAlternatives);
		pw.println("Num of duplicate candidate answers found: " + numOfDuplicateCandidates);
		pw.println("Num of unique alternative answers found: " + numOfUniqueAnswersFound);
		
//		return destNodes;
	}
	
	public int countNumberOfDuplicateAnswers() {
//		Set<int[]> srcNodesSet = new HashSet<int[]>();	
		//Set<String> srcNodesSet = new HashSet<String>();	
		Set<Integer> srcNodesSet = new HashSet<Integer>();	
		for (int u : ResultDestNodes) {
			srcNodesSet.add(
                Arrays.hashCode(nodesTable.get(u).sortedSrcNodes)
            );
/*
			StringBuffer sb = new StringBuffer();
			int[] srcNodes = nodesTable.get(u).srcNodes;
            for (int i = 0; i < srcNodes.length; i++) sortedNodes[i] = srcNodes[i];
			Arrays.sort(sortedNodes);
			for (int i = 0; i < sortedNodes.length; i++)
				sb.append(sortedNodes[i]);
//			System.out.println(sb);
			srcNodesSet.add(sb.toString());		
*/
		}
		return ResultDestNodes.length - srcNodesSet.size();
	}
	
	public void close() {
		nkmapRead.close();
	}

	public int countNumberOfDuplicateAnswers2(int[] nodes, Map<Integer, TopKNodeData2> nodesTable) {
		for (int u : nodes) {
			srcNodesSet.add(
                Arrays.hashCode(nodesTable.get(u).sortedSrcNodes)
            );
		}
		int r = nodes.length - srcNodesSet.size();
        srcNodesSet.clear();
        return r;
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

}
