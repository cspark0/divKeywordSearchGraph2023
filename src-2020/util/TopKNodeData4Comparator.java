package util;

import java.util.Comparator;

public class TopKNodeData4Comparator implements Comparator<TopKNodeData4> {
	    public TopKNodeData4Comparator() {}

		@Override
		public int compare(TopKNodeData4 s1, TopKNodeData4 s2) {
			return Float.compare(s1.score, s2.score);	// reverse order of ub        
		}
}
