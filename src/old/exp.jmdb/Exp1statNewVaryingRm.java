package exp.jmdb.old;

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
import util.CPUUtil;

public class Exp1statNewVaryingRm {

	static List<String> query = new ArrayList<String>();
    static CPUUtil cpuutil = new CPUUtil();
	
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
		PrintWriter pw1 = null, pw2 = null ,pw3 = null;
		File f1 = null, f2 = null, f3 = null;
		BufferedReader br = null;
		Params.setExpDB("jmdb");
		try {
//			br = new BufferedReader(new InputStreamReader(System.in));
			br = new BufferedReader(new FileReader(Params.ExpDB + "/rootdup/input.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		DirectedGraph<Integer, DefaultEdge> g = Searcher2.loadGraph();
		
		BlinkSearcherBDB2 searcher1 = new BlinkSearcherBDB2(g);
        	BlinkSearcherBDB_ConstRootDup searcher2 = new BlinkSearcherBDB_ConstRootDup(g);
/*
		BlinkSearcherBDB_RedundantExclusion2 searcher15 = new BlinkSearcherBDB_RedundantExclusion2(g);
		BlinkSearcherBDB_Reduced2 searcher2 = new BlinkSearcherBDB_Reduced2(g);
		BlinkSearcherBDB_NewIOOpt searcher3 = null;
        try {
    		searcher3 = new BlinkSearcherBDB_NewIOOpt(g);
		} catch (Exception e) {
			e.printStackTrace();
		}
        BlinkSearcherBDB_NewBF2 searcher4 = new BlinkSearcherBDB_NewBF2(g);
*/
//		List<Integer> destNodes = null;
	int[] K = {10,20,30}; //, 40, 50};
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


		  float rm = 0.0f;
		  while (rm < 1.0) {
			rm += 0.2;
			Params.rm = rm;
			f1 = new File(Params.ExpDB + "/rootdup/rm" + rm + "/" + query +".txt");
			try {
				pw1 = new PrintWriter(new BufferedWriter(new FileWriter(f1)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			f2 = new File(Params.ExpDB + "/rootdup/rm" + rm + "/" + query +"-stat.txt");
			try {
				pw2 = new PrintWriter(new BufferedWriter(new FileWriter(f2)));
				searcher1.printStatHeader(pw2);
			} catch (IOException e) {
				e.printStackTrace();
			}

			for (int i = 0; i < K.length; i++) {		
				Params.K = K[i];

				pw1.println("Blink Search Using BDB " + K[i]);
//                start = cpuutil.getCpuTime();
				searcher1.search(query);
//                end = cpuutil.getCpuTime();
				searcher1.printResult(query, pw1);
				searcher1.printStat(pw2, (end - start)/1000000000.0);
				searcher1.completeSearch();
//		searcher1.resetNKMapRead();
				pw1.println();

				pw1.println("Blink Search Using ConstRootDup" + K[i]);
 //               start = cpuutil.getCpuTime();
				searcher2.search(query);
  //              end = cpuutil.getCpuTime();
				searcher2.printResult(query, pw1);
				searcher2.printStat(pw2, (end - start)/1000000000.0);
				searcher2.completeSearch();
//		searcher2.resetNKMapRead();
				pw1.println();

/*
				pw1.println("Blink Search Using BDB_nored" + K[i]);
 //               start = cpuutil.getCpuTime();
				searcher15.search(query);
  //              end = cpuutil.getCpuTime();
				searcher15.printResult(query, pw1);
				searcher15.printStat(pw2, (end - start)/1000000000.0);
				searcher15.completeSearch();
//		searcher15.resetNKMapRead();
				pw1.println();
				
				pw1.println("Blink Search Using BDB_reduced " + K[i]);
 //               start = cpuutil.getCpuTime();
				searcher2.search(query);
  //              end = cpuutil.getCpuTime();
				searcher2.printResult(query, pw1);
				searcher2.printStat(pw2, (end - start)/1000000000.0);
				searcher2.completeSearch();
//		searcher2.resetNKMapRead();
				pw1.println();

				pw1.println("Blink Search Using BDB_reducedNunique_Opt " + K[i]);
//				start = System.currentTimeMillis();
   //             start = cpuutil.getCpuTime();
				searcher3.search(query);
//				end = System.currentTimeMillis();
    //            end = cpuutil.getCpuTime();
				searcher3.printResult(query, pw1);
				searcher3.printStat(pw2, (end - start)/1000000000.0);
				searcher3.completeSearch();
//		searcher3.resetNKMapRead();
				pw1.println();				

				pw1.println("Blink Search Using BDB_reducedNunique_BruteForce " + K[i]);
//                start = cpuutil.getCpuTime();
				searcher4.search(query);
//                end = cpuutil.getCpuTime();
				searcher4.printResult(query, pw1);
				searcher4.printStat(pw2, (end - start)/1000000000.0);
				searcher4.completeSearch();
//		searcher4.resetNKMapRead();
				pw1.println();				
*/
			}
			pw1.close();
			pw2.close();
		  }

		}

		searcher1.close();
//		searcher15.close();
		searcher2.close();
//		searcher3.close();
//		searcher4.close();
	}	
}
