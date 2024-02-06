package org.jgrapht.util;

import util.State;

public class StatePool {
    public State[] arr;
    protected int size;
    protected int curInd;

    public StatePool(){ }

    public StatePool(int size) {
        this.size = size;
        arr = new State[size];
        for (int i = 0; i < size; i++) arr[i] = new State();
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
    public State getState(int i, int j, int srcNode, State p, float score, float ub, float lb) throws Exception {

//        if (curInd == size-1) 
 //           throw new Exception("no more state node!!!");
  //      else {
            State n = arr[curInd++]; 
            // n.set(i, j, srcNode, p, score, ub, lb); 
            return n;
    //    }
    }

    public State getState(int i, int j, boolean checked, State p, float score, float ub, float lb) throws Exception {

//        if (curInd == size-1) 
 //           throw new Exception("no more state node!!!");
  //      else {
            State n = arr[curInd++]; 
            // n.set(i, j, checked, p, score, ub, lb); 
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

