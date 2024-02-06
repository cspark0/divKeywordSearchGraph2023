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

import queryProcessing.old.BlinkSearcherBDB_NewIOOpt;
import queryProcessing.old.Searcher2;
import util.Params;
import util.CPUUtil;

public class DupfreeReducedUnique {

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
		long start,end;
		PrintWriter pw1 = null, pw2 = null ;
		File f2 = null;
		BufferedReader br = null;
		Params.setExpDB("mondial");
		DirectedGraph<Integer, DefaultEdge> g = Searcher2.loadGraph();
		
//		Searcher[] searcher = new Searcher[4];
//		BlinkSearcherBDB searcher = new BlinkSearcherBDB(g);
//		BlinkSearcherBDB_RedundantExclusion searcher = new BlinkSearcherBDB_RedundantExclusion(g);
//		searcher[2] = new BlinkSearcherBDB_Reduced(g);
		BlinkSearcherBDB_NewIOOpt searcher = null;
        try {
		searcher = new BlinkSearcherBDB_NewIOOpt(g);
		} catch (Exception e) {
			e.printStackTrace();
		}

//		List<Integer> destNodes = null;
		int[] K = {10,20,30};
//		int[] K = {30};
//		int[] K = {10};
//		K[0] = k;
//		String[] M = {"blinks", "blinks-NonRed", "nonRed", "nonRednonDup" };
		pw1 = new PrintWriter(System.out);
		
		f2 = new File(Params.ExpDB + "/exp1/" + "dupfree-reducedUnique" + "-stat-exectime.txt");			
		try {
			pw2 = new PrintWriter(new BufferedWriter(new FileWriter(f2)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
//			br = new BufferedReader(new InputStreamReader(System.in));
			br = new BufferedReader(new FileReader(Params.ExpDB + "/exp1/input.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		while (true) {
			List<String> query = getKeywordQuery(br);
			if (query == null) break;
//			if (query.size()==4) 
//				Params.P = 6;
//			else Params.P = 5;
			pw2.print(query + ":");
//				for (int i = 0; i < K.length; i++) {		
				Params.K = K[2];								
//				pw1.println("Blink Search Using BDB " + K[i]);
//				start = System.currentTimeMillis();
                start = cpuutil.getCpuTime();
				searcher.search(query);
//				end = System.currentTimeMillis();
                end = cpuutil.getCpuTime();
//				searcher.printResult(query, pw1);
	//			searcher1.printStat(pw2, (end - start)/1000.0);
//				pw2.print((end - start)/1000.0 + "  ");	
                pw2.printf("%d ", searcher.accFindUniqTime);
                pw2.printf("%.6f  ", searcher.accFindUniqTime/1000000000.0);
				searcher.completeSearch();
        searcher.resetNKMapRead();

//				}
			pw2.println();
		}
		pw2.close();
		searcher.close();
	}	
}
