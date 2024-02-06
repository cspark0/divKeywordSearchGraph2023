package util;

import java.util.ArrayList;
import java.util.List;

public class State { // implements Comparable<State> {
	public List<AnswerTree> answer;
	public int pos;
	public float score;
	public float ub;
	public float sumDiss;
	// public boolean solChecked;

	public State() {
		this.answer = new ArrayList<AnswerTree>();
		this.pos = -1;
		this.score = 0;
		this.ub = 0;
		this.sumDiss = 0;
	}
	
	public State(List<AnswerTree> prevAnswer, AnswerTree t, float score, int pos, float sumDiss) {
		this.answer = new ArrayList<AnswerTree>(prevAnswer);
		this.answer.add(t);
		this.score = score;
		this.pos = pos;
		this.ub = 0;
		// this.ub = ub;
		this.sumDiss = sumDiss;
	}
	
	/*
	public State(List<AnswerTree> answer, float score, float sumDiss) {
		this.answer = answer;
		this.score = score;
		this.sumDiss = sumDiss;
	}
	*/

	@Override
	public String toString() {
		return "State [answer=" + answer + ", pos=" + pos + ", score=" + score + ", ub=" + ub + ", sumDiss=" + sumDiss
				+ "]";
	}

}
