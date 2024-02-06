package index.mondial;

import java.io.IOException;
import java.sql.*;
import index.GraphBuilder;

// 클래스 선언
public class MondialGraphBuilderExt2 extends GraphBuilder {
	
// field[8-9]: for organization, field[10-12]: for mountain, field[13-15]: for island, field[16-17]: for lake 
	                            // country, politics,    city,    province, religion, continent, river,  sea,    organization,           mountain,                  island,          lake
//	static final int prefix[]   = {20000,  60000,        40000,   30000,    70000,    10000,     90000,  100000, 5000, 0,                0, 0, 0,                   110000, 0, 0,    8000, 0};
//	static final String field[] = {"name", "government", "name",  "name",   "name",   "name",    "name", "name", "name","abbreviation", "name","mountains","type", "name","islands","type", "name","type"};
	
	public void createContinent() {
		connect();
		String sql = "select id from continent0";
		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				g.addVertex(rs.getInt("id"));	
				vdos.writeInt(rs.getInt("id"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createCountry() {
		connect();
		String sql1 = "select id from country0";
		String sql2 = "select a.id as cid, c.id as contid " +
				"from country0 a, encompasses b, continent0 c " +
				"where a.code = b.country and b.continent = c.name";
		try {
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				g.addVertex(rs.getInt("id"));
				vdos.writeInt(rs.getInt("id"));
			}
			rs.close(); pstmt.close();
		
			pstmt = conn.prepareStatement(sql2);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int cid = rs.getInt("cid"); 				
				int contid = rs.getInt("contid");
				g.addEdge(cid, contid);
				edos.writeInt(cid);
				edos.writeInt(contid);
//				g.addEdge(contid, cid);		
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
/*	public void createBorderRelation() {	
		connect();
		String sql1 = "select c1.id as c1id, c2.id as c2id " +
					"from country0 c1, borders b, country0 c2 " +
					"where c1.code = b.country1 and c2.code = b.country2";
		
		try {
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int c1id = rs.getInt("c1id"); 				
				int c2id = rs.getInt("c2id");
				g.addEdge(c1id, c2id);
				g.addEdge(c2id, c1id);
				edos.writeInt(c1id);
				edos.writeInt(c2id);
				edos.writeInt(c2id);
				edos.writeInt(c1id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}*/
	
	public void createPolitics() {
		connect();
		String sql = "select p.id as pid, cr.id as crid " +
				"from politics0 p, country0 cr " +
				"where p.country = cr.code";
		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int pid = rs.getInt("pid"); 				
				int crid = rs.getInt("crid");
				g.addVertex(pid);
				vdos.writeInt(rs.getInt("pid"));

//				g.addEdge(pid, crid);
				g.addEdge(crid, pid);		
				edos.writeInt(crid);
				edos.writeInt(pid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createReligion() {
		connect();
		String sql = "select r.id as rid, cr.id as crid " +
				"from religion0 r, country0 cr " +
				"where r.country = cr.code";
		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int rid = rs.getInt("rid"); 				
				int crid = rs.getInt("crid");
				g.addVertex(rid);
				vdos.writeInt(rs.getInt("rid"));

	//			g.addEdge(rid, crid);
				g.addEdge(crid, rid);	
				edos.writeInt(crid);
				edos.writeInt(rid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	public void createProvince() {
		connect();
		String sql = "select p.id as pid, cr.id as crid " +
				"from province0m p, country0 cr " +
				"where p.country = cr.code";
		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int pid = rs.getInt("pid"); 				
				int crid = rs.getInt("crid");
				g.addVertex(pid);
				vdos.writeInt(rs.getInt("pid"));

				g.addEdge(pid, crid);
				g.addEdge(crid, pid);	
				edos.writeInt(pid);
				edos.writeInt(crid);
				edos.writeInt(crid);
				edos.writeInt(pid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createCity() {
		connect();
		String sql1 = "select ct.id as ctid, p.id as pid " +		// city - province 관계
					"from city0 ct, province0m p " +
					"where ct.province = p.name";
		
		String sql2 = "select ct.id as ctid, cr.id as crid " +		// city - country 관계
					"from city0 ct, country0np cr " +
					"where ct.country = cr.code";
		try {
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int ctid = rs.getInt("ctid"); 				
				int pid = rs.getInt("pid");
				g.addVertex(ctid);
				vdos.writeInt(rs.getInt("ctid"));

				g.addEdge(ctid, pid);
				g.addEdge(pid, ctid);	
				edos.writeInt(ctid);
				edos.writeInt(pid);
				edos.writeInt(pid);
				edos.writeInt(ctid);
			}
			rs.close();	pstmt.close();
			
			pstmt = conn.prepareStatement(sql2);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int ctid = rs.getInt("ctid"); 				
				int crid = rs.getInt("crid");
				g.addVertex(ctid);
				vdos.writeInt(rs.getInt("ctid"));

				g.addEdge(ctid, crid);
				g.addEdge(crid, ctid);
				edos.writeInt(ctid);
				edos.writeInt(crid);
				edos.writeInt(crid);
				edos.writeInt(ctid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createOrganization() {
		connect();
		String sql1 = "select id from organization0";
		String sql2 = "select o.id as oid, c.id as cid " +
				"from organization0 o, city0 c " +
				"where o.city = c.name";
		try {
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				g.addVertex(rs.getInt("id"));
				vdos.writeInt(rs.getInt("id"));
			}
			rs.close(); pstmt.close();
		
			pstmt = conn.prepareStatement(sql2);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int oid = rs.getInt("oid"); 				
				int cid = rs.getInt("cid");
//				g.addEdge(oid, cid);
				g.addEdge(cid, oid);		// city -> orga
//				edos.writeInt(oid);
//				edos.writeInt(cid);
				edos.writeInt(cid);
				edos.writeInt(oid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createMemberRelation() {	
		connect();
		String sql1 = "select o.id as oid, c.id as cid " +
					"from organization0 o, isMember i, country0 c " +
					"where o.abbreviation = i.organization and c.code = i.country";
		
		try {
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int oid = rs.getInt("oid"); 				
				int cid = rs.getInt("cid");
//				g.addEdge(oid, cid);
				g.addEdge(cid, oid);	// country -> orga
//				edos.writeInt(oid);
//				edos.writeInt(cid);
				edos.writeInt(cid);
				edos.writeInt(oid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createLake() {
		connect();
		String sql1 = "select id from lake0";
		String sql2 = "select s.id as sid, p.id as pid " +		// lake - province
				"from lake0 s, geo_lake g, province0m p " +
				"where s.name = g.lake and g.province = p.name";
		String sql3 = "select s.id as sid, c.id as cid " +		// lake - country
				"from lake0 s, geo_lake g, country0np c " +
				"where s.name = g.lake and g.country = c.code";
		try {
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				g.addVertex(rs.getInt("id"));
				vdos.writeInt(rs.getInt("id"));
			}
			rs.close(); pstmt.close();
		
			pstmt = conn.prepareStatement(sql2);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int sid = rs.getInt("sid"); 				
				int pid = rs.getInt("pid");
				g.addEdge(sid, pid);
				g.addEdge(pid, sid);
				edos.writeInt(sid);
				edos.writeInt(pid);
				edos.writeInt(pid);
				edos.writeInt(sid);
			}
			rs.close(); pstmt.close();
			
			pstmt = conn.prepareStatement(sql3);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int sid = rs.getInt("sid"); 				
				int cid = rs.getInt("cid");
				g.addEdge(sid, cid);
				g.addEdge(cid, sid);	
				edos.writeInt(sid);
				edos.writeInt(cid);
				edos.writeInt(cid);
				edos.writeInt(sid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createSea() {
		connect();
		String sql1 = "select id from sea0";
		String sql2 = "select s.id as sid, p.id as pid " +		// sea - province
				"from sea0 s, geo_sea g, province0m p " +
				"where s.name = g.sea and g.province = p.name";
		String sql3 = "select s.id as sid, c.id as cid " +		// sea - country
					"from sea0 s, geo_sea g, country0np c " +
					"where s.name = g.sea and g.country = c.code";
		int sid=0, pid=0, cid=0;
		
		try {
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				g.addVertex(rs.getInt("id"));
				vdos.writeInt(rs.getInt("id"));
			}
			rs.close(); pstmt.close();
		
			pstmt = conn.prepareStatement(sql2);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				sid = rs.getInt("sid"); 				
				pid = rs.getInt("pid");
				g.addEdge(sid, pid);
				g.addEdge(pid, sid);	
				edos.writeInt(sid);
				edos.writeInt(pid);
				edos.writeInt(pid);
				edos.writeInt(sid);
			}
			rs.close(); pstmt.close();
			
			pstmt = conn.prepareStatement(sql3);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				sid = rs.getInt("sid"); 				
				cid = rs.getInt("cid");
				g.addEdge(sid, cid);
				g.addEdge(cid, sid);	
				edos.writeInt(sid);
				edos.writeInt(cid);
				edos.writeInt(cid);
				edos.writeInt(sid);
			}
		} catch (IOException e) {
			e.printStackTrace();			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e2) {
			System.out.println(sid + ", " + pid);
		}
		finally {
			disconnect();
		}
	}
	
	public void createRiver() {
		connect();
		String sql1 = "select id from river0";
		String sql2 = "select s.id as sid, p.id as pid " +		// river - province
				"from river0 s, geo_river g, province0m p " +
				"where s.name = g.river and g.province = p.name";
		String sql3 = "select s.id as sid, c.id as cid " +		// river - country
					"from river0 s, geo_river g, country0np c " +
					"where s.name = g.river and g.country = c.code";
		String sql4 = "select r.id as rid, l.id as lid " +		// river - lake
				"from river0 r, lake0 l where r.lake = l.name";
		String sql5 = "select r.id as rid, s.id as sid " +		// river - sea
				"from river0 r, sea0 s where r.sea = s.name";
		String sql6 = "select l.id as lid, r.id as rid " +		// lake - river
				"from lake0 l, river0 r where l.river = r.name";
		try {
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				g.addVertex(rs.getInt("id"));
				vdos.writeInt(rs.getInt("id"));
			}
			rs.close(); pstmt.close();
		
			pstmt = conn.prepareStatement(sql2);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int sid = rs.getInt("sid"); 				
				int pid = rs.getInt("pid");
				g.addEdge(sid, pid);
				g.addEdge(pid, sid);
				edos.writeInt(sid);
				edos.writeInt(pid);
				edos.writeInt(pid);
				edos.writeInt(sid);
			}
			rs.close(); pstmt.close();
			
			pstmt = conn.prepareStatement(sql3);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int sid = rs.getInt("sid"); 				
				int cid = rs.getInt("cid");
				g.addEdge(sid, cid);
				g.addEdge(cid, sid);	
				edos.writeInt(sid);
				edos.writeInt(cid);
				edos.writeInt(cid);
				edos.writeInt(sid);
			}
			rs.close(); pstmt.close();
			
			pstmt = conn.prepareStatement(sql4);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int rid = rs.getInt("rid");
				int lid = rs.getInt("lid"); 				
				g.addEdge(rid, lid);
				edos.writeInt(rid);
				edos.writeInt(lid);
			}	
			rs.close(); pstmt.close();
			
			pstmt = conn.prepareStatement(sql5);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int rid = rs.getInt("rid");
				int sid = rs.getInt("sid"); 				
				g.addEdge(rid, sid);
				edos.writeInt(rid);
				edos.writeInt(sid);
			}
			rs.close(); pstmt.close();
			
			pstmt = conn.prepareStatement(sql6);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int lid = rs.getInt("lid"); 				
				int rid = rs.getInt("rid");
				g.addEdge(lid, rid);
				edos.writeInt(lid);
				edos.writeInt(rid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createDesert() {
		connect();
		String sql1 = "select id from desert0";
		String sql2 = "select s.id as sid, p.id as pid " +		// desert - province
				"from desert0 s, geo_desert g, province0m p " +
				"where s.name = g.desert and g.province = p.name ";
		String sql3 = "select s.id as sid, c.id as cid " +		// desert - country
					"from desert0 s, geo_desert g, country0np c " +
					"where s.name = g.desert and g.country = c.code";	
		
		try {
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				g.addVertex(rs.getInt("id"));
				vdos.writeInt(rs.getInt("id"));
			}
			rs.close(); pstmt.close();
		
			pstmt = conn.prepareStatement(sql2);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int sid = rs.getInt("sid"); 				
				int pid = rs.getInt("pid");
				g.addEdge(sid, pid);
				g.addEdge(pid, sid);	
				edos.writeInt(sid);
				edos.writeInt(pid);
				edos.writeInt(pid);
				edos.writeInt(sid);
			}
			rs.close(); pstmt.close();
			
			pstmt = conn.prepareStatement(sql3);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int sid = rs.getInt("sid"); 				
				int cid = rs.getInt("cid");
				g.addEdge(sid, cid);
				g.addEdge(cid, sid);
				edos.writeInt(sid);
				edos.writeInt(cid);
				edos.writeInt(cid);
				edos.writeInt(sid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createIsland() {
		connect();
		String sql1 = "select id from island0";
		String sql2 = "select s.id as sid, p.id as pid " +		// island - province
				"from island0 s, geo_island g, province0m p " +
				"where s.name = g.island and g.province = p.name ";
		String sql3 = "select s.id as sid, c.id as cid " +		// island - country
					"from island0 s, geo_island g, country0np c " +
					"where s.name = g.island and g.country = c.code";
		String sql4 = "select s.id as sid, c.id as cid " +		// island - city
				"from island0 s, locatedOn g, city0 c " +
				"where s.name = g.island and g.city = c.name";
		try {
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				g.addVertex(rs.getInt("id"));
				vdos.writeInt(rs.getInt("id"));
			}
			rs.close(); pstmt.close();
		
			pstmt = conn.prepareStatement(sql2);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int sid = rs.getInt("sid"); 				
				int pid = rs.getInt("pid");
				g.addEdge(sid, pid);
				g.addEdge(pid, sid);	
				edos.writeInt(sid);
				edos.writeInt(pid);
				edos.writeInt(pid);
				edos.writeInt(sid);
			}
			rs.close(); pstmt.close();
			
			pstmt = conn.prepareStatement(sql3);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int sid = rs.getInt("sid"); 				
				int cid = rs.getInt("cid");
				g.addEdge(sid, cid);
				g.addEdge(cid, sid);
				edos.writeInt(sid);
				edos.writeInt(cid);
				edos.writeInt(cid);
				edos.writeInt(sid);
			}
			rs.close(); pstmt.close();
			
			pstmt = conn.prepareStatement(sql4);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int sid = rs.getInt("sid"); 				
				int cid = rs.getInt("cid");
				g.addEdge(sid, cid);
				g.addEdge(cid, sid);
				edos.writeInt(sid);
				edos.writeInt(cid);
				edos.writeInt(cid);
				edos.writeInt(sid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createIsIslandInRelation() {	
		connect();
		String sql1 = "select i.id as iid, s.id as sid " +		// island - sea
					"from island0 i, islandIn ii, sea0 s " +
					"where i.name = ii.island and s.name = ii.sea";		
		try {
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int iid = rs.getInt("iid"); 				
				int sid = rs.getInt("sid");
				g.addEdge(iid, sid);
				g.addEdge(sid, iid);	
				edos.writeInt(iid);
				edos.writeInt(sid);
				edos.writeInt(sid);
				edos.writeInt(iid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createMountain() {
		connect();
		String sql1 = "select id from mountain0";
		String sql2 = "select s.id as sid, p.id as pid " +		// mountain - province
				"from mountain0 s, geo_mountain g, province0m p " +
				"where s.name = g.mountain and g.province = p.name ";
		String sql3 = "select s.id as sid, c.id as cid " +		// mountain - country
					"from mountain0 s, geo_mountain g, country0np c " +
					"where s.name = g.mountain and g.country = c.code";
		String sql4 = "select s.id as sid, i.id as iid " +		// mountain - island
				"from mountain0 s, mountainonisland g, island0 i " +
				"where s.name = g.mountain and g.island = i.name";
		try {
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				g.addVertex(rs.getInt("id"));
				vdos.writeInt(rs.getInt("id"));
			}
			rs.close(); pstmt.close();
		
			pstmt = conn.prepareStatement(sql2);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int sid = rs.getInt("sid"); 				
				int pid = rs.getInt("pid");
				g.addEdge(sid, pid);
				g.addEdge(pid, sid);	
				edos.writeInt(sid);
				edos.writeInt(pid);
				edos.writeInt(pid);
				edos.writeInt(sid);
			}
			rs.close(); pstmt.close();
			
			pstmt = conn.prepareStatement(sql3);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int sid = rs.getInt("sid"); 				
				int cid = rs.getInt("cid");
				g.addEdge(sid, cid);
				g.addEdge(cid, sid);
				edos.writeInt(sid);
				edos.writeInt(cid);
				edos.writeInt(cid);
				edos.writeInt(sid);
			}
			rs.close(); pstmt.close();
			
			pstmt = conn.prepareStatement(sql4);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int sid = rs.getInt("sid"); 				
				int iid = rs.getInt("iid");
				g.addEdge(sid, iid);
				g.addEdge(iid, sid);
				edos.writeInt(sid);
				edos.writeInt(iid);
				edos.writeInt(iid);
				edos.writeInt(sid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
	
	public void createLocatedRelation() {
		connect();
		String sql1 = "select c.id as cid, r.id as rid " +		// city - river
				"from city0 c, located l, river0 r " +
				"where l.river = r.name and " +
				"c.name = l.city and c.province = l.province and c.country = l.country";
		String sql2 = "select c.id as cid, r.id as rid " +		// city - lake
				"from city0 c, located l, lake0 r " +
				"where l.lake = r.name and " +
				"c.name = l.city and c.province = l.province and c.country = l.country";
		String sql3 = "select c.id as cid, r.id as rid " +		// city - sea
				"from city0 c, located l, sea0 r " +
				"where l.sea = r.name and " +
				"c.name = l.city and c.province = l.province and c.country = l.country";

		try {
			pstmt = conn.prepareStatement(sql1);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int cid = rs.getInt("cid"); 				
				int rid = rs.getInt("rid");
				g.addEdge(rid, cid);
				g.addEdge(cid, rid);	
				edos.writeInt(rid);
				edos.writeInt(cid);
				edos.writeInt(cid);
				edos.writeInt(rid);
			}
			rs.close(); pstmt.close();
			
			pstmt = conn.prepareStatement(sql2);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int cid = rs.getInt("cid"); 				
				int rid = rs.getInt("rid");
				g.addEdge(rid, cid);
				g.addEdge(cid, rid);	
				edos.writeInt(rid);
				edos.writeInt(cid);
				edos.writeInt(cid);
				edos.writeInt(rid);
			}
			rs.close(); pstmt.close();
			
			pstmt = conn.prepareStatement(sql3);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int cid = rs.getInt("cid"); 				
				int rid = rs.getInt("rid");
				g.addEdge(rid, cid);
				g.addEdge(cid, rid);	
				edos.writeInt(rid);
				edos.writeInt(cid);
				edos.writeInt(cid);
				edos.writeInt(rid);
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
	}
}