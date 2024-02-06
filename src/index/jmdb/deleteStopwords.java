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

// Ŭ���� ����
public class deleteStopwords {
static final String termListFile = Params.ExpDB + "/termlist.bin";
	
	// �����ͺ��̽� ������� ���� ����
	static Connection conn = null;
	static Statement stmt = null;
	static PreparedStatement pstmt = null;
	static ResultSet rs = null;
	
	public deleteStopwords() { }
	
	// �����ͺ��̽� ���� �޼���
	static void connect() {
		// JDBC ����̹� �ε�
		try {
			Class.forName(Params.jdbc_driver);
			// �����ͺ��̽� ���������� �̿��� Connection �ν��Ͻ� Ȯ��
			conn = DriverManager.getConnection(Params.jdbc_url, Params.mysql_user, Params.mysql_pw);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// �����ͺ��̽� ���� ���� �޼���
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