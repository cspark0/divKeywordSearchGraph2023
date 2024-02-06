package index.dblp;

import java.sql.*;

import index.GraphBuilder;

public class DBLPGraphBuilder extends GraphBuilder {
	
// field[8-9]: for organization, field[10-12]: for mountain, field[13-15]: for island, field[16-17]: for lake 
	                            // country, politics,    city,    province, religion, continent, river,  sea,    organization,           mountain,                  island,          lake
//	static final int prefix[]   = {20000,  60000,        40000,   30000,    70000,    10000,     90000,  100000, 5000, 0,                0, 0, 0,                   110000, 0, 0,    8000, 0};
//	static final String field[] = {"name", "government", "name",  "name",   "name",   "name",    "name", "name", "name","abbreviation", "name","mountains","type", "name","islands","type", "name","type"};
	
	public void createPapers() {
		connect();
		String sql = "select id from paper";
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
	
	public void createAuthors() {
		connect();
		String sql = "select id from author";
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
	
	public void createPaper2AuthorRelation() {
		connect();
		String sql = "select pid, aid from paper2author";
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				int pid = rs.getInt("pid");
				int aid = rs.getInt("aid");
				g.addEdge(pid, aid);
				g.addEdge(aid, pid);
				edos.writeInt(pid);
				edos.writeInt(aid);
				edos.writeInt(aid);
				edos.writeInt(pid);
			}
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createVenues() {
		connect();
		String sql = "select id from venue";
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
	
	public void createPaper2VenueRelation() {
		connect();
		String sql = "select id, vid from paper";
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				int id = rs.getInt("id");
				int vid = rs.getInt("vid");
				g.addEdge(vid, id); // reverse edge for NMR !!
    			edos.writeInt(id); // normal edge for query processing !!
				edos.writeInt(vid);
			}
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createCitations() {
		connect();
		String sql = "select pid, refpid from citation";

		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				int pid = rs.getInt("pid");
				int refpid= rs.getInt("refpid");
				g.addEdge(refpid, pid); // reverse edge for NMR !!
				edos.writeInt(pid); // normal edge for query processing !!
				edos.writeInt(refpid);
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
