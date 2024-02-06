package util;

import java.util.ArrayList;
import java.util.NoSuchElementException;
/**
 * Generic class that implements a min-heap.
 *   @author Dave Reed
 *   @version 11/2/12
 */
//public class MinHeap<State extends Comparable<? super E>>  {
public class MinHeap  {
    private State[] values;
    public int size;
    /**
     * Constructs an empty heap.
     */
    public MinHeap(int size) {
        this.values = new State[size];
        this.size = 0;
    }
    
    /**
     * Identifies the minimum value stored in the heap
     *   @return the minimum value
     */
    public State peek() {			// get min
/*
        if (this.size == 0) {
            throw new NoSuchElementException();
        }
*/
        return this.values[0];
    }
    
    /**
     * Adds a new value to the heap.
     *   @param newValue the value to be added
     */
    public void add(State newValue) {
        int pos = this.size;
        this.values[pos] = newValue;
        this.size++;
        
        while (pos > 0) {
            if (newValue.ub > this.values[(pos-1)/2].ub) {
                values[pos] = this.values[(pos-1)/2];
                pos = (pos-1)/2;
            }
            else {
                break;
            }
        }
        this.values[pos]=newValue;
    }
    
    /**
     * Removes the minimum value from the heap.
     */
    public State poll() {
/*
    	if (this.size == 0) {
            throw new NoSuchElementException();
        }
*/
       State minValue = this.values[0];
       State newValue = this.values[this.size-1];
       this.size--;
       int pos = 0;
       
       if (this.size > 0) {
           while (2*pos+1 < this.size) {
               int minChild = 2*pos+1;
               if (2*pos+2 < this.size &&
                     this.values[2*pos+2].ub > this.values[2*pos+1].ub) {
                   minChild = 2*pos+2;
               }
           
               if (newValue.ub < this.values[minChild].ub) {
                   this.values[pos]=this.values[minChild];
                   pos = minChild;
               }
               else {
                   break;
               }
           }
           this.values[pos]=newValue;
       }      
       return minValue;
    }
    
    /**
     * Converts the heap into its String representation.
     *   @return the String representation
     */
    public String toString() {
        return values.toString();
    }
    
    public int size() { return this.size; }
    public void clear() { this.size = 0; }

/*    
    public static void main(String[] args) {
        int[] items = {5, 3, 7, 6, 1, 4, 2, 8, 10, 12, 19};

       MinHeap<Integer> itemHeap = new MinHeap<Integer>();
        for (int i = 0; i < items.size; i++) {
            itemHeap.add(items[i]);
        }
        int i = 0;
        for (; i < 5; i++) {
            items[i] = itemHeap.minValue();
            itemHeap.remove();
        }
        itemHeap.add(1);  
        itemHeap.add(11);  
        itemHeap.add(3);      
        for (; i < 10; i++) {
            items[i] = itemHeap.minValue();
            itemHeap.remove();
        }

        for (int j : items) {
            System.out.print(j + " ");
        }
        System.out.println();
    }
*/
}
