package index.jmdb;

import java.io.IOException;

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Runtime r = Runtime.getRuntime();
		for (int j = 0; j < 2; j++) {  //135
	      	
			try {
				String[] s = {"java", "nkmap.bdb.NKMapDatabasePut", ""+j};
				Process p = r.exec(s);  
//				p.destroy(); 
//				r.gc();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		System.out.println("Done");
		
	}

}
