package util;

public class EnhRelQueue extends RelQueue {
	protected static EnhQueueNodePool pool = new EnhQueueNodePool();	
	public EnhRelQueue() {
		super();
	}

	protected EnhQueueNode allocQueueNode(float r, int sNode, short index) {
		EnhQueueNode n;
		if (numOfNodes < Params.P){
			n = pool.createQueueNode(r, sNode, index);			
		}
		else {	// numOfNodes == Params.P
			if (cand != null) {	// some nodes are in cand list
				if (r <= cand.rel) return null;				// r cannot be inserted into the queue!
				n = (EnhQueueNode)cand; cand = cand.next;	// remove the min-valued node n
			}
			else {				// all nodes are in sel list
				if (r <= sel.rel) return null;				// r cannot be inserted into the queue!
				n = (EnhQueueNode)sel; sel = sel.next;	// remove the min-valued node n in sel list
				numOfSelNodes--; sumOfSelectedRels -= n.rel;
			}	
			numOfNodes--;
			n.rel = r; n.srcNode = sNode; n.listIndex = index; n.next = null;	// re-use n			
		}
		return n;
	}
	
	// for adding next relevance value (set sNode to 0)
	public void add(float r, float threshold, short index) {		
		EnhQueueNode n;
		if (r >= threshold) {		// insert into selected nodes list
			if (sel == null || r <= sel.rel) { 
				if ((n = allocQueueNode(r, 0, index)) == null) return;
				n.next = sel; sel = n;
			}
			else {	
				QueueNode m = sel;
				for (; m.next != null; m = m.next) {
					if (r <= m.next.rel) break;
				}
				n = allocQueueNode(r, 0, index);
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
				if ((n = allocQueueNode(r, 0, index)) == null) return;
				n.next = cand; cand = n;
			}
			else {
				QueueNode m = cand;
				for (; m.next != null; m = m.next) {
					if (r <= m.next.rel) break;
				}
				n = allocQueueNode(r, 0, index);
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

	// for adding (relevance, sNode) from the current entry
	public void add(float r, float threshold, int sNode, short index) {		
		EnhQueueNode n;
		if (r >= threshold) {		// insert into selected nodes list
			if (sel == null || r < sel.rel) { 
				if ((n = allocQueueNode(r, sNode, index)) == null) return;
				n.next = sel; sel = n;
			}
			else if (sel.srcNode == 0 && ((EnhQueueNode)sel).listIndex == index) {
				// sel node is the next relevance entry referring to the current entry
				sel.srcNode = sNode;
				return;
			}
			else {	
				QueueNode m = sel;
				for (; m.next != null; m = m.next) {
					if (r < m.next.rel) { break; }
					if (m.next.srcNode == 0 && ((EnhQueueNode)m.next).listIndex == index) {
						// m.next node is the next relevance entry referring to the current entry
						m.next.srcNode = sNode;
						return;
					}
				}
				if ((n = allocQueueNode(r, sNode, index)) == null) return;
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
			if (cand == null || r < cand.rel) { 
				if ((n = allocQueueNode(r, sNode, index)) == null) return;
				n.next = cand; cand = n;
			}
			else if (cand.srcNode == 0 && ((EnhQueueNode)cand).listIndex == index) {
				// cand node is the next relevance entry referring to the current entry
				cand.srcNode = sNode;
				return;
			}
			else {
				QueueNode m = cand;
				for (; m.next != null; m = m.next) {
					if (r < m.next.rel) { break; }
					if (m.next.srcNode == 0 && ((EnhQueueNode)m.next).listIndex == index) {
						// m.next node is the next relevance entry referring to the current entry
						m.next.srcNode = sNode;
						return;
					}
				}
				if ((n = allocQueueNode(r, sNode, index)) == null) return;
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
	
	@Override
	public void clear() {		// delete all entries in the queue
		if (cand != null) pool.returnQueueNodes(cand);
		if (sel != null) pool.returnQueueNodes(sel);
		cand = sel = null; numOfNodes = numOfSelNodes = 0; sumOfSelectedRels = 0.0;	
	}
}
