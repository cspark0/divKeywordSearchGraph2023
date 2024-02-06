package util;

public class Params {
	// 데이터베이스 연결관련정보를 문자열로 선언
	/*static final String jdbc_driver = "oracle.jdbc.driver.OracleDriver";
	static final String jdbc_url = "jdbc:oracle:thin:@127.0.0.1:1521";
	*/
	public static int P = 5;	// parameter p
	public static int K; 		// parameter k
	public static double tau;	// parameter tau
	public static double factor;	// = K*(K-1)*(1-tau);
	
//	public static float rm = (float)0.5;	// maximum root duplication parameter
	public static String keyspaceName;	// = "mondial" or "imdb"
	public static String ExpDB = "res/";		
	public static String jdbc_url;
	//public static final String mysql_user = "pittsburgh", mysql_pw = "hgrubsttip";		
	public static final String mysql_user = "cspark", mysql_pw = "cspark1234";		
//	public static final String jdbc_driver = "com.mysql.jdbc.Driver";
	public static final String jdbc_driver = "com.mysql.cj.jdbc.Driver";
	//public static final String jdbc_driver = "org.mariadb.jdbc.Driver";
	public static String nkmap, nkmap1, nkmap2, knnrDir, kns, nmrFile; 
	
	public static final int NumOfExpQueries = 1;
//	public static int UpdatePeriod = 3000;		// period to execute step 2
	public static final int FILE_BLOCK_SIZE = 32*1024*1024;	// bytes
	
	public static final int numOfPathsForNKMapExt = 5;	// number of paths stored in NKMapExt for a pair of (k, n)

	public static int MAX_QUERY_SIZE = 10;	

//	public static void setUpdatePeriod(int up) {
//		UpdatePeriod = up;
//	}
	public static void setExpDB(String dbname) {
		ExpDB += dbname;
		setJDBCURL(dbname);
		keyspaceName = dbname;
		nkmap1 = nkmap = ExpDB + "/NKMapForBlink";
		nkmap2 = ExpDB + "/NKMapForBlink2";
		kns = ExpDB + "/kns/";
		knnrDir = ExpDB + "/knnr/";
        nmrFile = ExpDB + "/NMR.txt";
	}
	static void setJDBCURL(String dbname) {
	//	jdbc_url = "jdbc:mysql://localhost:3306/" + dbname;
	//	jdbc_url = "jdbc:mysql://137.110.161.7:3306/" + dbname;
//		jdbc_url = "jdbc:mysql://saturn.dongduk.ac.kr:3306/" + dbname;
		jdbc_url = "jdbc:mysql://localhost:3306/" + dbname + "?serverTimezone=UTC";
	}
	public static String getSQLForName(int nodeId) {
		StringBuffer sql = new StringBuffer();
	
		if (keyspaceName.equals("mondial")) {
			String tables[] = { "mountain0", "continent0", "country0", "province0", "city0", "organization0", "politics0", "religion0", "lake0", "river0", "sea0", "island0", " ", "desert0" };
			if (nodeId < 10000 || 	// mountain
				nodeId >= 50000 && nodeId < 60000 ||	// organization
				nodeId >= 80000 && nodeId < 90000 ||	// lake
				nodeId >= 110000 && nodeId < 120000  )						// island 	
				sql.append("select concat(name, ' ', text) from ");
			else if (nodeId >= 60000 && nodeId < 70000) 	// religion
				sql.append("select government from ");
			else sql.append("select name from ");
			sql.append(tables[nodeId/10000]);
			sql.append(" where id = ?");
		}
		else if (keyspaceName.equals("jmdb")) {			
			String tables[] = { "movies0", "movies0", "actors0", "", "directors0", "genres0_c", "plots0", "movielinks0", "keywords0_c" } ;
			if (nodeId < 2000000)
				sql.append("select title from ");
			else if (nodeId >= 5000000 && nodeId < 6000000) 	
				sql.append("select genres from ");
			else if (nodeId >= 6000000 && nodeId < 7000000)
				sql.append("select substr(plottext,1,20) from ");
			else if (nodeId >= 7000000 && nodeId < 8000000)
				sql.append("select substr(movielinkstext,1,20) from ");
			else if (nodeId >= 8000000 && nodeId < 9000000)
				sql.append("select keywords from ");
			else 
				sql.append("select name from ");
			sql.append(tables[nodeId/1000000]);
			if (nodeId < 2000000) 
				sql.append(" where movieid = ?");
			else 
				sql.append(" where id = ?");
		}
		else if (keyspaceName.equals("dblp")) {			
			if (nodeId < 2000000)
				sql.append("select name from author where aid = ?");
			else if (nodeId >= 2000000 && nodeId < 4000000)
				sql.append("select title from journalpaper where pid = ?");
			else if (nodeId >= 4000000 && nodeId < 6000000)
				sql.append("select title from confpaper where pid = ?");
			else if (nodeId >= 6000000 && nodeId < 7000000)
				sql.append("select title from proceedings where cid = ?");
			else if (nodeId >= 7000000)
				sql.append("select title from journal where jid = ?");	
		}
		else if (keyspaceName.equals("dblpconf")) {			
			if (nodeId < 2000000)
				sql.append("select name from authorwpaper where aid = ?");
			/*else if (nodeId >= 2000000 && nodeId < 4000000)
				sql.append("select title from journalpaper where pid = ?");
			*/
			else if (nodeId >= 4000000 && nodeId < 6000000)
				sql.append("select title from confpaper where pid = ?");
			else if (nodeId >= 6000000 && nodeId < 7000000)
				sql.append("select title from proceedingswpaper where cid = ?");
			/*else if (nodeId >= 7000000)
				sql.append("select title from journal where jid = ?");*/	
		}
		return sql.toString();
	}
}
