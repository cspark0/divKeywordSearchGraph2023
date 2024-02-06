package util;

import java.util.Comparator;

public class AnswerTreeComparator implements Comparator<AnswerTree> {
	    public AnswerTreeComparator() {}

		@Override
		public int compare(AnswerTree t1, AnswerTree t2) {
			return Float.compare(t2.score, t1.score);	// reverse order of ub        
		}
}
