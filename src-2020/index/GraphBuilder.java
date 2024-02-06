package index;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import my.jgrapht.algorithm.BellmanFordShortestPathMod;
//import my.jgrapht.algorithm.FloydWarshallShortestPaths;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import util.Params;

import java.io.LineNumberReader;
import java.io.FileReader;
// 클래스 선언
public class GraphBuilder {
	
	// 데이터베이스 연결관련 변수 선언
	protected static Connection conn = null;
	protected PreparedStatement pstmt = null;
	protected ResultSet rs = null;
	
	protected File vFile = new File(Params.ExpDB + "/graph/vertexData.bin");
	protected File eFile = new File(Params.ExpDB + "/graph/edgeData.bin");
//	protected File nnrFile = new File(Params.ExpDB + "/graph/NNRdata.txt");
	protected FileOutputStream vfos, efos;
	protected DataOutputStream vdos, edos;
			
	protected DirectedGraph<Integer, DefaultEdge> g = null;		// graph to build
			
	public GraphBuilder() { 
		g = new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);

		try {
			vfos = new FileOutputStream(vFile);
			vdos = new DataOutputStream(vfos);			
			efos = new FileOutputStream(eFile);
			edos = new DataOutputStream(efos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getVertexSetSize() { 
		return g.vertexSet().size();
	}
	
	public int getEdgeSetSize() { 
		return g.edgeSet().size();
	}
	
	public void close() {
		try {
			vdos.close();
			vfos.close();
			edos.close();
			efos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// 데이터베이스 연결 메서드
	protected void connect() {
		// JDBC 드라이버 로드
		try {
			Class.forName(Params.jdbc_driver);
			// 데이터베이스 연결정보를 이용해 Connection 인스턴스 확보
			conn = DriverManager.getConnection(Params.jdbc_url, Params.mysql_user, Params.mysql_pw);
			//conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mondial?user=pittsburgh&password=hgrubsttip");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// 데이터베이스 연결 종료 메서드
	protected void disconnect() {
		if(rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(pstmt != null) {
			try {
				pstmt.close();
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
	
/*	public boolean computeNodeNodeRel(Graph<Integer,DefaultEdge> g) { // , int s, int e, float r) {
		connect();
		
		final String sql ="insert into NNR values(?,?,?)";	// startNode, endNode, rel
		
		Set<Integer> uSet = g.vertexSet();
		Iterator<Integer> uI = uSet.iterator();
//		FloydWarshallShortestPaths<Integer,DefaultEdge> sp 
//			= new FloydWarshallShortestPaths<Integer,DefaultEdge>(g);	

		BellmanFordShortestPathMod<Integer,DefaultEdge> sp = null;
		try {
			pstmt = conn.prepareStatement(sql);		
			while (uI.hasNext()) {
				int u = uI.next();
				pstmt.setInt(1, u);		
				
				sp = new BellmanFordShortestPathMod<Integer,DefaultEdge>(g, u, 3);
				sp.calculateShortestPaths();
				Set<Integer> vSet =  sp.getEndVertices();
				Iterator<Integer> vI = vSet.iterator();
				System.out.println("u="+u+" numOfEndVertices="+vSet.size()); 

				while (vI.hasNext()) {
					int v = vI.next();	
					if (v == u) continue;
//					System.out.println("	v="+v+", length="+sp.getPathEdgeList(v).size());
//					List<DefaultEdge> el = sp.getPathEdgeList(v);
//					if (el != null) {
						float r = (float)1.0 / sp.getPathEdgeListMod(v).size();
						pstmt.setInt(2, v);
						pstmt.setFloat(3, r);
						pstmt.executeUpdate();
//					}
				}
				sp = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		finally {
			disconnect();
		}
		return true;*/
/*
		try {
			pstmt = conn.prepareStatement(sql);		
			while (uI.hasNext()) {
				int u = uI.next();
				pstmt.setInt(1, u);		
				System.out.println("u="+u);
				System.out.println(sp.shortestDistance(u, 2000));

				List<GraphPath<Integer,DefaultEdge>> pL = sp.getShortestPaths(u);
				System.out.println("1");

				Iterator<GraphPath<Integer,DefaultEdge>> pI = pL.iterator();
				System.out.println("2");
				int i = 0;
				while (pI.hasNext()) {
					GraphPath<Integer,DefaultEdge> gp = pI.next();
					System.out.println(i++);

					int v = gp.getEndVertex();
					float r = (float)1.0 / gp.getEdgeList().size();
					pstmt.setInt(2, v);
					pstmt.setFloat(3, r);
					pstmt.executeUpdate();
				}
*/			
//	}

/*	public boolean computeNodeNodeRel(Graph<Integer,DefaultEdge> g) { // , int s, int e, float r) {
		connect();
	
		FileOutputStream fos = null;
		ByteBuffer wbuffer = ByteBuffer.allocateDirect(Params.FILE_BLOCK_SIZE);	// buffer for building indexes

		FileChannel fc = null;
		int SIZE_OF_ENTRY = (Integer.SIZE*2+Float.SIZE)/8;
		int NUM_OF_ENTRIES_IN_BLOCK = Params.FILE_BLOCK_SIZE / SIZE_OF_ENTRY;	
		int BUFFER_LIMIT = NUM_OF_ENTRIES_IN_BLOCK * SIZE_OF_ENTRY;
		try {
			fos = new FileOutputStream("NNRdata.txt");
			fc = fos.getChannel();
			wbuffer.clear(); wbuffer.limit(BUFFER_LIMIT);			
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		Set<Integer> uSet = g.vertexSet();
		Iterator<Integer> uI = uSet.iterator();

		BellmanFordShortestPathMod<Integer,DefaultEdge> sp = null;
		try {
			while (uI.hasNext()) {
				int u = uI.next();
				
				sp = new BellmanFordShortestPathMod<Integer,DefaultEdge>(g, u, 3);
				sp.calculateShortestPaths();
				Set<Integer> vSet =  sp.getEndVertices();
				Iterator<Integer> vI = vSet.iterator();
				System.out.println("u="+u+" numOfEndVertices="+vSet.size()); 

				while (vI.hasNext()) {
					int v = vI.next();	
					if (v == u) continue;
					float r = (float)1.0 / sp.getPathEdgeListMod(v).size();
					
					if (wbuffer.hasRemaining() == false) {
						wbuffer.flip();
						fc.write(wbuffer);
						wbuffer.rewind();
					}
					wbuffer.putInt(u);
					wbuffer.putInt(v);
					wbuffer.putFloat(r);					
				}
				sp = null;
			}
			
			if (wbuffer.position() > 0) {
				wbuffer.flip();
				fc.write(wbuffer);
				wbuffer.clear();
			}
			fc.close();
			fos.close();			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		finally {
			disconnect();
		}
		return true;
	}*/

	public boolean computeNodeNodeRel() { // , int s, int e, int f, float r) {
		// compute and save (n,n,r) triples into a text file, which will be used to be loaded into MySQL DB using LOAD DATAFILE command 
		
//		connect();
		PrintWriter w = null; 
		try {
			w = new PrintWriter(new FileOutputStream(Params.nmrFile, true)); 	// append!!!
		} catch (IOException e) {
			e.printStackTrace();
		}
		Set<Integer> uSet = g.vertexSet();
		Iterator<Integer> uI = uSet.iterator();
		BellmanFordShortestPathMod<Integer,DefaultEdge> sp = null;

//		int i = 0;
		
		try {

			while (uI.next()<2029264) ; 
/*
            {
                int u = 1668713;

				w.printf("%d,%d,%d,%f\n", u, u, u, (float)1.0);	// first, add a self-relation!!!
				
				sp = new BellmanFordShortestPathMod<Integer,DefaultEdge>(g, u, 5);
				sp.setStartVertex(u);
				sp.reset();
				sp.calculateShortestPaths();
				Set<Integer> vSet = sp.getEndVertices();
				Iterator<Integer> vI = vSet.iterator();
//				System.out.println("u="+u+" numOfEndVertices="+vSet.size()); 
				if (u % 100000 == 0)
					System.out.println("u="+u+" numOfEndVertices="+vSet.size()); 
				while (vI.hasNext()) {
					int v = vI.next();	
					if (v == u) continue;
					List<DefaultEdge> l = sp.getPathEdgeListMod(v);
					//int f = (Integer)(l.get(0).getTarget());
					int f = (Integer)(l.get(l.size()-1).getSource()); // f->v
					float r = (float)(1+Math.log10((double)1.0 / (l.size() + 1)));	// !!!					
					w.printf("%d,%d,%d,%f\n", u, v, f, r);
				}
            }
*/
			while (uI.hasNext()) {

				int u = uI.next();

				sp = new BellmanFordShortestPathMod<Integer,DefaultEdge>(g, u, 5);
				sp.setStartVertex(u);
				sp.reset();
				sp.calculateShortestPaths();
				Set<Integer> vSet = sp.getEndVertices();
				Iterator<Integer> vI = vSet.iterator();
//				System.out.println("u="+u+" numOfEndVertices="+vSet.size()); 
				if (u % 100000 == 0)
					System.out.println("u="+u+" numOfEndVertices="+vSet.size()); 
				while (vI.hasNext()) {
					int v = vI.next();	
					if (v == u) {
						w.printf("%d,%d,%d,%f\n", u, u, u, (float)1.0);	// first, add a self-relation!!!
						continue;
					}
					List<DefaultEdge> l = sp.getPathEdgeListMod(v);
//					int f = (Integer)(l.get(0).getTarget());
// for jmdb NMR(reverse edge graph)
					int f = (Integer)(l.get(l.size()-1).getSource()); // f->v
					// float r = (float)1.0 / (l.size() + 1);	// !!!		
					float r = (float)(1+Math.log10((double)1.0 / (l.size() + 1)));	// !!!					
					w.printf("%d,%d,%d,%f\n", u, v, f, r);
//					System.out.printf("%d,%d,%d,%f", u, v, f, r);
				}
//				sp = null;
			}
			w.close();			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		finally {
//			disconnect();
		}
		return true;
    }


	public void countLines() { 
        LineNumberReader lnr=null;
        try {
            lnr = new LineNumberReader(new FileReader(new File("../NNR.txt")));
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            lnr.skip(Long.MAX_VALUE);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(lnr.getLineNumber() + 1); //Add 1 because line index starts at 0
        // Finally, the LineNumberReader object should be closed to prevent resource leak
        try {
            lnr.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

