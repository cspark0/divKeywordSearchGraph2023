package queryProcessing;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Iterator;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import my.jgrapht.algorithm.BellmanFordShortestPathMod;

import nkmap.bdb.KeywordNode;
import nkmap.bdb.NKMapRead;
import nkmap.bdb.NKMapReadForJmdb;
import nkmap.bdb.NKMapReadForDblp;
import util.InvertedList;
import util.Params;
import util.TargetList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Searcher {

        protected static final Logger logger = LoggerFactory.getLogger(Searcher.class);

        protected Hashtable<String, InvertedList> dictionary;
        protected List<TargetList> targetLists;
        protected int RRListSelector;
        protected float[] curRel;

        protected int numOfQueryKeywords;
        protected int numOfNKMapLookups = 0;
        protected int numOfKNListsRead = 0;
        protected int numOfAnswerTreesExplored = 0;
        protected int[] ResultDestNodes;
        protected double[] ResultRelevs;
        protected DirectedGraph<Integer, DefaultEdge> graph; // = new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);
        protected NKMapRead nkmapRead;  // = new NKMapRead(Params.ExpDB + "/NKMapForBlink");
        protected KeywordNode nk = new KeywordNode();

        public double avgResultRelev;  // average relevance score of the optimal div top-k answer trees
        public double avgResultDiss;   // average dissimilarity of the optimal div top-k answer trees
//      double SumResultRelev, SumReducedResultRelev;
//      double DCG, DCG2;
//      double SumOfScoresOfAnswerTreesExplored = 0.0;
//      int numOfNKMapLookups2 = 0;
//  int numOfNKMapLookupsByKeyword = 0, maxNKMap = 0, minNKMap = 0;

        private void initDictionary() {
                String term;
                try {
                        BufferedReader br = new BufferedReader(new FileReader(Params.ExpDB + "/termlist.bin"));
                        while ((term = br.readLine()) != null) {
                                InvertedList l = new InvertedList(term);
                                dictionary.put(term, l);        // new TermData(l));
                        }
                        br.close();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        protected Searcher() {
//              dictionary = new Hashtable<String, TermData>(32);
                dictionary = new Hashtable<String, InvertedList>(32);
                targetLists = new ArrayList<TargetList>();
                graph = loadGraph();
                initDictionary();
				if (Params.ExpDB.equals("res/jmdb")) 
					nkmapRead = new NKMapReadForJmdb();
				else if (Params.ExpDB.equals("res/dblp")) 
					nkmapRead = new NKMapReadForDblp();
				else 	
					nkmapRead = new NKMapRead();
        	System.out.println(nkmapRead.getMyDbEnv1().getNKMapBlinkDB() + " ");
//        	System.out.println(((NKMapReadForJmdb)nkmapRead).myDbEnv2.getNKMapBlinkDB() );
        }

        protected Searcher(DirectedGraph<Integer, DefaultEdge> g) {
//              dictionary = new Hashtable<String, TermData>(32);
                dictionary = new Hashtable<String, InvertedList>(32);
                targetLists = new ArrayList<TargetList>();
                graph = g;
                initDictionary();
				if (Params.ExpDB.equals("res/jmdb")) 
					nkmapRead = new NKMapReadForJmdb();
				else if (Params.ExpDB.equals("res/dblp")) 
					nkmapRead = new NKMapReadForDblp();
				else 	
					nkmapRead = new NKMapRead();
        	System.out.println(nkmapRead.getMyDbEnv1().getNKMapBlinkDB() + " ");
//        	System.out.println(((NKMapReadForJmdb)nkmapRead).myDbEnv2.getNKMapBlinkDB() );
        }

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
                                graph.addVertex(v);     i++;
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
                                graph.addEdge(u, v);    j++;
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
/*              try {
                        findTargetLists(query);
                } catch (InvalidKeywordException e1) {
                        e1.printStackTrace();
                }
*/
                System.out.println("prepareSearch() by " + this.getClass().getName());
                System.out.println("Params.K= " + Params.K);
                System.out.println(query);
                if (findTargetLists(query) == false) return false;

                RRListSelector = -1;
                numOfQueryKeywords = query.size();
                curRel = new float[numOfQueryKeywords];
                for (int i = 0; i < numOfQueryKeywords; i++) curRel[i] = Float.MAX_VALUE;

        //      numOfUpdates = 0;
                numOfKNListsRead = 0;
                numOfNKMapLookups = 0;
                numOfAnswerTreesExplored = 0;
                ResultDestNodes = null;
                ResultRelevs = null;
                avgResultRelev = 0;
                avgResultDiss = 0;
                return true;
        }

        private boolean findTargetLists(List<String> query) {
                        for (int i = 0; i < query.size(); i++) {
        //                      TermData t = dictionary.get(query.get(i));     // i-th keyword ki

                                InvertedList t = dictionary.get(query.get(i)); // i-th keyword ki
                                if (t == null)
        //                              throw new InvalidKeywordException("Keyword " + query.get(i) + "is not in the dictionary");
                                        return false;
                                TargetList l = new TargetList(t, i);
                                l.openToScan();
                                targetLists.add(l);
                        }
                        return true;
                }

        protected int getNextListIndex() {
                RRListSelector = ++RRListSelector % targetLists.size();
                return RRListSelector;
        }

        protected void completeSearch() {
		        for (int i = 0; i < targetLists.size(); i++)
       			    targetLists.get(i).close();
                targetLists.clear();
        }

        // below vars are to be used in computeDissimilarity()
        protected HashSet<Integer> s1 = new HashSet<Integer>(10);
        protected HashSet<Integer> s2 = new HashSet<Integer>(10);
        protected HashSet<Integer> intersect = new HashSet<Integer>(10);

		/*
        protected double computeDissimilarityByJaccardDist(int[] n1, int[] n2) {
                s1.clear(); intersect.clear(); s2.clear();

				// use Jaccard distance 
                for(int i = 0; i < n1.length; i++) {
                	// assume n1.length == n2.length
               		s1.add(Integer.valueOf(n1[i]));
               		s2.add(Integer.valueOf(n2[i]));
               	}
               	intersect.addAll(s1);	// copy of s1
                intersect.retainAll(s2);    // <- s1 intersect s2
                s1.addAll(s2);             // s1 <- s1 union s2
                return ( (s1.size()-intersect.size())/(double)s1.size() );
        }
		*/

        protected double computeDissimilarityByDSC(int[] n1, int[] n2) {
                s1.clear(); intersect.clear(); s2.clear();

				// use Dice similarity coefficient (DSC)
                for(int i = 0; i < n1.length; i++) {
               		s1.add(Integer.valueOf(n1[i]));
               		s2.add(Integer.valueOf(n2[i]));
               	}
               	intersect.addAll(s1);	// copy of s1
                intersect.retainAll(s2);    // <- s1 intersect s2
                return ((s1.size()+s2.size()-2*intersect.size())/(double)(s1.size()+s2.size()));
        }

        protected boolean printAnswerTree(int u,  int[] srcNodes, PrintWriter pw) {

				if (srcNodes == null) {
					System.out.println("printAnswerTree error!!!: srcNodes is null");
					return false;
				}

//              Iterator<Integer> it = null;
                List<DefaultEdge> el = null;
                Iterator<DefaultEdge> elit1 = null, elit2 = null;
                String uName = null;
                boolean areAllFstNodesTheSame = true, destNodeContainsAKeyword = false;
                int fstNode = -1, FstFstNode = -1;

                connect();

                uName = getNodeName(u);
                int i = 0;
//              it = srcNodes.iterator();
//              while (it.hasNext()) {
//                      int v = it.next();
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

        public void close() {
                nkmapRead.close();
        }

        public void resetNKMapRead() {
                nkmapRead.reset();
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        protected String getNodeName(int nodeId) {
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

        protected void connect() {
                if (conn != null) return;
                try {
                        Class.forName(Params.jdbc_driver);
                        conn = DriverManager.getConnection(Params.jdbc_url, Params.mysql_user, Params.mysql_pw);
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        protected void disconnect() {
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

}

