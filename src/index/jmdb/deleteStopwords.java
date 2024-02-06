package index.jmdb;

import util.*;

import index.IndexBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

//import my.jgrapht.algorithm.RelSource;
import nkmap.bdb.KeywordNode;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.ParallelReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import com.sleepycat.je.Database;

// 클래스 선언
public class deleteStopwords {
static final String termListFile = Params.ExpDB + "/termlist.bin";
	
	// 데이터베이스 연결관련 변수 선언
	static Connection conn = null;
	static Statement stmt = null;
	static PreparedStatement pstmt = null;
	static ResultSet rs = null;
	
	public deleteStopwords() { }
	
	// 데이터베이스 연결 메서드
	static void connect() {
		// JDBC 드라이버 로드
		try {
			Class.forName(Params.jdbc_driver);
			// 데이터베이스 연결정보를 이용해 Connection 인스턴스 확보
			conn = DriverManager.getConnection(Params.jdbc_url, Params.mysql_user, Params.mysql_pw);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// 데이터베이스 연결 종료 메서드
	static void disconnect() {
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
		if(stmt != null) {
			try {
				stmt.close();
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
	
	public static void main(String[] args) throws SQLException {
		Params.setExpDB("jmdb");
		connect();
		
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(Params.ExpDB + "/stopwordsma.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		stmt = conn.createStatement();
//		pstmt = conn.prepareStatement("delete from kns where keyword=?");
		pstmt = conn.prepareStatement("delete from nk_map where keyword=?");

//		stmt.executeUpdate("drop index ind__endNode on NNR(endNode)"); 
//		System.out.println("index on NNR(endNode) was constructed.");
		while (true) {			
			String keyword=null;
			try {
				keyword = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (keyword == null) break;
			System.out.println(keyword);
			pstmt.setString(1, keyword);
			pstmt.executeUpdate();			
		}
//		conn.commit();
		System.out.println("kns processed");
		disconnect();

	}
}