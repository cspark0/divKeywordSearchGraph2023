package index.jmdb;

import java.sql.*;

import index.GraphBuilder;

// 클래스 선언
public class JMDBGraphBuilder extends GraphBuilder {
	
// field[8-9]: for organization, field[10-12]: for mountain, field[13-15]: for island, field[16-17]: for lake 
	                            // country, politics,    city,    province, religion, continent, river,  sea,    organization,           mountain,                  island,          lake
//	static final int prefix[]   = {20000,  60000,        40000,   30000,    70000,    10000,     90000,  100000, 5000, 0,                0, 0, 0,                   110000, 0, 0,    8000, 0};
//	static final String field[] = {"name", "government", "name",  "name",   "name",   "name",    "name", "name", "name","abbreviation", "name","mountains","type", "name","islands","type", "name","type"};
	
	public void createMovies() {
		connect();
		String sql = "select movieid from movies0";
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				g.addVertex(rs.getInt("movieid"));
				vdos.writeInt(rs.getInt("movieid"));
			}
			rs.close();			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createActors() {
		connect();
		String sql = "select id from actors0";
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				g.addVertex(rs.getInt("id"));
				vdos.writeInt(rs.getInt("id"));
			}
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createMov2ActRelation() {
		connect();
		String sql = "select movieid, actortableid from mov2act0";
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				int movieid = rs.getInt("movieid");
				int actorid = rs.getInt("actortableid");
				g.addEdge(movieid, actorid);
				g.addEdge(actorid, movieid);
				edos.writeInt(movieid);
				edos.writeInt(actorid);
				edos.writeInt(actorid);
				edos.writeInt(movieid);
			}
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createDirectors() {
		connect();
		String sql = "select id from directors0";
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				g.addVertex(rs.getInt("id"));
				vdos.writeInt(rs.getInt("id"));
			}
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createMov2DirRelation() {
		connect();
		String sql = "select movieid, directortableid from mov2dir0";

		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				int movieid = rs.getInt("movieid");
				int directorid = rs.getInt("directortableid");
				g.addEdge(movieid, directorid);
				g.addEdge(directorid, movieid);
				edos.writeInt(movieid);
				edos.writeInt(directorid);
				edos.writeInt(directorid);
				edos.writeInt(movieid);
			}
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}	
	
	public void createPlots() {
		connect();
		String sql = "select id, movieid from plots0";
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				int id = rs.getInt("id");
				int movieid = rs.getInt("movieid");
				g.addVertex(id);
				vdos.writeInt(rs.getInt("id"));
//				g.addEdge(movieid, id);
//				edos.writeInt(movieid);
//				edos.writeInt(id);
				g.addEdge(id, movieid); // reverse edge for NMR !!
    				edos.writeInt(movieid); // normal edge for query processing !!
				edos.writeInt(id);
			}
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createKeywords() {
		connect();
/*
		String sql1 = "select id from keywords0";
		String sql2 = "select a.id, b.movieid from keywords0 a, keywords00 b where a.keyword = b.keyword";
*/
		String sql = "select id, movieid from keywords0_c";
		try {
/*			pstmt = conn.prepareStatement(sql1);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				int id = rs.getInt("id");
				g.addVertex(id);
				vdos.writeInt(id);
			}
			rs.close();
			pstmt.close();
			pstmt = conn.prepareStatement(sql2);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int id = rs.getInt("id");
				int movieid = rs.getInt("movieid");
				g.addEdge(id, movieid); // reverse edge for NMR !!
    			edos.writeInt(movieid);
				edos.writeInt(id);
			}
			rs.close();
*/
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				int id = rs.getInt("id");
				int movieid = rs.getInt("movieid");
				g.addVertex(id);
				vdos.writeInt(rs.getInt("id"));
				g.addEdge(id, movieid); // reverse edge for NMR !!
    				edos.writeInt(movieid); // normal edge for query processing !!
				edos.writeInt(id);
			}
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}

	public void createGenres() {
		connect();
//		String sql1 = "select id from genres0";
//		String sql2 = "select a.id, b.movieid from genres0 a, genres00 b where a.genre = b.genre";
		String sql = "select id, movieid from genres0_c";

		try {
/*
			pstmt = conn.prepareStatement(sql1);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				id = rs.getInt("id");
				g.addVertex(id);
				vdos.writeInt(id);
			}
			rs.close();
			pstmt.close();

			pstmt = conn.prepareStatement(sql2);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				id = rs.getInt("id");
				movieid = rs.getInt("movieid");
				g.addEdge(id, movieid); // reverse edge for NMR !!
    			edos.writeInt(movieid);
				edos.writeInt(id);
			}
			rs.close();
*/
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				int id = rs.getInt("id");
				int movieid = rs.getInt("movieid");
				g.addVertex(id);
				vdos.writeInt(rs.getInt("id"));
				g.addEdge(id, movieid); // reverse edge for NMR !!
    				edos.writeInt(movieid); // normal edge for query processing !!
				edos.writeInt(id);
			}
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}

/*	public void createAkaTitles() {
		connect();
		String sql = "select id, movieid from akatitles0";
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				int id = rs.getInt("id");
				int movieid = rs.getInt("movieid");
				g.addVertex(id);
				vdos.writeInt(rs.getInt("id"));
				g.addEdge(movieid, id);
				edos.writeInt(movieid);
				edos.writeInt(id);
			}
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}*/
	
	public void createMovieLinks() {
		connect();
		String sql = "select id, movieid from movielinks0";
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				int id = rs.getInt("id");
				int movieid = rs.getInt("movieid");
				g.addVertex(id);
				vdos.writeInt(rs.getInt("id"));
				g.addEdge(id, movieid); // reverse edge for NMR !!
    			edos.writeInt(movieid);
				edos.writeInt(id);
			}
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
}
