package queryProcessing;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.io.PrintWriter;

import my.jgrapht.algorithm.BellmanFordShortestPathMod;
import nkmap.bdb.RelSourceFirst;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import util.TargetList;
import util.AnswerTree;
import util.AnswerTreeComparator;
import util.ListEntry;
import util.Params;

public class DivTopKSearcher extends Searcher {

        PriorityQueue<AnswerTree> R;
        Set<Integer> closed;

        int[] keywordNodes;     // to keep srcNodes read from NKLists in findNewAnaswerTree()
        float upperBoundOfUnseenTree;   // to be used in nextTopAnswer()
        public List<AnswerTree> optimalAnswer;         // optimal div top-k answer!
		public int numOfATSPrunnedByDissCond;
        public int numOfLocalAnswersFound;
		public int maxQSize;

        protected DivTopKSearcher() {
                super();
                init();
        }

        protected DivTopKSearcher(DirectedGraph<Integer, DefaultEdge> g) {
                super(g);
                init();
        }

        private void init() {
                R = new PriorityQueue<AnswerTree>(32, new AnswerTreeComparator()); 
				closed = new HashSet<Integer>(100);
        }

        @Override
        protected boolean prepareSearch(List<String> query) {
                if (super.prepareSearch(query) == false)
                        return false;

                keywordNodes = new int[query.size()];
                upperBoundOfUnseenTree = Float.MAX_VALUE;
                optimalAnswer = null;
				numOfATSPrunnedByDissCond = 0;
        		numOfLocalAnswersFound = 0;
				maxQSize = 0;

                return true;
        }

        @Override
        protected void completeSearch() {
                R.clear();
                closed.clear();

				numOfATSPrunnedByDissCond = 0;
        		numOfLocalAnswersFound = 0;
				maxQSize = 0;
                super.completeSearch();
        }

        private AnswerTree findNewAnswerTree(ListEntry e, List<String> query, int curInd) {
        //              AnswerTree t = new AnswerTree();
        //              float[] nkmapEntRels = new float[query.size()];
                        float score = 0;
        //              boolean areAllfNodesTheSame = true;

                        for (int i = 0; i < query.size(); i++) {        // look up the first NKMap
                                if (i == curInd) {
        //                              System.out.println(curInd);
        //                              System.out.println(e);
        //                              System.out.println(nkmapEntRels);

        //                              nkmapEntRels[curInd] = e.rel;
                                        keywordNodes[i] = e.sNodeID;
                                        score += e.rel;
                                        continue;
                                }
                                nk.setDestNode(e.nodeID);
                                nk.setKeyword(query.get(i));
                                RelSourceFirst rsf = nkmapRead.searchRelSourceFirst(nk);
                                numOfNKMapLookups++;
                                if (rsf == null) {
        //                              System.out.println("There is no path from node "+node+" to keyword "+query.get(i));
        //                              n.clear();
                                        return null;
                                }
        //                      if (rs.getFstNode() != e.fNodeID) areAllfNodesTheSame = false;
        //                      nkmapEntRels[i] = rsf.getRel();
                                keywordNodes[i] = rsf.getSrcNode();
                                score += rsf.getRel();
                        }
                        numOfAnswerTreesExplored++;
        //              SumOfScoresOfAnswerTreesExplored += score;

 		                return new AnswerTree(e.nodeID, score, keywordNodes); //, numOfQueryKeywords);
		}

        protected AnswerTree nextTopAnswer(List<String> query) {

                        if (upperBoundOfUnseenTree == 0)
                                return R.poll();

                        while(true) {
                                if (R.peek() != null && R.peek().score >= upperBoundOfUnseenTree) break;

                                int nextListIndex = getNextListIndex();
                                TargetList curList = targetLists.get(nextListIndex);
                                ListEntry e = curList.getNextEntry();
                                // System.out.print("list#" + nextListIndex + ": ");
                                // System.out.println(e);

                                if (e == null) {                        // all entries in this list have been read
                                        logger.debug("exit since one of the target lists has been read...");
                                        upperBoundOfUnseenTree = 0;
                                        return R.poll();
                                }
                                numOfKNListsRead++;

                                curRel[curList.getIndex()] = e.rel;
                                upperBoundOfUnseenTree = 0;
                                for (int i = 0; i < numOfQueryKeywords; i++)
                                        upperBoundOfUnseenTree += curRel[i];

                                if (closed.contains(e.nodeID) == false) { // first found from inverted lists

                                        AnswerTree t = findNewAnswerTree(e, query, curList.getIndex());

                                        if (t != null) {        // node of e.nodeID has paths to all the keywords.
//                                              System.out.println("New " + t);

                                                R.add(t);
//                                              System.out.println("R.size: " + R.size());
                                        }
                                        closed.add(e.nodeID);

                                }
                        }       // end of while

//                      System.out.println("Next top tree: " + R.peek());
                        return R.poll();                // pop
                }

        protected double checkDissCondition(List<AnswerTree> S, AnswerTree t, double sumDissOfS) {
                // return new SumDiss value Of S Union {t} if diss condition is satisfied,
                //                                         else -1
                for (int i = 0; i < S.size(); i++) {
                        //sumDissOfS += computeDissimilarityByDSC(S.get(i).srcNodes, t.srcNodes);
                        sumDissOfS += S.get(i).computeDissimilarityByDSCWith(t);
                }
                if (2*sumDissOfS >= ((S.size()+1)*S.size() - Params.factor)) {
                        return sumDissOfS;
                }
                return (double)-1.0;
        }

        protected float checkDissCondition_float(List<AnswerTree> S, AnswerTree t, double sumDissOfS) {
                // return new SumDiss value Of S Union {t} if diss condition is satisfied,
                //                                         else -1
                for (int i = 0; i < S.size(); i++) {
                        sumDissOfS += S.get(i).computeDissimilarityByDSCWith(t);
                }
                if (2*sumDissOfS >= ((S.size()+1)*S.size() - Params.factor)) {
                        return (float)sumDissOfS;
                }
                return (float)-1;
        }

        private double computeSumDissimilarity(List<AnswerTree> answer) { 
				double sumDiss = 0.0;
               	for (int i = 0; i < answer.size()-1; i++) 
               		for (int j = i+1; j < answer.size(); j++) 
                        sumDiss += answer.get(i).computeDissimilarityByDSCWith(answer.get(j));
	            return sumDiss;
		}

        protected void getResult(List<AnswerTree> answer, double sumDiss) { // List<AnswerTree> ans) {
                optimalAnswer = answer;
                int n = optimalAnswer.size();
                ResultDestNodes = new int[n]; // ArrayList<Integer>(T.size());
                ResultRelevs = new double[n]; // ArrayList<Double>(T.size());
                double sumRels = 0;
                for (int i = 0; i < n; i++) {
                        ResultDestNodes[i] = optimalAnswer.get(i).rootNodeId;  // nodeID
                        ResultRelevs[i] = optimalAnswer.get(i).score;          // rel
                        sumRels += ResultRelevs[i];
                }
                avgResultRelev = sumRels / n;
	            if (sumDiss == 0)  
					sumDiss = computeSumDissimilarity(answer); 
	            avgResultDiss = sumDiss/(n*(n-1)/2);
        }
}

