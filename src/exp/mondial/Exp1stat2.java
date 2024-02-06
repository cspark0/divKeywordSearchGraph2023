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

import queryProcessing.Searcher;
import queryProcessing.BlinkSearcher;
import queryProcessing.ExhustiveSearcher;
import queryProcessing.Astar0;
import queryProcessing.Astar1;
import queryProcessing.Astar2;
import queryProcessing.Astar12;
import queryProcessing.Astar0nc;
import queryProcessing.Astar1nc;
import queryProcessing.Astar2nc;
import queryProcessing.Astar12nc;
import queryProcessing.Astar1_ap;
import queryProcessing.Astar1nc_ap;
import util.Params;
import util.CPUUtil;

public class Exp1stat2 {

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

		Params.setExpDB("mondial");

//		int k = Integer.parseInt(args[0]);
		boolean blinkS=false, exhustive=false, astar0=false, astar1=false, astar2=false;
		boolean astar0nc=false, astar1nc=false, astar2nc=false;
		boolean astar12=false, astar12nc=false;
		boolean astar1_ap=false, astar1nc_ap=false;

		long start = 0,end = 0;
		PrintWriter pw1 = null, pw2 = null;
		File f1 = null, f2 = null;
		BufferedReader br = null;
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
		Astar0 divKAstar0 = new Astar0(g);
		Astar1 divKAstar1 = new Astar1(g);
		Astar2 divKAstar2 = new Astar2(g);
		Astar0nc divKAstar0nc = new Astar0nc(g);
		Astar1nc divKAstar1nc = new Astar1nc(g);
		Astar2nc divKAstar2nc= new Astar2nc(g);
		Astar12 divKAstar12 = new Astar12(g);
		Astar12nc divKAstar12nc = new Astar12nc(g);
		Astar1_ap divKAstar1_ap = new Astar1_ap(g);
		Astar1nc_ap divKAstar1nc_ap = new Astar1nc_ap(g);

//		List<Integer> destNodes = null;
		//int[] K = {10,20,30}; //, 40, 50};
		int[] K = {5};
//, 40, 50};
		//int[] K = {40, 50};
//		int[] K = {30};
//		int[] K = {10};
//		K[0] = k;

		double tau = 0.5; //0.3; // Double.parseDouble(args[1]); 
		int qsize = 3; //0.3; // Double.parseDouble(args[1]); 
		
		blinkS = true;
		exhustive = true;
		//astar0 = true; 
		//astar1 = true; 
		astar2 = true; 
		//astar12 = true; 
		//astar0nc = true; 
		//astar1nc = true; 
		//astar2nc = true; 
		//astar12nc = true; 
		//astar1_ap = true; 
		astar1nc_ap = true; 

		String filename = "/t" + tau + ".q" + qsize;
		if (K.length == 1) filename += ".k" + K[0];
		f1 = new File(Params.ExpDB + filename);
		try {
			pw1 = new PrintWriter(new BufferedWriter(new FileWriter(f1)));
		} catch (IOException e) {
		e.printStackTrace();
		}
		f2 = new File(Params.ExpDB + filename + ".stat");
		try {
			pw2 = new PrintWriter(new BufferedWriter(new FileWriter(f2)));
	//		blink.printStatHeader(pw2);
		} catch (IOException e) {
			e.printStackTrace();
		}

		divKAstar0.printStatHeader(pw2);

		while (true) {
			List<String> query = getKeywordQuery(br);
			if (query == null) break;

			pw1.println("query: " + query);
			pw2.println("query: " + query);

//			if (query.size()==4) 
//				Params.P = 6;
//			else Params.P = 5;
/*
		  for (tau = 0.5; tau < 1.0; tau += 0.1) {

			f1 = new File(Params.ExpDB + "/divtopk/" + query + "-" + tau + ".txt");
			try {
				pw1 = new PrintWriter(new BufferedWriter(new FileWriter(f1)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			//pw1 = new PrintWriter(System.out);
			f2 = new File(Params.ExpDB + "/divtopk/" + query + "-" + tau + "-stat.txt");
			try {
				pw2 = new PrintWriter(new BufferedWriter(new FileWriter(f2)));
				blink.printStatHeader(pw2);
				divKExhustive.printStatHeader(pw2);
				divKAstar1.printStatHeader(pw2);
			} catch (IOException e) {
				e.printStackTrace();
			}
*/
			for (int i = 0; i < K.length; i++) {		
				Params.K = K[i];
				Params.tau = tau; 
				Params.factor = K[i]*(K[i]-1)*(1.0-tau);

			  if (blinkS) { 
				pw1.println("Top-k Search by Blink-BDB, k=" + K[i]);
                start = CPUUtil.getCpuTime();
				blink.search(query);
                end = CPUUtil.getCpuTime();
				blink.printResult(pw1, query);
				blink.printStat(pw2, (end - start)/1000000000.0);
				blink.completeSearch();
				blink.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();
			  }

			  if (exhustive) { 
  			   if (Params.K < 20) {
				pw1.println("Div Top-k Search by Exhustive, k=" + K[i]);
				start = CPUUtil.getCpuTime();
				divKExhustive.search(query);
				end = CPUUtil.getCpuTime();
				divKExhustive.printResult(pw1, query);
				divKExhustive.printStat(pw2, (end - start)/1000000000.0);
				divKExhustive.completeSearch();
				divKExhustive.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();
			   }
			  }
			  
			  if (astar0) { 
				pw1.println("Div Top-k Search by Astar0, k=" + K[i]);
       			start = CPUUtil.getCpuTime();
				divKAstar0.search(query);
		        end = CPUUtil.getCpuTime();
				divKAstar0.printResult(pw1, query);
				divKAstar0.printStat(pw2, (end - start)/1000000000.0);
				divKAstar0.completeSearch();
				divKAstar0.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();
			  }

			  if (astar1) { 
				pw1.println("Div Top-k Search by Astar1, k=" + K[i]);
       			start = CPUUtil.getCpuTime();
				divKAstar1.search(query);
		        end = CPUUtil.getCpuTime();
				divKAstar1.printResult(pw1, query);
				divKAstar1.printStat(pw2, (end - start)/1000000000.0);
				divKAstar1.completeSearch();
				divKAstar1.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();
			  }

			  if (astar2) { 
				pw1.println("Div Top-k Search by Astar2, k=" + K[i]);
       			start = CPUUtil.getCpuTime();
				divKAstar2.search(query);
		        end = CPUUtil.getCpuTime();
				divKAstar2.printResult(pw1, query);
				divKAstar2.printStat(pw2, (end - start)/1000000000.0);
				divKAstar2.completeSearch();
				divKAstar2.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();
			  }

			  if (astar12) { 
				pw1.println("Div Top-k Search by Astar12, k=" + K[i]);
       			start = CPUUtil.getCpuTime();
				divKAstar12.search(query);
		        end = CPUUtil.getCpuTime();
				divKAstar12.printResult(pw1, query);
				divKAstar12.printStat(pw2, (end - start)/1000000000.0);
				divKAstar12.completeSearch();
				divKAstar12.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();
			  }

			  if (astar0nc) { 
				pw1.println("Div Top-k Search by Astar0nc, k=" + K[i]);
       			start = CPUUtil.getCpuTime();
				divKAstar0nc.search(query);
		        end = CPUUtil.getCpuTime();
				divKAstar0nc.printResult(pw1, query);
				divKAstar0nc.printStat(pw2, (end - start)/1000000000.0);
				divKAstar0nc.completeSearch();
				divKAstar0nc.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();
			  }

			  if (astar1nc) { 
				pw1.println("Div Top-k Search by Astar1nc, k=" + K[i]);
       			start = CPUUtil.getCpuTime();
				divKAstar1nc.search(query);
		        end = CPUUtil.getCpuTime();
				divKAstar1nc.printResult(pw1, query);
				divKAstar1nc.printStat(pw2, (end - start)/1000000000.0);
				divKAstar1nc.completeSearch();
				divKAstar1nc.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();
			  }

			  if (astar2nc) { 
				pw1.println("Div Top-k Search by Astar2nc, k=" + K[i]);
       			start = CPUUtil.getCpuTime();
				divKAstar2nc.search(query);
		        end = CPUUtil.getCpuTime();
				divKAstar2nc.printResult(pw1, query);
				divKAstar2nc.printStat(pw2, (end - start)/1000000000.0);
				divKAstar2nc.completeSearch();
				divKAstar2nc.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();
			  }
			
			  if (astar12nc) { 
				pw1.println("Div Top-k Search by Astar12nc, k=" + K[i]);
       			start = CPUUtil.getCpuTime();
				divKAstar12nc.search(query);
		        end = CPUUtil.getCpuTime();
				divKAstar12nc.printResult(pw1, query);
				divKAstar12nc.printStat(pw2, (end - start)/1000000000.0);
				divKAstar12nc.completeSearch();
				divKAstar12nc.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();
			  }

			  if (astar1_ap) { 
				pw1.println("Div Top-k Search by Astar1_ap, k=" + K[i]);
       			start = CPUUtil.getCpuTime();
				divKAstar1_ap.search(query);
		        end = CPUUtil.getCpuTime();
				divKAstar1_ap.printResult(pw1, query);
				divKAstar1_ap.printStat(pw2, (end - start)/1000000000.0);
				divKAstar1_ap.completeSearch();
				divKAstar1_ap.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();
			  }
			
			  if (astar1nc_ap) { 
				pw1.println("Div Top-k Search by Astar1nc_ap, k=" + K[i]);
       			start = CPUUtil.getCpuTime();
				divKAstar1nc_ap.search(query);
		        end = CPUUtil.getCpuTime();
				divKAstar1nc_ap.printResult(pw1, query);
				divKAstar1nc_ap.printStat(pw2, (end - start)/1000000000.0);
				divKAstar1nc_ap.completeSearch();
				divKAstar1nc_ap.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();
			  }
			}
		}

		pw1.close();
		pw2.close();

		blink.close();
		divKExhustive.close();
		divKAstar0.close();
		divKAstar1.close();
		divKAstar2.close();
		divKAstar0nc.close();
		divKAstar1nc.close();
		divKAstar2nc.close();
		divKAstar1_ap.close();
		divKAstar1nc_ap.close();
	}	
}
