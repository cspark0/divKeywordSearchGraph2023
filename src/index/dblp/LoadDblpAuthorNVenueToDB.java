package index.dblp;

import java.util.StringTokenizer;
import java.sql.*;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import util.Params;

public class LoadDblpAuthorNVenueToDB {

    static Connection conn = null;
	static PreparedStatement pstmt1, pstmt2, pstmt3, pstmt4;
	static Statement stmt;
	static ResultSet rs = null;

    public static void main(String[] args) {

		int numOfAuthors = 0;
		int numOfVenues = 0;

        Params.setExpDB("dblp");
		String DblpDatafile = Params.ExpDB + "/DBLP-citation-Jan8.txt";

	 	BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(DblpDatafile));
			try {
				System.out.println("# of papers: " + br.readLine());
			} catch (IOException e) {
				e.printStackTrace();
			}
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

		connect();
		try {
			pstmt1 = conn.prepareStatement(
				"select id from author where name = ?");

			pstmt2 = conn.prepareStatement(
				"insert into author (name) values (?)");

			pstmt3 = conn.prepareStatement(
				"select id from venue where name = ?");

			pstmt4 = conn.prepareStatement(
				"insert into venue (name) values (?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}

        System.out.println("Begin processing authors and venues...");

		while (true) {
			String line = null; 
			try {
				line = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (line == null) break;	// end of file
			if (line.length() < 2) continue;	// end of file

			switch (line.substring(0,2)) {
				case "#@":
					StringTokenizer st = new StringTokenizer(line.substring(2), ","); 
					while (st.hasMoreTokens()) {
						String author = st.nextToken().trim();
//        				System.out.print(author);

						try {
							pstmt1.setString(1, author);
							rs = pstmt1.executeQuery();		// search author record
							if(rs.next() == false) {		// not found
								pstmt2.setString(1, author);
								pstmt2.executeUpdate();		// insert new author
								numOfAuthors++;
        						System.out.println(numOfAuthors);
							}
							rs.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					break;

				case "#c":
					if (line.length()==2) break;	// no venue
					String venue = line.substring(2); 
//        			System.out.print(venue);
					try {
						pstmt3.setString(1, venue);
						rs = pstmt3.executeQuery();		// search venue record
						if(rs.next() == false) {		// not found
							pstmt4.setString(1, venue);
							pstmt4.executeUpdate();		// insert new venue 
							numOfVenues++;
        					System.out.println(numOfVenues);
						}
						rs.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					break;
			}
//        	System.out.println();
		}		

		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("create index ind_author_name on author(name)");	
			stmt.close();
			System.out.println("index on author(name) was constructed.");

			stmt = conn.createStatement();
			stmt.executeUpdate("create index ind_venue_name on venue(name)");
			System.out.println("index on venue(name) was constructed.");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		disconnect();
    }

	static void connect() {
        try {
            Class.forName(Params.jdbc_driver);
            conn = DriverManager.getConnection(Params.jdbc_url, Params.mysql_user, Params.mysql_pw);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void disconnect() {
        if(rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(pstmt1 != null) {
            try {
                pstmt1.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(pstmt2 != null) {
            try {
                pstmt2.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(pstmt3 != null) {
            try {
                pstmt3.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(pstmt4 != null) {
            try {
                pstmt4.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
