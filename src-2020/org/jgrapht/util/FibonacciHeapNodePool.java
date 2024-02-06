package org.jgrapht.util;

import java.lang.reflect.Array;
import util.State;

public class FibonacciHeapNodePool<T>{
    protected Object[] arr;
    protected int size;
    protected int curInd;
//    protected StatePool spool;

    public FibonacciHeapNodePool(){ }

    public FibonacciHeapNodePool(int size) { //, StatePool sp){

        //arr = new FibonacciHeapNode<T>[size];
//        @SuppressWarnings("unchecked")
//        arr = (FibonacciHeapNode<T>[])Array.newInstance(FibonacciHeapNode<c>, size);
        arr = new Object[size];
        for (int i = 0; i < size; i++) arr[i] = new FibonacciHeapNode<T>();
        this.size = size;
        this.curInd = 0;
//        this.spool = sp;
    }
    
    public FibonacciHeapNode<T> getFibonacciHeapNode(T data) throws Exception {
       /* if (head == null) 
            throw new Exception("no more heap node!!!");
        else {
*/
            FibonacciHeapNode<T> n = (FibonacciHeapNode<T>)arr[curInd++]; 
            n.set(data); 
            return n;
 //       }
    }

/*
    public void returnFibonacciHeapNode(FibonacciHeapNode<T> node) {
        spool.returnState(node.getData());
        node.setRight(head);
        head = node;
    }

    public FibonacciHeapNode<T> getHead() { return head; }
*/

    public void clear() { curInd = 0;} 

}

