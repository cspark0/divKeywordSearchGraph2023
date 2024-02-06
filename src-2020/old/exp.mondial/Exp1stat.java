package exp.mondial.old;

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

import queryProcessing.old.BlinkSearcherBDB2;
import queryProcessing.old.BlinkSearcherBDB_ConstRootDup;
import queryProcessing.old.Searcher2;
import util.Params;

public class Exp1stat {

	static List<String> query = new ArrayList<String>();
	
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
		long start=0,end=0;
		PrintWriter pw1 = null, pw2 = null;
		File f1 = null, f2 = null;
		BufferedReader br = null;
		Params.setExpDB("mondial");
		try {
//			br = new BufferedReader(new InputStreamReader(System.in));
			br = new BufferedReader(new FileReader(Params.ExpDB + "/exp1/input.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		DirectedGraph<Integer, DefaultEdge> g = Searcher2.loadGraph();
		
		BlinkSearcherBDB2 searcher1 = new BlinkSearcherBDB2(g);
		BlinkSearcherBDB_ConstRootDup searcher2 = new BlinkSearcherBDB_ConstRootDup(g);
		
//		List<Integer> destNodes = null;
		int[] K = {10,20,30};
//		int[] K = {30};
//		int[] K = {10};
//		K[0] = k;
		while (true) {
			List<String> query = getKeywordQuery(br);
			if (query == null) break;
//			if (query.size()==4) 
//				Params.P = 6;
//			else Params.P = 5;
			f1 = new File(Params.ExpDB + "/exp1/" + query +".txt");
			try {
				pw1 = new PrintWriter(new BufferedWriter(new FileWriter(f1)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			f2 = new File(Params.ExpDB + "/exp1/" + query +"-stat.txt");
			try {
				pw2 = new PrintWriter(new BufferedWriter(new FileWriter(f2)));
			} catch (IOException e) {
				e.printStackTrace();
			}

			for (int i = 0; i < K.length; i++) {		
				Params.K = K[i];
								
				pw1.println("Blink Search Using BDB " + K[i]);
//				start = System.currentTimeMillis();
				searcher1.search(query);
//				end = System.currentTimeMillis();
				searcher1.printResult(query, pw1);
				searcher1.printStat(pw2, (end - start)/1000.0);
				searcher1.completeSearch();
				pw1.println();
				
				pw1.println("Blink Search Using BDB_ConstDup " + K[i]);
//				start = System.currentTimeMillis();
				searcher2.search(query);
//				end = System.currentTimeMillis();
				searcher2.printResult(query, pw1);
				searcher2.printStat(pw2, (end - start)/1000.0);
				searcher2.completeSearch();
				pw1.println();				
			}
			pw1.close();
			pw2.close();

		}
		searcher1.close();
		searcher2.close();
	}	
}
