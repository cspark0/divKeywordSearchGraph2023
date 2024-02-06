package util;

public class Utils {
	public static int findMaxValPos(float[] curScores, int size) {
		int maxPos = 0;
		for (int i = 1; i < size; i++)
			if (curScores[i] > curScores[maxPos]) maxPos = i;
		return maxPos;
	}
}
