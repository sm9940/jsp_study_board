package DAO;



import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


import DTO.Board;

public class BoardDAO {
	final static String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
	final static String JDBC_URL = "jdbc:oracle:thin:@localhost:1521:xe";
	
	//데이터 베이스 연결 메소드
	public Connection open(){
		Connection conn = null;
		try {
			Class.forName(JDBC_DRIVER);
			conn= DriverManager.getConnection(JDBC_URL,"test","test1234");
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return conn;
	}
	
	//게시판 리스트 가져오는 메소드
	public ArrayList<Board> getList() throws Exception{
		Connection conn = open(); //DB 커넥션 열기
		ArrayList<Board> boardList = new ArrayList<>();
		
		String sql = "SELECT BOARD_NO ,TITLE ,USER_ID , TO_CHAR(REG_DATE,'yyyy.mm.dd') reg_date,views FROM BOARD"; //쿼리문
		PreparedStatement pstmt = conn.prepareStatement(sql); //쿼리문 등록
		ResultSet rs = pstmt.executeQuery(); //쿼리문 실행 ->데이터베이스 결과 저장
		
		/*
		 try{} catch(Exception e){} finally{
		 conn.close(); pstmt.close(); rs.close();} 
		  */
		//Exception에서 사용하는 리소스 자동 닫기(try-with-resource)
		try(conn; pstmt; rs){
			while(rs.next()) {
				Board b = new Board();
				b.setBoard_no(rs.getInt("board_no"));
				b.setTitle(rs.getString("title"));
				b.setUser_id(rs.getString("user_id"));
				b.setReg_date(rs.getString("reg_date"));
				b.setViews(rs.getInt("views"));
				
				boardList.add(b);
			}
		}
		return boardList;
	}
 
	//게시판 내용 가져오는 메소드
	public Board getView(int board_no) throws Exception{
		Connection conn = open();
		Board b =new Board();
		
		String sql = "SELECT BOARD_NO ,TITLE ,USER_ID ,TO_CHAR(REG_DATE,'yyyy.mm.dd') reg_date, views,content,img  FROM BOARD WHERE BOARD_NO = ? order by board_no";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, board_no); //물음표에 들어갈 값을 반드시 먼저 지정
		ResultSet rs = pstmt.executeQuery();
		
		try(conn; pstmt; rs){
			while(rs.next()) {
				
				b.setBoard_no(rs.getInt("board_no"));
				b.setTitle(rs.getString("title"));
				b.setUser_id(rs.getString("user_id"));
				b.setReg_date(rs.getString("reg_date"));
				b.setViews(rs.getInt("views"));
				b.setContent(rs.getString("content"));
				b.setImg(rs.getString("img"));
				
			}
		}
		return b;
	}
	//조회수 증가 메소드
	public void updateViews(int board_no) throws Exception {
		Connection conn = open();
		String sql ="UPDATE BOARD SET VIEWS =(VIEWS+1) WHERE BOARD_NO =?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, board_no);
		try(conn; pstmt){
			pstmt.executeUpdate(); //insert, update, delete 문에서 사용
		}
	}
	//게시판 글등록 메소드
	public void insertBoard(Board b) throws Exception{
		Connection conn = open();
		String sql = "insert into board values(board_seq.nextval, ?, ?, ?, sysdate, 0, ?)";
		PreparedStatement pstmt = conn.prepareStatement(sql);
	
		try(conn; pstmt){
			pstmt.setString(1, b.getUser_id());
			pstmt.setString(2, b.getTitle());
			pstmt.setString(3, b.getContent());
			pstmt.setString(4,b.getImg());
			pstmt.executeUpdate();
		}
	}
	public void updateBoard(Board b) throws Exception{
		Connection conn = open();
		String sql = "UPDATE BOARD SET title= ? ,CONTENT =? , IMG =? WHERE BOARD_NO =?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
	
		try(conn; pstmt){
			
			pstmt.setString(1, b.getTitle());
			pstmt.setString(2, b.getContent());
			pstmt.setString(3,b.getImg());
			pstmt.setInt(4, b.getBoard_no());
			pstmt.executeUpdate();
			
			if(pstmt.executeUpdate() !=1) {
				throw new Exception("수정된 글이 없습니다.");
			}
		}
	}
	public void deleteBoard(int board_no) throws Exception{
		Connection conn = open();
		String sql = "delete from board where board_no =?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		try (conn; pstmt){
			pstmt.setInt(1, board_no);
			if(pstmt.executeUpdate() != 1) {
				throw new Exception("삭제된 글이 없습니다.");
			}
		}
	}
	
}
	
