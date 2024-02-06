package org.jgrapht.util;

import util.StateNew;

public class StatePoolNew {
    public StateNew[] arr;
    protected int size;
    protected int curInd;

    public StatePoolNew(){ }

    public StatePoolNew(int size, int numOfKeywords) {
        this.size = size;
        arr = new StateNew[size];
        for (int i = 0; i < size; i++) arr[i] = new StateNew(numOfKeywords);
        curInd = 0;
    }
    
/*
    public State getState() throws Exception
    {
        if (head == null) 
            throw new Exception("no more state node!!!");
        else {
            State n = head; 
            head = head.getNext();
            return n;
        }
    }
 */       
    public StateNew getState(int i, int[] J, float score, float ub, boolean ch) throws Exception {

//        if (curInd == size-1) 
 //           throw new Exception("no more state node!!!");
  //      else {
            StateNew n = arr[curInd++]; 
            n.set(i, J, score, ub, ch); 
            return n;
    //    }
    }
    public StateNew getState(int i, int[] J, float score, float ub) throws Exception {

//        if (curInd == size-1) 
 //           throw new Exception("no more state node!!!");
  //      else {
            StateNew n = arr[curInd++]; 
            n.set(i, J, score, ub, true); 
            return n;
    //    }
    }
/*
    public State getState(int i, int j, State p, float score, float ub, float lb,  int srcNode, int[] nodes) throws Exception {
        if (head == null) 
            throw new Exception("no more state node!!!");
        else {
            State n = head; 
            head = head.next;
            n.set(i, j, score, ub, lb, srcNode, nodes); 
            return n;
        }
    }
    public void returnState(Object s) {
        ((State)s).next = head;
        head = (State)s;
    }
*/

    public void clear() { curInd = 0; } 

}

