package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class AnswerTreeSet {	
	public List<AnswerTree> answer;		// a set of answer trees
	public float score;					// sum of relevance scores
	public float sumDiss;
	
	public AnswerTreeSet() {
		this.answer = new ArrayList<AnswerTree>();
		this.score = 0;
		this.sumDiss = 0;
	}
	
	public AnswerTreeSet(AnswerTree t) {
		this.answer = new ArrayList<AnswerTree>();
		this.answer.add(t);			
		this.score = t.score;
		this.sumDiss = 0;
	}
	
	public AnswerTreeSet(List<AnswerTree> prevAnswer, AnswerTree t, float score, float sumDiss) { 
		this.answer = new ArrayList<AnswerTree>(prevAnswer);
		this.answer.add(t);									
		this.score = score; 
		this.sumDiss = sumDiss;
	}

	public void setAnswerTreeSet(AnswerTree t) {
		this.answer.add(t);			
		this.score = t.score;
		this.sumDiss = 0;
	}
	
	public void setAnswerTreeSet(List<AnswerTree> prevAnswer, AnswerTree t, float score, float sumDiss) { 
		this.answer.addAll(prevAnswer);
		this.answer.add(t);									
		this.score = score; 
		this.sumDiss = sumDiss;
	}

	public int setOnlyDissimAnswerTrees(List<AnswerTree> prevAnswer, double dissimThreshold) { 
		AnswerTree curAT = this.answer.get(0);
		Iterator<AnswerTree> iter = prevAnswer.iterator();
		while (iter.hasNext()) {
			AnswerTree t = iter.next();
    		if (t.computeDissimilarityByDSCWith(curAT) >= dissimThreshold) {
				this.answer.add(t);									
				this.score += t.score;
			}
		}
		return this.answer.size();
	}

	public void clear() {
		this.answer.clear(); 
		this.score = 0;
		this.sumDiss = 0;
	}
	
	@Override
	public String toString() {
		return "AnswerTreeSet [answer=" + answer + ", score=" + score + ", sumDiss=" + sumDiss + "]";
	}

}
