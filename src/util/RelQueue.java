package util;

import java.util.ArrayList;
import java.util.List;

public class RelQueue {
	private static QueueNodePool pool = new QueueNodePool();	
	protected QueueNode cand;		// candidate nodes list
	protected QueueNode sel;		// selected nodes list	
	protected int numOfNodes;		// total number of nodes in the queue
	protected int numOfSelNodes;	// number of selected nodes in the queue
	public int getNumOfSelNodes() { return numOfSelNodes; }
	
///////////////// modified for IP&M paper /////////////////////////////////////////////////////	
//	protected double ws;			// worstScore = sum of rel-values in the selected nodes 
	protected double sumOfSelectedRels;	// sum of rel-values in the selected nodes; 
	public double getSumOfSelectedRels() {
		return sumOfSelectedRels;
	}
	public double getSumOfRels() {			// compute the sum of relevances in all nodes
		double sumOfRels = sumOfSelectedRels;		 
		for (QueueNode n = cand; n != null; n = n.next)	sumOfRels += n.rel;
		return sumOfRels; 
	}
	public QueueNode getSelNodeList() { return sel; }
///////////////// modified for IP&M paper /////////////////////////////////////////////////////	
	
	public RelQueue() {
		cand = sel = null; 
		numOfNodes = numOfSelNodes = 0; 
		sumOfSelectedRels = 0.0;
	}
	
	protected QueueNode allocQueueNode(float r, int sNode) {
		QueueNode n;
		if (numOfNodes < Params.P){
			n = pool.createQueueNode(r, sNode);			
		}
		else {	// numOfNodes == Params.P
			if (cand != null) {	// some nodes are in cand list
				if (r <= cand.rel) return null;				// r cannot be inserted into the queue!
				n = cand; cand = cand.next;	// remove the min-valued node n
			}
			else {				// all nodes are in sel list
				if (r <= sel.rel) return null;				// r cannot be inserted into the queue!
				n = sel; sel = sel.next;	// remove the min-valued node n in sel list
				numOfSelNodes--; sumOfSelectedRels -= n.rel;
			}	
			numOfNodes--;
			n.rel = r; n.srcNode = sNode; n.next = null;	// re-use n			
		}
		return n;
	}
	
	public void add(float r, float threshold, int sNode) {		
		QueueNode n;
		if (r >= threshold) {		// insert into selected nodes list
			if (sel == null || r <= sel.rel) { 
				if ((n = allocQueueNode(r, sNode)) == null) return;
				n.next = sel; sel = n;
			}
			else {	
				QueueNode m = sel;
				for (; m.next != null; m = m.next) {
					if (r <= m.next.rel) break;
				}
				n = allocQueueNode(r, sNode);
				if (m != n) {	
					n.next = m.next;
					m.next = n;
				} else {		// n is the new smallest node in sel --> m has been replaced by n
					n.next = sel;	
					sel = n;
				}
			}
			this.sumOfSelectedRels += r; 
			numOfSelNodes++;
		}
		else {							// insert into candidate nodes list
			if (cand == null || r <= cand.rel) { 
				if ((n = allocQueueNode(r, sNode)) == null) return;
				n.next = cand; cand = n;
			}
			else {
				QueueNode m = cand;
				for (; m.next != null; m = m.next) {
					if (r <= m.next.rel) break;
				}
				n = allocQueueNode(r, sNode);
				if (m != n) {	
					n.next = m.next;
					m.next = n;
				} else {		// n is the new smallest node in cand --> m has been replaced by n
					n.next = cand;	
					cand = n;
				}
			}
		}
		numOfNodes++;
	}		

/*	public void add(double r, double threshold) {
	if (numOfNodes < Params.P){
		QueueNode n = pool.createQueueNode(r); 
		insert(n, threshold);
	}
	else {	// numOfNodes == Params.P
		if (cand == null) return;	// numOfSelNodes == numOfNodes
		if (r <= cand.rel) return;
		else { 
			QueueNode n = cand; cand = cand.next;	// remove the min-valued node n
			numOfNodes--;
			n.rel = r; n.next = null;	// re-use n			
			insert(n, threshold);
		}
	}			
}*/

/*	public void add(double r) {			// insert at the right position in the cand list or sel list
		if (sel == null) 
			this.add(r, Double.MAX_VALUE);	// insert into candidate nodes list
		else
			this.add(r, sel.rel);	// use sel.rel as the threshold
	}
*/
/*	
	public void add(float r, int sNode) {			// insert at the right position in the cand list or sel list
		if (sel == null) 
			this.add(r, Float.MAX_VALUE, sNode);	// insert into candidate nodes list
		else
			this.add(r, sel.rel, sNode);	// use sel.rel as the threshold
	}
*/	
	public void reorganizeRelQueue(float threshold) {	// reorganize queue and recompute worstScore
		double s = 0.0;
		QueueNode start, m, n;		
		if (cand == null) return;
		if (cand.rel >= threshold) {	
			start = n = cand;		// We should move all the candidate nodes into the selected nodes list	
			cand = null;		// The candidate nodes list becomes empty!
		}
		else {
			for (m = cand, n = cand.next; n != null; m = n, n = n.next) 
				if (n.rel >= threshold) break;
			if (n == null) return;		// nothing to move
			m.next = null; start = n;	// cut the candidate nodes list before n
		}
		for (m = n; n != null; m = n, n = n.next) {	// at first, n is not null			
			s += n.rel; numOfSelNodes++;
		}
		m.next = sel; sel = start;	// move sub-list starting from "start" into the beginning of the selected nodes list (join)
		this.sumOfSelectedRels += s;				// increased
	}
		
	public List<Integer> getQueueNodes() {		// return selected nodes in the queue
		List<Integer> nodes = new ArrayList<Integer>(numOfSelNodes);
		for (QueueNode m = sel; m != null; m = m.next) {
			nodes.add(m.srcNode); 
		}
		return nodes;		
	}
	
	public void clear() {		// delete all entries in the queue
		if (cand != null) pool.returnQueueNodes(cand);
		if (sel != null) pool.returnQueueNodes(sel);
		cand = sel = null; numOfNodes = numOfSelNodes = 0; sumOfSelectedRels = 0.0;	
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
//		StringBuffer sb = new StringBuffer("RelQueue [ws=" + getWS() + ", num=" + numOfSelNodes + ", ");	
		StringBuffer sb = new StringBuffer("RelQueue [sumOfSelRels=" + sumOfSelectedRels + ", numOfSelRels=" + numOfSelNodes + ", ");	
/*		for (QueueNode m = sel; m != null; m = m.next) {
			sb.append("(" + m.srcNode + ", " + m.rel + "), "); 
		}
		sb.append(" / ");
		for (QueueNode m = cand; m != null; m = m.next) {
			sb.append("(" + m.srcNode + ", " + m.rel + "), "); 
		}		
		sb.append("]");
*/		return sb.toString();
	}		
}
