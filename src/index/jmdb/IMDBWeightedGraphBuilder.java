package index.jmdb;

import java.sql.*;

import index.WeightedGraphBuilder;
import org.jgrapht.graph.DefaultWeightedEdge;

// 클래스 선언
public class IMDBWeightedGraphBuilder extends WeightedGraphBuilder {
	
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
//		String sql = "select id, movieid from keywords0";
		String sql1 = "select id from keywords0";
		String sql3 = "select * from movies2keywords"; 

		int keywordid, movieid, numOfMovs, numOfKwds;
		double weight;

		DefaultWeightedEdge e = null;

		try {
			pstmt = conn.prepareStatement(sql1);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				keywordid = rs.getInt("id");
				g.addVertex(keywordid);
				vdos.writeInt(keywordid);
			}
			rs.close();
			pstmt.close();

			pstmt = conn.prepareStatement(sql3);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				movieid = rs.getInt("movieid");
				keywordid = rs.getInt("keywordid");
				numOfMovs = rs.getInt("numOfMovies");
				numOfKwds = rs.getInt("numOfKeywords");
				
				weight = (double)1.0+Math.log10((double)1.0 + 0.1*numOfKwds + 0.9*numOfMovs);	// !!!					

				e = efactory.createEdge(keywordid, movieid); // reverse edge for NMR !!
				g.setEdgeWeight(e, weight);
				g.addEdge(keywordid, movieid, e); 

	    			edos.writeInt(movieid);
				edos.writeInt(keywordid);
			}
			rs.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			disconnect();
		}
	}

	public void createGenres() {
		connect();
//		String sql = "select id, movieid from genres0";
		String sql1 = "select id from genres0";
		String sql3 = "select * from movies2genres"; 

		int genreid, movieid, numOfMovs, numOfGens;
		double weight;

		DefaultWeightedEdge e = null;

		try {
			pstmt = conn.prepareStatement(sql1);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				genreid = rs.getInt("id");
				g.addVertex(genreid);
				vdos.writeInt(genreid);
			}
			rs.close();
			pstmt.close();

			pstmt = conn.prepareStatement(sql3);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				movieid = rs.getInt("movieid");
				genreid = rs.getInt("genreid");
				numOfMovs = rs.getInt("numOfMovies");
				numOfGens = rs.getInt("numOfGenres");
				
				weight = (double)1.0+Math.log10((double)1.0 + 0.5*numOfGens + 0.5*numOfMovs);	// !!!					

				e = efactory.createEdge(genreid, movieid); // reverse edge for NMR !!
				g.setEdgeWeight(e, weight);
				g.addEdge(genreid, movieid, e); 

	    			edos.writeInt(movieid);
				edos.writeInt(genreid);
			}
			rs.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
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
