package exp.mondial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import queryProcessing.DivTopK_Astar1;
import queryProcessing.Searcher;
import queryProcessing.BlinkSearcher;
import queryProcessing.DivTopKAstarSearcher;
import queryProcessing.DivTopKExhustiveSearcher;
import util.Params;
import util.CPUUtil;

public class Exp1statVaryingTau {

	static List<String> query = new ArrayList<String>();
    // static CPUUtil cpuutil = null;
	
	public static List<String> getKeywordQuery(BufferedReader br) {
		int numOfKeywords;
		query.clear();
		System.out.println("number of queries: ");
		try {
			numOfKeywords = Integer.parseInt(br.readLine());
			if (numOfKeywords == 0) return null;
			for (int i = 0; i < numOfKeywords; i++) {
				query.add(br.readLine());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return query;
	}
	
	public static void main(String[] args) {
//		int k = Integer.parseInt(args[0]);
		long start = 0,end = 0;
		PrintWriter pw1 = null, pw2 = null;
		File f1 = null, f2 = null;
		BufferedReader br = null;
		Params.setExpDB("mondial");
		try {
//			br = new BufferedReader(new InputStreamReader(System.in));
			br = new BufferedReader(new FileReader(Params.ExpDB + "/divtopk/input.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		DirectedGraph<Integer, DefaultEdge> g = Searcher.loadGraph();
		
		BlinkSearcher blink = new BlinkSearcher(g);
//		DivTopKExhustiveSearcher divKExhustive = new DivTopKExhustiveSearcher(g);
//		DivTopKAstarSearcher divKAstar1 = new DivTopK_Astar1(g);
//		DivTopKAstarSearcher divKAstar2 = new DivTopK_Astar2(g);
//		DivTopKAstarSearcher divKAstar3 = new DivTopK_Astar3(g);

//		List<Integer> destNodes = null;
		//int[] K = {10,20,30}; //, 40, 50};
		int[] K = {5}; //, 40, 50};
		//int[] K = {40, 50};

//		int[] K = {30};
//		int[] K = {10};
//		K[0] = k;
		while (true) {
			List<String> query = getKeywordQuery(br);
			if (query == null) break;

//			if (query.size()==4) 
//				Params.P = 6;
//			else Params.P = 5;

		  float tau = 0.0f;
		  while (tau < 1.0) {
			tau += 0.2;
			Params.tau = tau;
			f1 = new File(Params.ExpDB + "/divtopk/tau" + tau + "/" + query +".txt");
			try {
				pw1 = new PrintWriter(new BufferedWriter(new FileWriter(f1)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			f2 = new File(Params.ExpDB + "/divtopk/tau" + tau + "/" + query +"-stat.txt");
			try {
				pw2 = new PrintWriter(new BufferedWriter(new FileWriter(f2)));
				blink.printStatHeader(pw2);
			} catch (IOException e) {
				e.printStackTrace();
			}

			for (int i = 0; i < K.length; i++) {		
				Params.K = K[i];

				pw1.println("Top-k Search by Blink-BDB " + K[i]);
                start = CPUUtil.getCpuTime();
				blink.search(query);
                end = CPUUtil.getCpuTime();
				blink.printResult(pw1, query);
				blink.printStat(pw2, (end - start)/1000000000.0);
				blink.completeSearch();
				// blink.resetNKMapRead();
				pw1.println();

			}
			pw1.close();
			pw2.close();
		  }

		}

		blink.close();
	}	
}
