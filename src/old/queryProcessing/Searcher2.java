package queryProcessing.old;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import my.jgrapht.algorithm.BellmanFordShortestPathMod;

import org.jgrapht.DirectedGraph;
//import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

//import temp.TermData;
//import util.InvalidKeywordException;

import util.NodeScore;
import util.NodeScoreComparator;
import util.Params;
import util.TargetList;
import util.InvertedList;
import util.InvertedList;
import java.util.PriorityQueue;
//import util.TopKNodeData;

public abstract class Searcher2 {
	
	protected Hashtable<String, InvertedList> dictionary; 	// term dictionary in the inverted index
	protected List<TargetList> targetLists; 	// inverted lists for the given query keywords 
	protected int RRListSelector;					// index of the target lists	
	protected int numOfQueryKeywords;
	protected float[] curScores; 		
	PriorityQueue<NodeScore> T;
	//	FibonacciHeap<Integer> T;				// global top-k queue T 
//	FibonacciHeap<TopKNode> T;				// global top-k queue T 
//	public int numOfListAccess;
//	int numOfUpdates = 0; 
	int numOfNKMapLookups = 0;
	int numOfAccInUpdatePeriod = 0;
	int numOfAnswerTreeExplored = 0;
	int numOfNonReducedAnswers = 0;
	int numOfDuplicateCandidates = 0;
	int numOfNonReducedCandidates = 0;
	int numOfReducedAlternatives = 0;
	int	numOfDuplicateAnswers = 0, numOfUniqueAnswersFound = 0;
    int numOfNonredundantAnswers = 0;
    int numOfStatesChecked = 0;
//	List<Integer> ResultDestNodes = null; 
//	List<Double> ResultRelevs = null; 
	int[] ResultDestNodes;
	double[] ResultRelevs;
	double SumResultRelev, SumReducedResultRelev;
	double DCG, DCG2;
	double SumOfScoresOfAnswerTreesExplored = 0.0;
	
	int numOfNKMapLookups2 = 0;
    int numOfNKMapLookupsByKeyword = 0, maxNKMap = 0, minNKMap = 0;

	protected DirectedGraph<Integer, DefaultEdge> graph =
        new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);

	public Searcher2() {
//		dictionary = new Hashtable<String, TermData>(32);
		dictionary = new Hashtable<String, InvertedList>(32);
		targetLists = new ArrayList<TargetList>();	
		T = new PriorityQueue<NodeScore>(40, new NodeScoreComparator()); 			// worstScore의 내림차순으로 최대 k개 저장; root node == min-k node
		graph = loadGraph();
	}

	public Searcher2(DirectedGraph<Integer, DefaultEdge> g) {
//		dictionary = new Hashtable<String, TermData>(32);
		dictionary = new Hashtable<String, InvertedList>(32);
		targetLists = new ArrayList<TargetList>();	
		T = new PriorityQueue<NodeScore>(40, new NodeScoreComparator()); 			// worstScore의 내림차순으로 최대 k개 저장; root node == min-k node
		graph = g;
	}

//	private void loadGraph() {
	public static DirectedGraph<Integer, DefaultEdge> loadGraph() {
		DirectedGraph<Integer, DefaultEdge> graph =
		        new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);

		File vFile = new File(Params.ExpDB + "/graph/vertexData.bin");
		File eFile = new File(Params.ExpDB + "/graph/edgeData.bin");
		FileInputStream vfis = null, efis = null;
		DataInputStream vdis = null, edis = null;
		int i=0, j=0;
		
		try {
			vfis = new FileInputStream(vFile);
			vdis = new DataInputStream(vfis);			
			int v;			
			while(true) {
				v = vdis.readInt();			
				graph.addVertex(v);	i++;
			}
		} catch (EOFException e) {
			System.out.println("All the vertices have been loaded");
			try {
				vdis.close();
				vfis.close();
			} catch (IOException e1) {
				e1.printStackTrace();			
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		try {
			efis = new FileInputStream(eFile);
			edis = new DataInputStream(efis);			
			int u, v;			
			while(true) {
				u = edis.readInt();			
				v = edis.readInt();			
				graph.addEdge(u, v);	j++;
			}
		} catch (EOFException e) {
			System.out.println("All the edges have been loaded");
			try {
				edis.close();
				efis.close();
			} catch (IOException e1) {
				e1.printStackTrace();			
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		System.out.println(i + " " + j);
		return graph;
	}
		
	protected boolean prepareSearch(List<String> query) {
/*		try {
			findTargetLists(query);
		} catch (InvalidKeywordException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
*/		if (findTargetLists(query) == false) return false;
		RRListSelector = -1;
		numOfQueryKeywords = query.size();
		curScores = new float[numOfQueryKeywords];
		for (int i = 0; i < numOfQueryKeywords; i++) curScores[i] = (float)1000.0;
		return true;
	}

	protected boolean findTargetLists(List<String> query)  {
		for (int i = 0; i < query.size(); i++) {
//			TermData t = dictionary.get(query.get(i));	// i-th keyword ki
			InvertedList t = dictionary.get(query.get(i));	// i-th keyword ki
			if (t == null)
//				throw new InvalidKeywordException("Keyword " + query.get(i) + "is not in the dictionary");
				return false;
			TargetList l = new TargetList(t, i);
			l.openToScan(); 
			targetLists.add(l);				// ki에 대한 list li 
		}
		return true;
	}
	
	protected int getNextListIndex() {
		RRListSelector = ++RRListSelector % targetLists.size();
		return RRListSelector;
	}

	public abstract void search(List<String> query);
	public abstract void printResult(List<String> q, PrintWriter pw);
	public abstract void close();

	public void completeSearch() {
        for (int i = 0; i < targetLists.size(); i++) 
            targetLists.get(i).close(); 
		targetLists.clear();
		T.clear();
//		numOfUpdates = 0; 
		numOfNKMapLookups = 0;
		numOfAnswerTreeExplored = 0;
		SumOfScoresOfAnswerTreesExplored = 0.0;
		numOfAccInUpdatePeriod = 0;
		numOfNonReducedAnswers = 0;
		numOfDuplicateCandidates = 0;
		numOfNonReducedCandidates = 0;
		numOfReducedAlternatives = 0;
		numOfDuplicateAnswers = numOfUniqueAnswersFound = 0;		
        numOfNonredundantAnswers = 0;
        numOfStatesChecked = 0;
		ResultDestNodes = null; 
		ResultRelevs = null; 	
		SumResultRelev = SumReducedResultRelev = 0;
	    numOfNKMapLookups2 = 0;
        numOfNKMapLookupsByKeyword = maxNKMap = minNKMap = 0;
		DCG = 0.0; 	DCG2 = 0.0; 	
	}
		
	protected void getResult() {
		// return top-k nodes in T as the query result
//		FibonacciHeapNode<Integer> TNode;
		NodeScore ns;
//		int[] nodes = new int[T.size()];
//		double[] rels = new double[T.size()];
		
		ResultDestNodes = new int[T.size()]; // ArrayList<Integer>(T.size());
		ResultRelevs = new double[T.size()]; // ArrayList<Double>(T.size());
		
		int len = T.size();
		for (int i = len - 1; i >= 0; i--) {
			ns = T.poll();
			ResultDestNodes[i] = ns.nodeId;	// nodeID
			ResultRelevs[i] = ns.score;	// rel
//			nodes[i] = TNode.getData();	// nodeID
//			rels[i] = TNode.getKey();	// rel			 
		}		
/*		for (int i = 0; i < len; i++) {
			ResultDestNodes.add(nodes[i]);
			ResultRelevs.add(rels[i]);
		}
*/	}
	
//	public void printAnswerTree(int u,  List<Integer> srcNodes, PrintWriter pw) {
/*
	public boolean printAnswerTree0(int u,  int[] srcNodes, PrintWriter pw) {
    return true;
    }
*/
	public boolean printAnswerTree(int u,  int[] srcNodes, PrintWriter pw) {
//		Iterator<Integer> it = null;
		List<DefaultEdge> el = null;		
		Iterator<DefaultEdge> elit1 = null, elit2 = null;
		String uName = null;
		boolean areAllFstNodesTheSame = true, destNodeContainsAKeyword = false;
		int fstNode = -1, FstFstNode = -1;

		connect();

		uName = getNodeName(u);
		int i = 0;
//		it = srcNodes.iterator();
//		while (it.hasNext()) {
//			int v = it.next();	
		for ( ; i < srcNodes.length; i++) {
			int v = srcNodes[i];
			pw.print("  path: " + i); 
			if (v == u) {
				pw.print("["+ u + " -> " + v +"], ");
				pw.println("[" + uName + " -> " + uName + "]");
				destNodeContainsAKeyword = true;
				if (i == 0) FstFstNode = u;
				continue;
			}
			
			el = BellmanFordShortestPathMod.findPathBetween(graph, u, v);

            if (el==null) {
			    System.out.println(u + "," + v);
			    pw.print(" -> " + fstNode);
			    pw.print("["+ u + " -> ??? -> " + v + "]");
			    pw.println();
                continue;
            }
                
			pw.print("["+ u);
			elit1 = el.iterator();
			
			// check first nodes to find if it is a non-reduced answer tree
			fstNode = (Integer) elit1.next().getTarget();
			pw.print(" -> " + fstNode);
			if (i == 0) FstFstNode = fstNode;
			else if (fstNode != FstFstNode) areAllFstNodesTheSame = false;
			
			while (elit1.hasNext()) {
				pw.print(" -> " + elit1.next().getTarget());				
			}
			pw.print("], ");
			
			pw.print("["+ uName);
			elit2 = el.iterator();
			while (elit2.hasNext()) {
				pw.print(" -> " + getNodeName((Integer)(elit2.next().getTarget())));				
			}
			pw.println("] ");
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

	public void printStat(PrintWriter pw, double elapseTime) {
		if (ResultDestNodes != null)
			pw.print(ResultDestNodes.length + ", ");
		pw.print(numOfNonReducedAnswers + ", ");
		pw.print(numOfDuplicateAnswers + ", ");
		pw.print(numOfNonredundantAnswers + ", ");
		pw.print(numOfAccInUpdatePeriod + ", ");
		pw.print(numOfNKMapLookups + ", ");
		pw.print(numOfAnswerTreeExplored + ", ");
		pw.print(numOfNonReducedCandidates + ", ");
		pw.print(numOfReducedAlternatives + ", ");
		pw.print(numOfDuplicateCandidates + ", ");
		pw.print(numOfUniqueAnswersFound + ": ");
		pw.print(numOfStatesChecked+ ": ");
        if (ResultRelevs != null) {
    		pw.printf("%.2f, %.2f: ",SumResultRelev/ResultRelevs.length, 
				SumReducedResultRelev/(ResultRelevs.length-numOfNonReducedAnswers));
    		pw.printf("%.2f:: ",SumResultRelev/ResultRelevs.length/ResultRelevs[0]); 
    		pw.printf("%.2f ", SumOfScoresOfAnswerTreesExplored/numOfAnswerTreeExplored/ResultRelevs[0]);
    		pw.printf("%.2f, ", DCG); 
    		pw.printf("%.2f, ", DCG2); 
        }
/*		double min = 10000.0, max = 0.0, sum = 0.0;
		for (double r : ResultRelevs) {
			if (r < min) min = r;
			if (r > max) max = r;
			sum += r;
		}
		pw.printf("(%.2f, %.2f, %.2f): ",  min, max, sum/ResultRelevs.length);
	*/	//pw.print(elapseTime + ", ");		
        pw.printf("(%.2f, %d, %d)",
		    numOfNKMapLookups2 / (float)numOfNKMapLookupsByKeyword, maxNKMap, minNKMap);
        pw.println();
	}
	
	public String getNodeName(int nodeId) {
		String name = null;		
		try {
			pstmt = conn.prepareStatement(Params.getSQLForName(nodeId));
			pstmt.setInt(1, nodeId);
			rs = pstmt.executeQuery();
			while(rs.next()) 
				name = rs.getString(1); 				
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				pstmt.close();
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return name;
	}
	
	// 데이터베이스 연결관련 변수 선언
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	
	// 데이터베이스 연결 메서드
	void connect() {
		if (conn != null) return;
		// JDBC 드라이버 로드
		try {
			Class.forName(Params.jdbc_driver);
			// 데이터베이스 연결정보를 이용해 Connection 인스턴스 확보
			conn = DriverManager.getConnection(Params.jdbc_url, Params.mysql_user, Params.mysql_pw);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// 데이터베이스 연결 종료 메서드
	void disconnect() {
		if(rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(pstmt != null) {
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} 
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		conn = null; pstmt = null; rs = null;
	}

	public void printStatHeader(PrintWriter pw) {
		pw.print("ResultDestNodes.length, ");
		pw.print("numOfNonReducedAnswers, ");
		pw.print("numOfDuplicateAnswers, ");
		pw.print("numOfNonredundantAnswers, ");
		pw.print("numOfAccInUpdatePeriod, ");
		pw.print("numOfNKMapLookups, ");
		pw.print("numOfAnswerTreeExplored, ");
		pw.print("numOfNonReducedCandidates, ");
		pw.print("numOfReducedAlternatives, ");
		pw.print("numOfDuplicateCandidates, ");
		pw.print("numOfUniqueAnswersFound: ");
		pw.print("numOfStatesChecked: ");
		pw.println();
        if (ResultRelevs != null) {
    		pw.printf("(SumResultRelev/ResultRelevs.length, SumReducedResultRelev/(ResultRelevs.length-numOfNonReducedAnswers)");
    		pw.printf("(SumResultRelev/ResultRelevs.length/ResultRelevs[0])"); 
    		pw.printf("(SumOfScoresOfAnswerTreesExplored/numOfAnswerTreeExplored/ResultRelevs[0])");
        }
//		pw.print("elapseTime, ");		
        pw.printf("(numOfNKMapLookups2/(float)numOfNKMapLookupsByKeyword, maxNKMap, minNKMap)");
        pw.println();
	}

}
