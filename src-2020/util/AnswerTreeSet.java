package util;

import java.util.ArrayList;
import java.util.List;

public class AnswerTreeSet {	
	public List<AnswerTree> answer;		// a set of answer trees
	public float score;					// sum of relevance scores
	public float sumDiss;
	
	/*
	public AnswerTreeSet() {
		this.answer = new ArrayList<AnswerTree>();
		this.score = 0;
		this.sumDiss = 0;
	}
	*/
	
	public AnswerTreeSet(AnswerTree t) {
		this.answer = new ArrayList<AnswerTree>();
		answer.add(t);			
		this.score = t.score;
		this.sumDiss = 0;
	}
	
	public AnswerTreeSet(List<AnswerTree> prevAnswer, AnswerTree t, float score, float sumDiss) { 
		this.answer = new ArrayList<AnswerTree>(prevAnswer);	// copy!
		answer.add(t);										// add t
		this.score = score; 
		this.sumDiss = sumDiss;
	}

	@Override
	public String toString() {
		return "AnswerTreeSet [answer=" + answer + ", score=" + score + ", sumDiss=" + sumDiss + "]";
	}

}
