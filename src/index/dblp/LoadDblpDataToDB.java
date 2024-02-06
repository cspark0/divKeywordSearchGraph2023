package index.dblp;

import java.util.StringTokenizer;
import java.sql.*;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import util.Params;

public class LoadDblpDataToDB {

	static DblpRecord r = new DblpRecord();
    static Connection conn = null;
	static PreparedStatement pstmt1, pstmt2, pstmt3, pstmt4, pstmt5;
	static ResultSet rs = null;

    public static void main(String[] args) {

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
				"insert into paper (id, title, abstract, year, vid) values (?, ?, ?, ?, ?)");
   	         
			pstmt2 = conn.prepareStatement(
				"select id from author where name = ?");

			pstmt3 = conn.prepareStatement(
				"select id from venue where name = ?");

			pstmt4 = conn.prepareStatement(
				"insert into paper2author (pid, aid) values (?, ?)");

			pstmt5 = conn.prepareStatement(
				"insert into citation (pid, refpid) values (?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		while (true) {
			String line = null; 
			try {
				line = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if ((line == null || // end of file
			     line.length() < 2) && // empty line: end of a record
				r.title != null) {

				// here, a dblp record has been read and stored into r

				//insertPaper2Authors for each author
				StringTokenizer st = new StringTokenizer(r.authors, ","); 
				while (st.hasMoreTokens()) {
					String author = st.nextToken().trim();

					try {
						// find author id
						pstmt2.setString(1, author);
						rs = pstmt2.executeQuery();		
						if(rs.next() == false) {	// not found
							System.out.println("author not found error!!!");
							System.out.println(author + "," + r.pid);
							rs.close();
							break;						
						}
						int aid = rs.getInt("id");
						rs.close();

						// insert new paper2author
						pstmt4.setInt(1, r.pid);
						pstmt4.setInt(2, aid);
						pstmt4.executeUpdate();		// insert new paper2author
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

/////////////////////////////////////////////
/////////////////////////////////////////////
/////////////////////////////////////////////
/////////////////////////////////////////////
//				if (r.pid < 1344930) continue;
/////////////////////////////////////////////
/////////////////////////////////////////////
/////////////////////////////////////////////
/////////////////////////////////////////////

				//insertPaper
				try {
					// find venue id
					int vid = 0;
					if (r.venue != null) {
						pstmt3.setString(1, r.venue);
						rs = pstmt3.executeQuery();		
						if(rs.next() == false) {	// not found
							System.out.println("venue not found error!!!");
							System.out.println(r.venue+"," + r.pid);
							rs.close();
							break;						
						}
						vid = rs.getInt("id");
						rs.close();
					}
					// insert new paper 
					pstmt1.setInt(1, r.pid);
					pstmt1.setString(2, r.title);
					pstmt1.setString(3, r.abstr);
					pstmt1.setString(4, r.year);
					if (r.venue != null) pstmt1.setInt(5, vid);
					else pstmt1.setNull(5, Types.INTEGER);
					pstmt1.executeUpdate();		
				} catch (SQLException e) {
					e.printStackTrace();
				}

				r.clear();
			}

			if (line == null) // end of file
				break; 

			if (line.length() < 2) // empty line 
				continue;

			switch (line.substring(0,2)) {
				case "#*":
					if (line.length() >= 201)
						r.title = line.substring(2,201);
					else 
						r.title = line.substring(2); 
					break;
				case "#@":
					r.authors = line.substring(2); break;
				case "#t":
					r.year = line.substring(2); break;
				case "#c":
					if (line.length()>2)
						r.venue = line.substring(2); 
					else r.venue = null;
					break;
				case "#!":
					if (line.length() >= 5001)
						r.abstr = line.substring(2,5001);
					else 
						r.abstr = line.substring(2); 
					break;
				case "#i":
					r.pid = Integer.parseInt(line.substring(6)); break;
				case "#%":
/////////////////////////////////////////////
/////////////////////////////////////////////
/////////////////////////////////////////////
/////////////////////////////////////////////
//					if (r.pid <= 1344930) break;
/////////////////////////////////////////////
/////////////////////////////////////////////
/////////////////////////////////////////////
/////////////////////////////////////////////
					int refpid = Integer.valueOf(line.substring(2));
					// insertCitation(r.pid, refpid);
					try {
						pstmt5.setInt(1, r.pid);
						pstmt5.setInt(2, refpid);
						pstmt5.executeUpdate();		// insert new citation 
					} catch (SQLException e) {
						e.printStackTrace();
					}
					break;
				default:
					System.out.println("Wrong record error!!!");
					System.out.println(r.pid);
					System.out.println(line);
					return;						
			}
		}		
		disconnect();
    }

	static void connect() {
        try {
            Class.forName(Params.jdbc_driver);
            conn = DriverManager.getConnection(Params.jdbc_url, Params.mysql_user, Params.mysql_pw);
			System.out.println(conn);
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
        if(pstmt5 != null) {
            try {
                pstmt5.close();
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

