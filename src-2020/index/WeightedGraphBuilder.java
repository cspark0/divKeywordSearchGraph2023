package index;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ClassBasedEdgeFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import my.jgrapht.algorithm.BellmanFordShortestPathMod;
//import my.jgrapht.algorithm.FloydWarshallShortestPaths;

import org.jgrapht.DirectedGraph;
import util.Params;

import java.io.LineNumberReader;
import java.io.FileReader;

public class WeightedGraphBuilder extends GraphBuilder {
	
	protected WeightedGraph<Integer, DefaultWeightedEdge> g = null;		// graph to build

	protected ClassBasedEdgeFactory<Integer, DefaultWeightedEdge> efactory = null;

	public WeightedGraphBuilder() { 
		g = new DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		efactory = new ClassBasedEdgeFactory<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);

		try {
			vfos = new FileOutputStream(vFile);
			vdos = new DataOutputStream(vfos);			
			efos = new FileOutputStream(eFile);
			edos = new DataOutputStream(efos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean computeNodeNodeRel() { // , int s, int e, int f, float r) {
		// compute and save (n,n,r) triples into a text file, which will be used to be loaded into MySQL DB using LOAD DATAFILE command 
		
		PrintWriter w = null; 
		try {
			w = new PrintWriter(new FileOutputStream(Params.nmrFile)); 
		} catch (IOException e) {
			e.printStackTrace();
		}	
			
		Set<Integer> uSet = g.vertexSet();
		Iterator<Integer> uI = uSet.iterator();
		BellmanFordShortestPathMod<Integer,DefaultWeightedEdge> sp = null;

//		int i = 0;
		
		try {

		//	while (uI.next()<1668713) ; 
			while (uI.hasNext()) {
				int u = uI.next();
				sp = new BellmanFordShortestPathMod<Integer,DefaultWeightedEdge>(g, u, 5);
				sp.setStartVertex(u);
				sp.reset();
				sp.calculateShortestPaths();
				Set<Integer> vSet = sp.getEndVertices();
				Iterator<Integer> vI = vSet.iterator();
//				System.out.println("u="+u+" numOfEndVertices="+vSet.size()); 
				if (u % 100000 == 0)
					System.out.println("u="+u+" numOfEndVertices="+vSet.size()); 
				while (vI.hasNext()) {
					int v = vI.next();	
					if (v == u) {
						w.printf("%d,%d,%d,%f\n", u, u, u, (float)1.0);	// first, add a self-relation!!!
						continue;
					}
					List<DefaultWeightedEdge> l = sp.getPathEdgeListMod(v);
//					int f = (Integer)(l.get(0).getTarget());
// for jmdb NMR(reverse edge graph)
					int f = (Integer)(l.get(l.size()-1).getSource()); // f->v
					// float r = (float)1.0 / (l.size() + 1);	// !!!		
					Iterator<DefaultWeightedEdge> it = l.iterator();
					double pathWt = 0.0;
					while (it.hasNext()) {
						double edgeWt = g.getEdgeWeight(it.next());
						pathWt += edgeWt;
					}
//					System.out.println(pathWt + ", ");
					//float r = (float)(1+Math.log10((double)1.0 / (l.size() + 1)));	// !!!					
					float r = (float)(1+Math.log10((double)1.0 / (pathWt + 1.0)));	// !!!					
					w.printf("%d,%d,%d,%f\n", u, v, f, r);
//					System.out.printf("%d,%d,%d,%f", u, v, f, r);
				}
//				sp = null;
			}
			w.close();			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		finally {
//			disconnect();
		}
		return true;
    }
}

