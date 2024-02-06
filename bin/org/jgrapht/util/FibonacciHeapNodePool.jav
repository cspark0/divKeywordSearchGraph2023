package org.jgrapht.util;

public class FibonacciHeapNodePool<T>{
    protected FibonacciHeapNode<T> head;
    protected StatePool spool;

    public FibonacciHeapNodePool(){ }

    public FibonacciHeapNodePool(int size, StatePool sp){
        head = null;
        for (int i = 0; i < size; i++) {
            FibonacciHeapNode<T> n = new FibonacciHeapNode<T>(null, head);
            head = n;
        }
        this.spool = sp;
    }
    
    public FibonacciHeapNode<T> getFibonacciHeapNode(T data) throws Exception {
        if (head == null) 
            throw new Exception("no more heap node!!!");
        else {
            FibonacciHeapNode<T> n = head; 
            head = head.getRight();
            n.set(data); 
            return n;
        }
    }

    public void returnFibonacciHeapNode(FibonacciHeapNode<T> node) {
        spool.returnState(node.getData());
        node.setRight(head);
        head = node;
    }

    public FibonacciHeapNode<T> getHead() { return head; }

}

