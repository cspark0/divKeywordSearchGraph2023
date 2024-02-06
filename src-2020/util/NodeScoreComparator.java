package util;

import java.util.Comparator;

public class NodeScoreComparator implements Comparator<NodeScore> {
	    public NodeScoreComparator() {}

		@Override
		public int compare(NodeScore s1, NodeScore s2) {
			return Float.compare(s1.score, s2.score);	      
		}
}
