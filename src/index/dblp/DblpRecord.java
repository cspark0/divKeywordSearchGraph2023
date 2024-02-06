package index.dblp;

//import java.util.ArrayList;
//import java.util.List;
//import java.util.StringTokenizer;

public class DblpRecord { 
	public int pid;
	public String title;
	public String abstr;
	public String year;
	public String venue;
	public String authors;
//	public List<String> authors;
//	public List<Integer> citations;

	public DblpRecord() {
//		authors = new List<String>();
//		citations = new List<Integer>();
	}

	public void clear() {
		pid = -1;
		title = abstr = year = venue = authors = null;	
//		authors.clear();
//		citations.clear();
	}
}
