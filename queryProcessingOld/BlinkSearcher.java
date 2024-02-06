package queryProcessing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Iterator;
import java.util.List;
import java.io.PrintWriter;

import my.jgrapht.algorithm.BellmanFordShortestPathMod;
import nkmap.bdb.RelSourceFirst;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import util.ListEntry;
import util.NodeScore;
import util.NodeScoreComparator;
import util.Params;
import util.TargetList;

public class BlinkSearcher extends Searcher {
	
 	HashMap<Integer, int[]> nodesTable;
	PriorityQueue<NodeScore> T;
	
//	int numOfNonReducedAnswers = 0;
//	int numOfDuplicateCandidates = 0;
//	int numOfNonReducedCandidates = 0;
//	int numOfReducedAlternatives = 0;
//	int	numOfDuplicateAnswers = 0, numOfUniqueAnswersFound = 0;
//    int numOfNonredundantAnswers = 0;
//    int numOfStatesChecked = 0;
//    double SumReducedResultRelev;
	double DCG, DCG2;
//	double SumOfScoresOfAnswerTreesExplored = 0.0;	
	int numOfNKMapLookups2 = 0;
    int numOfNKMapLookupsByKeyword = 0, maxNKMap = 0, minNKMap = 0;

	public BlinkSearcher() {
		super();
		nodesTable = new HashMap<Integer, int[]>(32);
		T = new PriorityQueue<NodeScore>(40, new NodeScoreComparator()); 			// worstScore의 내림차순으로 최대 k개 저장; root node == min-k node
	}

	public BlinkSearcher(DirectedGraph<Integer, DefaultEdge> g) {
		super(g);
		nodesTable = new HashMap<Integer, int[]>(32);
		T = new PriorityQueue<NodeScore>(40, new NodeScoreComparator()); 			// worstScore의 내림차순으로 최대 k개 저장; root node == min-k node
	}

	@Override
	protected boolean prepareSearch(List<String> query) {
		if (super.prepareSearch(query) == false)
			return false;
		
//		SumOfScoresOfAnswerTreesExplored = 0.0;
//		numOfNonReducedAnswers = 0;
//		numOfDuplicateCandidates = 0;
//		numOfNonReducedCandidates = 0;
//		numOfReducedAlternatives = 0;
//		numOfDuplicateAnswers = numOfUniqueAnswersFound = 0;		
//      numOfNonredundantAnswers = 0;
//      numOfStatesChecked = 0;
		
	    numOfNKMapLookups2 = 0;
        numOfNKMapLookupsByKeyword = maxNKMap = minNKMap = 0;
		DCG = 0.0; 	DCG2 = 0.0; 	
		return true;
	}

	@Override
	public void completeSearch() {
		nodesTable.clear();
		super.completeSearch();
	}

	private float visitNode(int[] n, ListEntry e, List<String> query, int curInd) {
			float[] nkmapEntRels = new float[query.size()];
			float score = (float)0;		
	//		boolean areAllfNodesTheSame = true;
			
	
			for (int i = 0; i < query.size(); i++) {	// look up the first NKMap
				if (i == curInd) {
					nkmapEntRels[curInd] = e.rel; 
					n[i] = e.sNodeID;
					score += e.rel;
					continue;
				}
				nk.setDestNode(e.nodeID);
				nk.setKeyword(query.get(i));
				RelSourceFirst rsf = nkmapRead.searchRelSourceFirst(nk);
				numOfNKMapLookups++;
				if (rsf == null) { 
	//				System.out.println("There is no path from node "+node+" to keyword "+query.get(i));
	//				n.clear();
					return (float)-1;
				}
	//			if (rs.getFstNode() != e.fNodeID) areAllfNodesTheSame = false;
				nkmapEntRels[i] = rsf.getRel(); 			
				n[i] = rsf.getSrcNode();
				score += rsf.getRel();
			}
	
	/*
			if (areAllfNodesTheSame == true) {	// have to look up the second NKMap
				int newSrcNode = -1, maxIndex = -1;
				float newRel = (float)0;
				float maxScore =(float)0, newScore = (float)0;
				for (int i = 0; i < query.size(); i++) {	
					// look up the second NKMap
					nk.setDestNode(e.nodeID);
					nk.setKeyword(query.get(i));
					RelSource new_rs = nkmapRead.searchRelSourceIn2ndNKMap(nk);
					if (new_rs == null) continue;				
					
					// check the score of new combination of src paths and find the best 
					newScore = score - nkmapEntRels[i] + new_rs.getRel();
					if (newScore > maxScore) {
						maxScore = newScore;
						maxIndex = i;
						newSrcNode = new_rs.getSrcNode();
						newRel = new_rs.getRel();
					}
				}
				if (maxIndex == -1) {	// no alternative reduced answer tree
					n.clear();
					return (float)-1;
				}
				n.setSrcNode(maxIndex, newSrcNode);	// replace srcNodeID at maxIndex
				newScore = score - nkmapEntRels[maxIndex] + newRel;	// recompute score
				System.out.println("++++++****** reduced answer found with score "+newScore
						+ " instead of score " + score + "++++++******");
				score = newScore;
			}
	*/
			numOfAnswerTreesExplored++;
	
	//		SumOfScoresOfAnswerTreesExplored += score;
	
			return score;
		}

	public void search(List<String> query) {
			float min_k = 0;
	//		FibonacciHeapNode<Integer> TNode = null;	
			float score = 0;
	//		double maxCurScore = 1.0;
	//		int maxValPos = 0;						// index of an element in curScores[] which has maximum value 
			int[] n;
	
			if (prepareSearch(query) == false) { 
				return;
			}
			
			while(true) {
				
				int listIndex = getNextListIndex();
				TargetList l = targetLists.get(listIndex);
				ListEntry e = l.getNextEntry();
/*

				System.out.print("list#" + listIndex + ": ");
				System.out.println(e);
*/	
				logger.debug("list#: {}, ListEntry: {}", listIndex, e);

				if (e == null) { 			// all entries in this list have been read
					logger.debug("exit since one of the target lists has been read...");
					getResult();
					return;
				}
				numOfKNListsRead++;
	
				int i = l.getIndex();
				curRel[i] = e.rel;
	//			if (i == maxValPos)
	//				maxValPos = Utils.findMaxValPos(curScores, numOfQueryKeywords);
	//			maxCurScore = curScores[maxValPos];
				float scoreSum = 0;
				for (int j = 0; j < numOfQueryKeywords; j++)
					scoreSum += curRel[j];
				
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
	//						T.removeMin();
					        T.poll();
							//TNode = T.removeMin();
	//						System.out.print("T -> (node " + TNode.getData() + ", " + TNode.getKey() + ") -> ");
	//						min_k = T.min().getKey();	// increased
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

	private void getResult() {
		// return top-k nodes in T as the query result
//		FibonacciHeapNode<Integer> TNode;
		NodeScore ns;
//		int[] nodes = new int[T.size()];
//		double[] rels = new double[T.size()];
		
		ResultDestNodes = new int[T.size()]; // ArrayList<Integer>(T.size());
		ResultRelevs = new double[T.size()]; // ArrayList<Double>(T.size());
		
		int len = T.size();
		double sumRelev = 0;
		for (int i = len - 1; i >= 0; i--) {
			ns = T.poll();
			ResultDestNodes[i] = ns.nodeId;	// nodeID
			ResultRelevs[i] = ns.score;	// rel
//			nodes[i] = TNode.getData();	// nodeID
//			rels[i] = TNode.getKey();	// rel	
			
			sumRelev += ResultRelevs[i];
		}		
		avgResultRelev = sumRelev / ResultDestNodes.length;
		avgResultDiss = computeAverageDissimilarity();
		
/*		for (int i = 0; i < len; i++) {
			ResultDestNodes.add(nodes[i]);
			ResultRelevs.add(rels[i]);
		}
*/	}
	
	private double computeAverageDissimilarity() {
		double sumDiss = 0;
		int n = ResultDestNodes.length;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < i; j++) {
				sumDiss += computeDissimilarity(
					nodesTable.get(ResultDestNodes[i]), 
					nodesTable.get(ResultDestNodes[j]));				
			}
		}
		return sumDiss/(n*(n-1)/2);
	}
	
	public void printResult(PrintWriter pw, List<String> q) {
	//		List<Integer> destNodes = super.getResult();
	//		List<Integer> srcNodes = null;
	//        List<Integer> reducedNodes = new ArrayList<Integer>(); // to store reduced answers
	
		pw.println(q);
	
		if (ResultDestNodes == null) return;
	
		//double DCG = 0.0d, IDCG = 0.0d;
		double logval = 0.0d;
/*
		double[] ResultRelevs_sorted = Arrays.copyOf(ResultRelevs, ResultRelevs.length);
		Arrays.sort(ResultRelevs_sorted);	
		for (int i = 0; i < ResultRelevs.length; i++) 
			pw.print(ResultRelevs[i] + ", ");
		pw.println();	
		for (int i = 0; i < ResultRelevs.length; i++) 
			pw.print(ResultRelevs_sorted[i] + ", ");
		pw.println();	
*/
		for (int i = 0; i < ResultDestNodes.length; i++) {
			int u = ResultDestNodes[i];
			int[] srcNodes = nodesTable.get(u);
//			srcNodes = n.getSrcNodes();
			pw.println("srcNodes: " + Arrays.toString(srcNodes));
			
			printAnswerTree(u, srcNodes, pw);
/*	
			if (printAnswerTree(u, srcNodes, pw) == true) {
		// reduced answer!
				SumReducedResultRelev += ResultRelevs[i];
				reducedNodes.add(u);
			}
			else
				numOfNonReducedAnswers++;			
*/	
			// to find NDCG@K
			logval = Math.log(i+1.0+1.0)/Math.log(2.0);
			DCG += (ResultRelevs[i]/logval);
				DCG2 += ((Math.pow(2, ResultRelevs[i]/ResultRelevs[0])-1.0)/logval);
			//	IDCG += (ResultRelevs_sorted[ResultDestNodes.length-i-1]/logval);
		}
		DCG = DCG/ResultRelevs[0];
		//nDCG = DCG / IDCG;
		
	//	numOfDuplicateAnswers = countNumberOfDuplicateAnswers(ResultDestNodes, nodesTable);
	//	numOfNonredundantAnswers = countNumberOfUniqueAnswers(reducedNodes, nodesTable);
		pw.println("-----------------------------------------");
		pw.println("Num of top-k answers: " + ResultDestNodes.length);
		pw.println("ResultDestNodes: " + Arrays.toString(ResultDestNodes));
		pw.println("ResultRelevs: " + Arrays.toString(ResultRelevs));			
		pw.println("AvgResultRelev: " + avgResultRelev);
		pw.println("AvgResultDissimilarity: " + avgResultDiss);

	//	pw.println("Num of non-reduced answers: " + numOfNonReducedAnswers);
	//	pw.println("Num of duplicate answers: " + numOfDuplicateAnswers);
	//	pw.println("Num of non-redundant answers: " + numOfNonredundantAnswers);
		pw.println("Num of entries retrieved from the lists: " + numOfKNListsRead);
		pw.println("Num of entries retrieved from NKMaps: " + numOfNKMapLookups);
		pw.println("Num of answer trees explored: " + numOfAnswerTreesExplored);
	}

/*	
	public boolean printAnswerTree2(int u,  int[] srcNodes, PrintWriter pw) {
//		Iterator<Integer> it = null;
		List<DefaultEdge> el = null;		
		Iterator<DefaultEdge> elit1 = null; //, elit2 = null;
//		String uName = null;
		boolean areAllFstNodesTheSame = true, destNodeContainsAKeyword = false;
		int fstNode = -1, FstFstNode = -1;
		connect();

//		uName = getNodeName(u);
		int i = 0;
//		it = srcNodes.iterator();
//		while (it.hasNext()) {
//			int v = it.next();	
		for ( ; i < srcNodes.length; i++) {
			int v = srcNodes[i];
//			pw.print("  path: " + i); 
			if (v == u) {
//				pw.print("["+ u + " -> " + v +"], ");
//				pw.println("[" + uName + " -> " + uName + "]");
				destNodeContainsAKeyword = true;
				if (i == 0) FstFstNode = u;
				continue;
			}
			
			el = BellmanFordShortestPathMod.findPathBetween(graph, u, v);

//			pw.print("["+ u);
			elit1 = el.iterator();
			
			// check first nodes to find if it is a non-reduced answer tree
			fstNode = (Integer) elit1.next().getTarget();
//			pw.print(" -> " + fstNode);
			if (i == 0) FstFstNode = fstNode;
			else if (fstNode != FstFstNode) areAllFstNodesTheSame = false;
			
//			while (elit1.hasNext()) {
//				pw.print(" -> " + elit1.next().getTarget());				
//			}
//			pw.print("], ");
			
//			pw.print("["+ uName);
//			elit2 = el.iterator();
//			while (elit2.hasNext()) {
//				pw.print(" -> " + getNodeName((Integer)(elit2.next().getTarget())));				
//			}
//			pw.println("] ");
			pw.println();
		}
		disconnect();

		if (areAllFstNodesTheSame && !destNodeContainsAKeyword) {
			// at least one srcNode is different from destNode
			pw.println("This is a non-reduced answer tree with common first node " + FstFstNode);
			pw.println();
			return false;
		}
		return true;			
	}
*/
/*
	Set<String> srcNodesSet = new HashSet<String>();	
	public int countNumberOfUniqueAnswers(List<Integer> nodes, Map<Integer, int[]> nodesTable) {
		for (int u : nodes) {
			StringBuffer sb = new StringBuffer();
			int[] srcNodes = (int[])nodesTable.get(u);
			int[] sortedNodes = Arrays.copyOf(srcNodes, srcNodes.length);
			Arrays.sort(sortedNodes);
			for (int i = 0; i < sortedNodes.length; i++)
				sb.append(sortedNodes[i]);
//			System.out.println(sb);
			srcNodesSet.add(sb.toString());		
		}
        int r = srcNodesSet.size();
        srcNodesSet.clear();
        return r;
    }

	public int countNumberOfDuplicateAnswers(int[] nodes, Map<Integer, int[]> nodesTable) {
		for (int u : nodes) {
			StringBuffer sb = new StringBuffer();
			int[] srcNodes = (int[])nodesTable.get(u);
			int[] sortedNodes = Arrays.copyOf(srcNodes, srcNodes.length);
			Arrays.sort(sortedNodes);
			for (int i = 0; i < sortedNodes.length; i++)
				sb.append(sortedNodes[i]);
//			System.out.println(sb);
			srcNodesSet.add(sb.toString());		
		}
        int r = nodes.length - srcNodesSet.size();
        srcNodesSet.clear();
		return r;
	}	
*/
	public void printStat(PrintWriter pw, double elapseTime) {
		pw.print(this.getClass().getName().substring(16, 24) + ", \t");
		pw.print(Params.K + ", ");

		if (ResultDestNodes == null) {
			pw.print("(no result) "); 
		}
		else {
			pw.printf("%.5f, %.5f, ", avgResultRelev, avgResultRelev/ResultRelevs[0]); pw.printf("%.5f, ", avgResultDiss);
		}

//    	pw.printf("%.2f, ", DCG);
 //   	pw.printf("%.2f, ", DCG2);
/*		double min = 10000.0, max = 0.0, sum = 0.0;
		for (double r : ResultRelevs) {
			if (r < min) min = r;
			if (r > max) max = r;
			sum += r;
		}
		pw.printf("(%.2f, %.2f, %.2f): ",  min, max, sum/ResultRelevs.length);
*/
		pw.print(numOfKNListsRead + ", ");
		pw.print(numOfNKMapLookups + ", ");
		pw.print(numOfAnswerTreesExplored + ", ");
		pw.print(elapseTime + ", ");
//        pw.printf("(%.2f, %d, %d)",
//		    numOfNKMapLookups2 / (float)numOfNKMapLookupsByKeyword, maxNKMap, minNKMap);
	        pw.println();
	}
	
	public void printStatHeader(PrintWriter pw) {
		pw.print("Top-k, ");
		pw.printf("(AvgResultRelev, AvgResultRelev/ResultRelevs[0], ");
	       	pw.printf("AvgResultDiss) ");
		pw.print("numOfKNListsRead, ");
		pw.print("numOfNKMapLookups, ");
		pw.print("numOfAnswerTreesExplored, ");
		pw.print("elapseTime, ");
       	 	pw.println();
	}

}
