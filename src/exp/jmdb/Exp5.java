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
import queryProc2.ExhustiveSearcher;
import queryProc2.Astar02;
import queryProc2.Astar1nc_ap_new;
import queryProc2.SimGrpSearcher;
import util.Params;
import util.CPUUtil;

public class Exp5 {

	static List<String> query = new ArrayList<String>();
	static List<String> emptyList = new ArrayList<String>();
    // static CPUUtil cpuutil = null;
	
	public static List<String> getKeywordQuery(BufferedReader br) {
		int numOfKeywords;
		query.clear();
		System.out.println("number of queries: ");
		try {
			numOfKeywords = Integer.parseInt(br.readLine());
			if (numOfKeywords == 0) 	// end of the list of queries
				return null;
			if (numOfKeywords < 0) { 	// ignore the query
				for (int i = 0; i < -numOfKeywords; i++) 
					br.readLine();
				return emptyList;
			}
			for (int i = 0; i < numOfKeywords; i++) {
				query.add(br.readLine());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return query;
	}
	
	public static void main(String[] args) {

		Params.setExpDB("jmdb");

		//System.out.println(args[1]);
		boolean blinkS=false, exhustive=false, astar02=false;
		boolean astar1nc_ap_new=false;
		boolean simgrp=false;

//		int k = Integer.parseInt(args[0]);
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
		ExhustiveSearcher divKExhustive = new ExhustiveSearcher(g);
		Astar02 divKAstar02 = new Astar02(g);
		Astar1nc_ap_new divKAstar1nc_ap_new = new Astar1nc_ap_new(g);
		SimGrpSearcher divKSimGrp = new SimGrpSearcher(g);

		int[] K = {5, 10, 15, 20};//, 20}; //, 20};
	//	int K = 15; //, 20};
		//int[] K = {16}; //, 20};
	//	int[] K = {4, 8, 12 };
// 		int[] K = {4,8,12,16}; //, 20};
		//int[] K = {4, 8, 12, 16}; //, 20};
		// int[] K = {5, 10, 15}; //, 20};

		//double tau = Double.parseDouble(args[0]); //0.3; // Double.parseDouble(args[1]); 
		//int qsize = Integer.parseInt(args[1]); //0.3; // Double.parseDouble(args[1]); 
//		double[] tau = {0.2, 0.4, 0.6, 0.8}; //0.5; // Double.parseDouble(args[1]); 
		double[] tau= /*{0.1, 0.15, 0.2, 0.25, 0.3,*/{0.333};// 0.15, 0.2, 0.25, 0.3, 0.35, 0.4}; //0.5; // Double.parseDouble(args[1]); 
		int qnum = 3;//0.3; // Double.parseDouble(args[1]); 

		blinkS = true;  //true;
		exhustive = false;
		//astar0 = true; 
		astar02 = false; 
		//astar1 = true; 
		//astar2 = true; 
		//astar12 = true; 
		//astar0nc = true; 
		//astar1nc = true; 
		//astar2nc = true; 
		//astar12nc = true; 
		//astar1_ap_new = true; 
		astar1nc_ap_new = true; 
		simgrp = true;

		try {
			String filename = "/exp5.q" + qnum + ".t" + tau[0] + ".k" + K[0];
			if (K.length > 1) filename += "-" + K[K.length-1];
//			String filename = "/exp4.k" + K + ".q" + qsize + ".t" + tau[0];
//			if (tau.length > 1) filename += "-" + tau[tau.length-1];
			f1 = new File(Params.ExpDB + filename);
			pw1 = new PrintWriter(new BufferedWriter(new FileWriter(f1)));
			f2 = new File(Params.ExpDB + filename + ".stat");
			pw2 = new PrintWriter(new BufferedWriter(new FileWriter(f2)));
			divKAstar02.printStatHeader(pw2);
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (true) {
			List<String> query = getKeywordQuery(br);
			if (query == null) break;
			if (query.isEmpty()) continue;

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
			/*
				Params.K = K[0];
                start = CPUUtil.getCpuTime();
				blink.search(query);
                end = CPUUtil.getCpuTime();
				blink.printStat(pw2, (end - start)/1000000000.0);
				blink.completeSearch();
				blink.resetNKMapRead();
	
			*/
			/***********************************/

//			Params.K = K;

		for (int i = 0; i < tau.length; i++) {
			Params.tau = tau[i]; 
			pw2.println("thr: " + tau[i]); 
			for (int j = 0; j < K.length; j++) {		
				Params.K = K[j];
				Params.factor = K[j]*(K[j]-1)*(1.0-tau[i]);

			  if (simgrp) { 
  			//   if (Params.K < 20) {
				pw1.println("Div Top-k Search by SimGrp, dissim thr=" + tau[i]);
				start = CPUUtil.getCpuTime();
				divKSimGrp.search(query);
				end = CPUUtil.getCpuTime();
				divKSimGrp.printResult(pw1, query);
				divKSimGrp.printStat(pw2, (end - start)/1000000000.0);
				divKSimGrp.completeSearch();
				divKSimGrp.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();
			 //  }
			  }

			  if (blinkS) { 
			//	pw1.println("Top-k Search by Blink-BDB, tau=" + tau[i]);
                start = CPUUtil.getCpuTime();
				blink.search(query);
                end = CPUUtil.getCpuTime();
			//	blink.printResult(pw1, query);
				//blink.printStat(pw2, (end - start)/1000000000.0);
				blink.completeSearch();
				blink.resetNKMapRead();
			//	pw1.println();
			//	pw1.flush(); pw2.flush();
			  }

			  if (exhustive) { 
  			//   if (Params.K < 20) {
				pw1.println("Div Top-k Search by Exhustive, tau=" + tau[i]);
				start = CPUUtil.getCpuTime();
				divKExhustive.search(query);
				end = CPUUtil.getCpuTime();
				divKExhustive.printResult(pw1, query);
				divKExhustive.printStat(pw2, (end - start)/1000000000.0);
				divKExhustive.completeSearch();
				divKExhustive.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();
			 //  }
			  }
			  
			  /*
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
			*/
			  if (astar02) { 
				//pw1.println("Div Top-k Search by Astar02, tau=" + tau[i]);
       			start = CPUUtil.getCpuTime();
				divKAstar02.search(query);
		        end = CPUUtil.getCpuTime();
				//divKAstar02.printResult(pw1, query);
				//divKAstar02.printStat(pw2, (end - start)/1000000000.0);
				divKAstar02.completeSearch();
				divKAstar02.resetNKMapRead();
				//pw1.println();
				//pw1.flush(); pw2.flush();
			  }
			/*
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
			  
			  if (astar1_ap_new) { 
				pw1.println("Div Top-k Search by Astar1_ap_new, k=" + K[i]);
       			start = CPUUtil.getCpuTime();
				divKAstar1_ap_new.search(query);
		        end = CPUUtil.getCpuTime();
				divKAstar1_ap_new.printResult(pw1, query);
				divKAstar1_ap_new.printStat(pw2, (end - start)/1000000000.0);
				divKAstar1_ap_new.completeSearch();
				divKAstar1_ap_new.resetNKMapRead();
				pw1.println();
				pw1.flush(); pw2.flush();
			  }
		*/	
			  if (astar1nc_ap_new) { 
				//pw1.println("Div Top-k Search by Astar1nc_ap_new, tau=" + tau[i]);
       			start = CPUUtil.getCpuTime();
				divKAstar1nc_ap_new.search(query);
		        end = CPUUtil.getCpuTime();
				//divKAstar1nc_ap_new.printResult(pw1, query);
				//divKAstar1nc_ap_new.printStat(pw2, (end - start)/1000000000.0);
				divKAstar1nc_ap_new.completeSearch();
				divKAstar1nc_ap_new.resetNKMapRead();
				//pw1.println();
				//pw1.flush(); pw2.flush();
			  }

			}
		}
	}	
		pw1.close();
		pw2.close();

		blink.close();
		divKExhustive.close();
		divKAstar02.close();
		divKAstar1nc_ap_new.close();
		divKSimGrp.close();
	}	
}