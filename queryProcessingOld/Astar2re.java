package queryProcessing;
import util.*;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
//import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;


public class Astar2re extends AstarSearcher {

        List<State> Closed = new ArrayList<State>();

        float lowerBound = 0;   // keep the score of the best sub-optimal div-k answer found in Tops

        public Astar2re() {
        }

        public Astar2re(DirectedGraph<Integer, DefaultEdge> g) {
                super(g);
        }

        @Override
        protected boolean prepareSearch(List<String> query) {
                if (super.prepareSearch(query) == false)
                        return false;
                lowerBound = 0;
                return true;
        }

 //       @Override
 //       protected State heuristicSearch() { return null;}

        @Override
        protected State heuristicSearch(boolean finalRound) {
                int lastIndexOfTops = Tops.size()-1;
                float relOfLastTreeInTops = Tops.get(lastIndexOfTops).score;
                float ub_unseen = 0;
                State s = null, answerFound = null;
                int num_of_states_created = 0;
                int num_of_states_explored = 0;
                int numOfStates = 0;

                System.out.println("New heuristic search starts...");
                if (numOfStatesCreated == 0) {          // first round
                        Q.clear();
                        Closed.clear();
                        Q.add(new State());
                        numOfStatesCreated++;
                        numOfStates++;
                }
                else {
                        updateOpenStates(lastIndexOfTops-1, relOfLastTreeInTops);
                        ub_unseen = createNewChildStates(lastIndexOfTops);
                        numOfStates = Q.size();
                        if (numOfStates > maxNumOfStates) maxNumOfStates = numOfStates;
                }

                while ((s = Q.poll()) != null) {
                        numOfStatesExplored++;
                        num_of_states_explored++;
                        numOfStates--;
//                      System.out.println("H.poll(): " + s.score + ", " + s.ub);

                        if (s.answer.size() == Params.K) {
//                              System.out.println("k-size answer=" + s + ", s.score="+ s.score +", ub_unseen=" + ub_unseen);
                                if (finalRound ||
                                        s.score >= ub_unseen) {
                                        answerFound = s;
                                        System.out.println("optimal answer found!");
                                }
                                else {
                                        lowerBound = s.score;
//                                      System.out.println("New lowerBound: " + lowerBound);
                                }
                                break;
                        }
                        else if (s.pos == lastIndexOfTops) {
//                              System.out.println("incomplete answer=" + s);
//                                break;
                        }

                        boolean s_was_expanded = false;
                        for (int i = s.pos + 1; i <= lastIndexOfTops; i++) {
                                AnswerTree t = Tops.get(i);
                                float newSumDiss = checkDissCondition(s.answer, t, s.sumDiss);
                                if (newSumDiss >= 0) { // diss-condition is satisfied!
                                        State e = new State(s.answer, t, s.score + t.score, i, newSumDiss);
                                        //
                                        //
                                        // start of computeUpperBound(e);
                                        if (e.answer.size() == Params.K) {
                                                e.ub = e.score;
                                        }
                                        else {
	                                        	float ub = e.score;
	                                            int j = e.pos + 1;
	                                            int n = e.answer.size();
	                                            while (j <= lastIndexOfTops && n < Params.K) { 
	                                            	if (checkDissCondition(e.answer, Tops.get(j), e.sumDiss) >= 0) {
	                                                    ub += Tops.get(j).score;
	                                                    n++;
	                                            	}
	                                            	j++;
	                                            }
	                                            if (n < Params.K) {
	                                                ub += (Params.K - n) * relOfLastTreeInTops;
	                                            }
	                                            e.ub = ub;                                        
	                                            /*
	                                            int first = e.pos + 1;
                                                int last = e.pos + Params.K - e.answer.size();
                                                if (last <= lastIndexOfTops) {
                                                        for (int j = first; j <= last; j++)
                                                                ub += Tops.get(j).score;
                                                }
                                                else {  // last > lastIndexOfTops
                                                        for (int j = first; j <= lastIndexOfTops; j++)
                                                                ub += Tops.get(j).score;
                                                        ub += (last - lastIndexOfTops) * relOfLastTreeInTops;
                                                }
                                                e.ub = ub;
                                                */
                                        }
                                        // end of computeUpperBound(e);
                                        //
                                        //
                                        if (e.ub >= lowerBound) {       // it is possible to find a better div-k answer
                                                Q.add(e);
                                                s_was_expanded = true;
                                                numOfStatesCreated++;
                                                num_of_states_created++;
                                                numOfStates++;
                                                if (numOfStates > maxNumOfStates)
                                                        maxNumOfStates = numOfStates;
//                                              System.out.println("New state created: " + e.score + ", " + e.ub);
                                        }
                                        else {
//                                              System.out.println("e.ub " + e.ub +" < lowerBound " + lowerBound + " so break!");
                                        break;          // all remaining child states has ub smaller than lowerBound!
                                        }
                                }
                        }
                        if (s_was_expanded == false) {
                                float ub_unseen_with_s = s.score + (Params.K - s.answer.size()) * relOfLastTreeInTops;
                                // if (ub_unseen_with_s >= lowerBound &&
                                if (ub_unseen_with_s > ub_unseen) {
                                        ub_unseen = ub_unseen_with_s;
                                        // update ub-unseen by upper bound of the score of a set of div k answer trees consisting of s.answer and unseen trees outside Tops
//                                      System.out.println("New ub_unseen: " + ub_unseen);
                                }
                        }
                        Closed.add(s);
                }
                System.out.print(ub_unseen + ", ");
                if (s != null)
                        System.out.print(s.score + ", " + s.pos + ", " + lastIndexOfTops);
                System.out.println(" created=" + num_of_states_created + ", " + "explored(removed)=" + num_of_states_explored);
                System.out.println();

                if (answerFound == null && s != null) {
                        Q.add(s);
                        numOfStates++;
                }
                return answerFound;
        }

        private void updateOpenStates(int prevLastIndexOfTops, float relOfLastTreeInTops) {
//              System.out.println("updateOpenStates() begins");
                float relDiff = Tops.get(Tops.size()-2).score - relOfLastTreeInTops;
//              System.out.println("H's size: " + Q.size());

                ArrayList<State> openStates = new ArrayList<State>(Q);
                Iterator<State> it = openStates.iterator();
                while (it.hasNext()) {
                        State s = it.next();
//                      System.out.println("State checked: " + s);
                        int last = s.pos + Params.K - s.answer.size();
                        if (last > prevLastIndexOfTops) {
                                s.ub -= ((last - prevLastIndexOfTops) * relDiff);
//                              System.out.println("s.ub updated to " + s.ub);
                                if (s.ub < lowerBound) {
                                        it.remove();
//                                      System.out.println("Removed since ub is lower than lowerBound!");
                                }
                        }
                }
                Q.clear();
//              System.out.println("H cleared: " + H.size());
                Q.addAll(openStates);
//              System.out.println("H reorganized: " + H.size());
        }

        private float createNewChildStates(int lastIndexOfTops) {
//              System.out.println("createNewChildStates() begins");

                float ub_unseen = 0;
                AnswerTree t = Tops.get(lastIndexOfTops);

//              System.out.println("Closed size: " + Closed.size());
//              System.out.println("H's size: " + H.size());
                Iterator<State> it = Closed.iterator();
                while (it.hasNext()) {
                        State s = it.next();
//                      System.out.println("State checked: " + s);

                        float newSumDiss = checkDissCondition(s.answer, t, s.sumDiss);
                        if (newSumDiss >= 0) { // diss-condition is satisfied!
                                // compute ub of new child
                                float child_ub = s.score + t.score;
                                if (s.answer.size() + 1 < Params.K)
                                        child_ub += (Params.K - s.answer.size() - 1) * t.score;

                                if (child_ub >= lowerBound) {
                                        State e = new State(s.answer, t, s.score + t.score, lastIndexOfTops, newSumDiss);
                                        e.ub = child_ub;
                                        Q.add(e);
                                        numOfStatesCreated++;
//                                      System.out.println("    Child state created: " + e);
                                }
                        }
                        else {
                                float ub_unseen_with_s = s.score + (Params.K - s.answer.size()) * t.score;
                                if (ub_unseen_with_s > ub_unseen)
                                        ub_unseen = ub_unseen_with_s;
                        }
                }
//              System.out.println("H's size: " + H.size());
                return ub_unseen;
        }

}


