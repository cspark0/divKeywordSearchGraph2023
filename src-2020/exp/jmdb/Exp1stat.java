package exp.jmdb;

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

import queryProcessing.Searcher;
import queryProcessing.BlinkSearcher;
import queryProcessing.ExhustiveSearcher;
import queryProcessing.IncrementalSearcher;
import queryProcessing.Astar1;
import queryProcessing.Astar1m;
import queryProcessing.Astar2;
import queryProcessing.Astar2m;
import queryProcessing.Astar2re;
import queryProcessing.Astar3;
import queryProcessing.Astar4;
import util.Params;
import util.CPUUtil;

public class Exp1stat {

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
		Params.setExpDB("jmdb");
		try {
//			br = new BufferedReader(new InputStreamReader(System.in));
			br = new BufferedReader(new FileReader(Params.ExpDB + "/input.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		DirectedGraph<Integer, DefaultEdge> g = Searcher.loadGraph();
		
		BlinkSearcher blink = new BlinkSearcher(g);
//		IncrementalSearcher incremental = new IncrementalSearcher(g);
		ExhustiveSearcher divKExhustive = new ExhustiveSearcher(g);
		Astar1 divKAstar1 = new Astar1(g);
		Astar1m divKAstar1m = new Astar1m(g);
		Astar2 divKAstar2 = new Astar2(g);
		Astar2m divKAstar2m = new Astar2m(g);
//		Astar2re divKAstar2re = new Astar2re(g);
//		Astar3 divKAstar3 = new Astar3(g);
//		Astar4 divKAstar4 = new Astar4(g);

//		List<Integer> destNodes = null;
//		int[] K = {5, 10, 15};//, 20}; //, 20};
		//int[] K = {4, 8, 12, 16}; //, 20};
		int[] K = {16}; //, 20};

		double tau = 0.7; // 0.8;

			f1 = new File(Params.ExpDB + "/divtopk/tau" + tau + ".txt");
			try {
				pw1 = new PrintWriter(new BufferedWriter(new FileWriter(f1)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			f2 = new File(Params.ExpDB + "/divtopk/tau" + tau + "-stat.txt");
			try {
				pw2 = new PrintWriter(new BufferedWriter(new FileWriter(f2)));
		//		blink.printStatHeader(pw2);
			} catch (IOException e) {
				e.printStackTrace();
			}

			blink.printStatHeader(pw2);

		while (true) {
			List<String> query = getKeywordQuery(br);
			if (query == null) break;

				pw1.println("query: " + query);
				pw2.println("query: " + query);

//			if (query.size()==4) 
//				Params.P = 6;
//			else Params.P = 5;
/*
			f1 = new File(Params.ExpDB + "/divtopk/" + query + "-" + tau + ".txt");
			try {
				pw1 = new PrintWriter(new BufferedWriter(new FileWriter(f1)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			f2 = new File(Params.ExpDB + "/divtopk/" + query + "-" + tau + "-stat.txt");
			try {
				pw2 = new PrintWriter(new BufferedWriter(new FileWriter(f2)));
				blink.printStatHeader(pw2);
			} catch (IOException e) {
				e.printStackTrace();
			}
*/
			/***********************************/
			/* warming up cache */
			/***********************************/
				Params.K = K[0];
                start = CPUUtil.getCpuTime();
				blink.search(query);
                end = CPUUtil.getCpuTime();
				blink.printStat(pw2, (end - start)/1000000000.0);
				blink.completeSearch();
				blink.resetNKMapRead();
	
			/***********************************/


			for (int i = 0; i < K.length; i++) {		
				Params.K = K[i];
				Params.tau = tau; 
				Params.factor = K[i]*(K[i]-1)*(1.0-tau);

				pw1.println("Top-k Search by Blink-BDB, k=" + K[i]);
                start = CPUUtil.getCpuTime();
				blink.search(query);
                end = CPUUtil.getCpuTime();
//				blink.printResult(pw1, query);
				blink.printStat(pw2, (end - start)/1000000000.0);
				blink.completeSearch();
				blink.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();


/*
//			if (Params.K < 20) {
				pw1.println("Div Top-k Search by Exhustive" + K[i]);
				start = CPUUtil.getCpuTime();
				divKExhustive.search(query);
				end = CPUUtil.getCpuTime();
//				divKExhustive.printResult(pw1, query);
				divKExhustive.printStat(pw2, (end - start)/1000000000.0);
				divKExhustive.completeSearch();
				divKExhustive.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();
*/
/*				
				pw1.println("Incremental Top-k Search, k=" + K[i]);
                start = CPUUtil.getCpuTime();
				incremental.search_test(query);
                end = CPUUtil.getCpuTime();
                incremental.printResult(pw1, query);
                incremental.printStat(pw2, (end - start)/1000000000.0);
                incremental.completeSearch();
				// incremental.resetNKMapRead();
				pw1.println();

*/


				pw1.println("Div Top-k Search by Astar1" + K[i]);
       			start = CPUUtil.getCpuTime();
				divKAstar1.search(query);
		        end = CPUUtil.getCpuTime();
//				divKAstar1.printResult(pw1, query);
				divKAstar1.printStat(pw2, (end - start)/1000000000.0);
				divKAstar1.completeSearch();
				divKAstar1.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();


/*
				pw1.println("Div Top-k Search by Astar1m" + K[i]);
                start = CPUUtil.getCpuTime();
				divKAstar1m.search(query);
                end = CPUUtil.getCpuTime();
//				divKAstar1m.printResult(pw1, query);
				divKAstar1m.printStat(pw2, (end - start)/1000000000.0);
				divKAstar1m.completeSearch();
				divKAstar1m.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();

*/


				pw1.println("Div Top-k Search by Astar2" + K[i]);
               	start = CPUUtil.getCpuTime();
				divKAstar2.search(query);
		        end = CPUUtil.getCpuTime();
//				divKAstar2.printResult(pw1, query);
				divKAstar2.printStat(pw2, (end - start)/1000000000.0);
				divKAstar2.completeSearch();
				divKAstar2.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();


/*
				pw1.println("Div Top-k Search by Astar2m" + K[i]);
                start = CPUUtil.getCpuTime();
				divKAstar2m.search(query);
                end = CPUUtil.getCpuTime();
//				divKAstar2m.printResult(pw1, query);
				divKAstar2m.printStat(pw2, (end - start)/1000000000.0);
				divKAstar2m.completeSearch();
				divKAstar2m.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();

/*
				pw1.println("Div Top-k Search by Astar2re" + K[i]);
               	start = CPUUtil.getCpuTime();
				divKAstar2re.search(query);
		        end = CPUUtil.getCpuTime();
				divKAstar2re.printResult(pw1, query);
				divKAstar2re.printStat(pw2, (end - start)/1000000000.0);
				divKAstar2re.completeSearch();
				divKAstar2re.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();

				pw1.println("Div Top-k Search by Astar3" + K[i]);
                start = CPUUtil.getCpuTime();
				divKAstar3.search(query);
                end = CPUUtil.getCpuTime();
				divKAstar3.printResult(pw1, query);
				divKAstar3.printStat(pw2, (end - start)/1000000000.0);
				divKAstar3.completeSearch();
				divKAstar3.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();

				pw1.println("Div Top-k Search by Astar4" + K[i]);
                start = CPUUtil.getCpuTime();
				divKAstar4.search(query);
                end = CPUUtil.getCpuTime();
				divKAstar4.printResult(pw1, query);
				divKAstar4.printStat(pw2, (end - start)/1000000000.0);
				divKAstar4.completeSearch();
				divKAstar4.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();
*/
			}
		//	pw1.close();
//			pw2.close();
		}

		pw1.close();
		pw2.close();

		blink.close();
//		incremental.close();
		divKExhustive.close();
		divKAstar1.close();
		divKAstar1m.close();
		divKAstar2.close();
		divKAstar2m.close();

		//divKAstar2re.close();
	}	
}
