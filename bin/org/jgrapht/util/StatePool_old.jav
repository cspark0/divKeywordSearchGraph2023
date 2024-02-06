package org.jgrapht.util;

import util.State;

public class StatePool {
    protected State head;

    public StatePool(){ }

    public StatePool(int size) {
        head = null;
        for (int i = 0; i < size; i++) {
            head = new State(head);
//            State n = new State(head);
//            State n = (State)clazz.newInstance();
 //           n.setNext(head);
        }
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
    public State getState(int i, int srcNode, State p, float score, float ub, float lb) throws Exception {
        if (head == null) 
            throw new Exception("no more state node!!!");
        else {
            State n = head; 
            head = n.next;
            n.set(i, srcNode, p, score, ub, lb); 
            return n;
        }
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
*/
    public void returnState(Object s) {
        ((State)s).next = head;
        head = (State)s;
    }

    public State getHead() { return head; }

}

