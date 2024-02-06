package util;

import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * Generic class that implements a min-heap.
 *   @author Dave Reed
 *   @version 11/2/12
 */
public class MinHeap_old<E extends Comparable<? super E>>  {
    private ArrayList<E> values;

    /**
     * Constructs an empty heap.
     */
    public MinHeap_old(int size) {
        this.values = new ArrayList<E>(size);
    }
    
    /**
     * Identifies the minimum value stored in the heap
     *   @return the minimum value
     */
    public E peek() {			// get min
        if (this.values.size() == 0) {
            throw new NoSuchElementException();
        }
        return this.values.get(0);
    }
    
    /**
     * Adds a new value to the heap.
     *   @param newValue the value to be added
     */
    public void add(E newValue) {
        values.add(newValue);
        int pos = this.values.size()-1;
        
        while (pos > 0) {
            if (newValue.compareTo(this.values.get((pos-1)/2)) < 0) {
                values.set(pos, this.values.get((pos-1)/2));
                pos = (pos-1)/2;
            }
            else {
                break;
            }
        }
        this.values.set(pos, newValue);
    }
    
    /**
     * Removes the minimum value from the heap.
     */
    public E poll() {
    	if (this.values.size() == 0) {
            throw new NoSuchElementException();
        }
        E minValue = this.values.get(0);
       E newValue = this.values.remove(this.values.size()-1);
       int pos = 0;
       
       if (this.values.size() > 0) {
           while (2*pos+1 < this.values.size()) {
               int minChild = 2*pos+1;
               if (2*pos+2 < this.values.size() &&
                     this.values.get(2*pos+2).compareTo(this.values.get(2*pos+1)) < 0) {
                   minChild = 2*pos+2;
               }
           
               if (newValue.compareTo(this.values.get(minChild)) > 0) {
                   this.values.set(pos, this.values.get(minChild));
                   pos = minChild;
               }
               else {
                   break;
               }
           }
           this.values.set(pos, newValue);
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
    
    public int size() { return this.values.size(); }
    public void clear() { this.values.clear(); }
    
    public static void main(String[] args) {
        int[] items = {5, 3, 7, 6, 1, 4, 2, 8, 10, 12, 19};

 /*       MinHeap<Integer> itemHeap = new MinHeap<Integer>();
        for (int i = 0; i < items.length; i++) {
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
        System.out.println();*/
    }
}
