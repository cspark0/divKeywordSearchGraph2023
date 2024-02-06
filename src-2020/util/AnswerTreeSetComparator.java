package util;

import java.util.Comparator;

public class AnswerTreeSetComparator implements Comparator<AnswerTreeSet> {
	public AnswerTreeSetComparator() {}

	@Override
	public int compare(AnswerTreeSet s1, AnswerTreeSet s2) {
		return Float.compare(s2.score, s1.score);	// reverse order of rel        
	}
}
