package util;

import java.util.ArrayList;
import java.util.List;

public class State { // implements Comparable<State> {
	public static int count = 0;
	public List<AnswerTree> answer;
	public int pos;
	public double score;
	public double ub;
	public double sumDiss;
	// public boolean solChecked;

	public State() {
		this.answer = new ArrayList<AnswerTree>();
		this.pos = -1;
		this.score = 0;
		this.ub = Double.MAX_VALUE;
		this.sumDiss = 0;
		count++;
	}
	
	public State(List<AnswerTree> prevAnswer, AnswerTree t, double score, int pos, double sumDiss) {
		this.answer = new ArrayList<AnswerTree>(prevAnswer);
		this.answer.add(t);
		this.score = score;
		this.pos = pos;
		this.ub = Double.MAX_VALUE; // value must be computed and assigned 
		// this.ub = ub;
		this.sumDiss = sumDiss;
		count++;
	}

	public void setState(List<AnswerTree> prevAnswer, AnswerTree t, double score, int pos, double sumDiss) {
		this.answer.addAll(prevAnswer);
		this.answer.add(t);
		this.score = score;
		this.pos = pos;
		this.ub = Double.MAX_VALUE; // value must be computed and assigned 
		// this.ub = ub;
		this.sumDiss = sumDiss;
	}
	
	public void reset() {
		/*
		if (this.answer == null)
			this.answer = new ArrayList<AnswerTree>();
		*/
		this.answer.clear();
		this.pos = -1;
		this.score = 0;
		this.ub = Double.MAX_VALUE;
		this.sumDiss = 0;
	}
	
	/*
	public State(List<AnswerTree> answer, double score, double sumDiss) {
		this.answer = answer;
		this.score = score;
		this.sumDiss = sumDiss;
	}
	*/

	public boolean hasTheSameAnswer(State other) {
		if (other == null) return false;
		return (this.answer.equals(other.answer));
	}

	@Override
	public String toString() {
		return "State [answer=" + answer + ", pos=" + pos + ", score=" + score + ", ub=" + ub + ", sumDiss=" + sumDiss
				+ "]";
	}

}
