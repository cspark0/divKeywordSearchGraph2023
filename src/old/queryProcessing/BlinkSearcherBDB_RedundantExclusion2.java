package queryProcessing.old;

//import temp.TermData;
import util.*;
import util.NodeScore;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import nkmap.bdb.NKMapRead;
import nkmap.bdb.NKMapReadForJmdb;
import nkmap.bdb.KeywordNode;
import nkmap.bdb.RelSource;
import nkmap.bdb.RelSourceFirst;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.util.FibonacciHeapNode;

public class BlinkSearcherBDB_RedundantExclusion2 extends Searcher2 {

	HashMap<Integer, int[]> nodesTable;
//	FibonacciHeap<Integer> T;				// global top-k queue T 
	NKMapRead nkmapRead;	// = new NKMapRead(Params.ExpDB + "/NKMapForBlink");
	KeywordNode nk = new KeywordNode();
	
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

	public BlinkSearcherBDB_RedundantExclusion2() {
		super();
		initDictionary();	
		nodesTable = new HashMap<Integer, int[]>(32);
	}

	public BlinkSearcherBDB_RedundantExclusion2(DirectedGraph<Integer, DefaultEdge> g) {
		super(g);
		initDictionary();	
		nodesTable = new HashMap<Integer, int[]>(32);
        nkmapRead = (Params.ExpDB.equals("res/jmdb")) ? new NKMapReadForJmdb() : new NKMapRead();
	}
	
	public void resetNKMapRead() {
        nkmapRead.close();
        nkmapRead.reset();
	}

	@Override
	public void completeSearch() {
		nodesTable.clear();
		super.completeSearch();
	}
		
	public float visitNode(int[] n, ListEntry e, List<String> query, int curInd) {
		float[] nkmapEntRels = new float[query.size()];
		float score = (float)0;		
		boolean areAllfNodesTheSame = true;
		
		for (int i = 0; i < query.size(); i++) {	// look up the first NKMap
			if (i == curInd) {
//				System.out.println(curInd);
	//			System.out.println(e);
	//			System.out.println(nkmapEntRels);
				
				n[i] = e.sNodeID;	// n.addSrcNode(e.sNodeID);
				score += (nkmapEntRels[i] = e.rel);
				continue;
			}
			nk.setDestNode(e.nodeID);
			nk.setKeyword(query.get(i));
			RelSourceFirst rsf = nkmapRead.searchRelSourceFirst(nk); numOfNKMapLookups++;
			if (rsf == null) { 
//				System.out.println("There is no path from node "+node+" to keyword "+query.get(i));
//				n.clear();
				return (float)-1;
			}
			if (rsf.getFstNode() != e.fNodeID) areAllfNodesTheSame = false;
			n[i] = rsf.getSrcNode();	// n.addSrcNode(rs.getSrcNode());
			score += (nkmapEntRels[i] = rsf.getRel());
		}
		if (areAllfNodesTheSame == true) {	
//			System.out.println("++++++ non-reduced answer found!! ++++++");
			// just ignore it!!!
			numOfNonReducedCandidates++;
			return (float)-1;
		}
		return score;
	}
	
	public void search(List<String> query) {
		float min_k = 0;
//		FibonacciHeapNode<Integer> TNode = null;	
		float score = 0;
//		double maxCurScore = 1.0;
//		int maxValPos = 0;						// index of an element in curScores[] which has maximum value 
		int[] n;

		if (prepareSearch(query) == false) return;
//		nkmapRead = new NKMapRead("E:/res/jmdb/NKMapForBlink");
		
		while(true) {
			
			int listIndex = getNextListIndex();
			TargetList l = targetLists.get(listIndex);
			ListEntry e = l.getNextEntry();
			if (e == null) { 			// all entries in this list have been read
//					System.out.println("exit since one of the target lists has been read...");
					getResult();
					return;
			}
			numOfAccInUpdatePeriod++;

			int i = l.getIndex();
			curScores[i] = e.rel;
//			if (i == maxValPos)
//				maxValPos = Utils.findMaxValPos(curScores, numOfQueryKeywords);
//			maxCurScore = curScores[maxValPos];
			double scoreSum = 0.0;
			for (int j = 0; j < numOfQueryKeywords; j++)
				scoreSum += curScores[j];
			
			if (nodesTable.containsKey(e.nodeID) == false) { // first found from inverted lists
				n = new int[numOfQueryKeywords];;
				nodesTable.put(e.nodeID, n);
			}
			else {
				continue;
//				n = nodesTable.get(e.nodeID);
//				if (n == null) continue;	// already removed from the candidate queue C
			}
			
			score = visitNode(n, e, query, i);

			if (score >= 0) { // node of e.nodeID has paths to all the keywords.
//				System.out.print(".");
//				System.out.print(numOfListAccess + "th entry read from list " + l.getIndex());
//				System.out.println(n);
//				System.out.println("Score=" + score + ", maxCurScore=" + maxCurScore);

				if (score > min_k) {	
                    T.add(new NodeScore(e.nodeID, score));
//					System.out.println("(node " + e.nodeID + ", " + score + ") -> T");

					if (T.size() == Params.K) {		// T becomes now full
				        min_k = T.peek().score;	// increase from 0 to a min value;
					}
					else if (T.size() == Params.K + 1) {	// T was already full, thus should remove min node	
						T.poll();
						//TNode = T.removeMin();
//						System.out.print("T -> (node " + TNode.getData() + ", " + TNode.getKey() + ") -> ");
				        min_k = T.peek().score;	// increase from 0 to a min value;
					}
//					System.out.println(T.toString());
//					System.out.println("min-k=" + min_k);
				}
				else nodesTable.put(e.nodeID, null); 		// remove
			}
			else 
				nodesTable.put(e.nodeID, null); 		// remove n
			
						
			if (scoreSum <= min_k) {
				// return top-k nodes in T as the query result
//				System.out.println("exit from step1...");
				getResult(); return;			
			}
		}	// end of while
	}
	
	public void printResult(List<String> q, PrintWriter pw) {
//		List<Integer> destNodes = super.getResult();
//		List<Integer> srcNodes = null;

        List<Integer> nodes = new ArrayList<Integer>();
		pw.println(q);
		
		if (ResultDestNodes == null) return;
		for (int i = 0; i < ResultDestNodes.length; i++) {
			int u = ResultDestNodes[i];
			int[] srcNodes = nodesTable.get(u);
//			srcNodes = n.getSrcNodes();
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
		pw.println("Num of redundant answers: " + numOfNonReducedAnswers);
		pw.println("Num of duplicate answers: " + numOfDuplicateAnswers);
		pw.println("Num of entries retrieved from the lists: " + numOfAccInUpdatePeriod);
		pw.println("Num of entries retrieved from NKMaps: " + numOfNKMapLookups);
	}
	

	public void close() {
		nkmapRead.close();
	}

}
