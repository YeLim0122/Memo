package memo.app;

import java.sql.*;
import common.util.*;
import java.util.*;

/**
 * @author min_y
 *
 * 영속성 계층(Persistence Layer)에 속함
 * DAO(Data Access Object): 데이터베이스에 접근해서 CRUD의 로직을 수행하는 객체
 *	==> Model에 속함
 */
public class MemoDAO {
	private Connection con;
	private PreparedStatement ps;
	private ResultSet rs;
	
	/**
	 * 한 줄 메모장에 insert문을 수행하는 메소드 (C)
	 */
	public int insertMemo(MemoVO memo) throws SQLException {
		try {
			con = DBUtil.getCon();
			// String: 불변성(immutable) : 원본은 불변함. += 할 때마다 새로운 객체 생성..
			/* String sql="INSERT INTO ";
					sql+="memo VALUES(";
					sql+=")"; */
			// StringBuffer, StringBuilder: 문자열 편집작업이 가능한 클래스
			//								문자열을 메모리 버퍼에 넣고 수정, 삽입, 삭제 등을 수행함.
			
			StringBuilder buf = new StringBuilder("INSERT INTO memo(no, name, msg, wdate)")
							.append(" VALUES(memo_seq.nextval, ?, ?, sysdate)");
			
			String sql = buf.toString();
			
			ps = con.prepareStatement(sql);
			ps.setString(1, memo.getName());
			ps.setString(2, memo.getMsg());
			
			int n = ps.executeUpdate();
			return n;
			
		}
		finally {
			// DB 연결자원 반납
			close();
		}
	}
	
	/**
	 * 전체 메모글을 가져오는 메소드
	 */
	public List<MemoVO> listMemo() throws SQLException {
		try {
			con = DBUtil.getCon();
			StringBuilder buf = new StringBuilder("SELECT RPAD(no,7,' ') no, RPAD(name,16,' ') name,")
									.append("RPAD(msg,105,' ') msg, wdate FROM memo ORDER BY wdate DESC");
			String sql = buf.toString();
			
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			
			List<MemoVO> arr = makeList(rs);
			return arr;
		}
		finally {
			close();
		}	// close 매우 중요!!!! 반드시 실행되도록 finally에 넣는 것이 좋음.
	}
	
	
	public List<MemoVO> makeList(ResultSet rs) throws SQLException {
		List<MemoVO> arr = new ArrayList<>();
		while(rs.next()) {
			int no = rs.getInt("no");
			String name = rs.getString("name");
			String msg = rs.getString("msg");
			java.sql.Date wdate = rs.getDate("wdate");
			
			MemoVO memo = new MemoVO(no, name, msg, wdate);
			arr.add(memo);	// table
		}	// while-----------------
		return arr;
	}
	
	
	/**
	 * 글 번호(PK)로 메모글을 가져오는 메소드
	 */
	public MemoVO selectMemo(int no) throws SQLException {
		try {
			con = DBUtil.getCon();
			
			String sql = "SELECT no, name, msg, wdate FROM memo WHERE no=?";
			
			ps = con.prepareStatement(sql);
			ps.setInt(1, no);
			
			rs = ps.executeQuery();
			
			List<MemoVO> arr = makeList(rs);
			if(arr != null && arr.size() == 1) {
				MemoVO memo = arr.get(0);
				return memo;	// 해당 글 반환
			}
			return null;	// 해당 글이 없을 경우
		}
		finally {
			close();
		}
	}	//------------------------------------
	
	
	/**
	 * keyword로 메모 글 내용을 검색하는 메소드
	 */
	public List<MemoVO> findMemo(String keyword) throws SQLException {
		try {
			con = DBUtil.getCon();
			
			StringBuilder buf = new StringBuilder("SELECT RPAD(no,7,' ') no,")
						.append(" RPAD(name,16,' ') name, RPAD(msg,105,' ') msg, wdate FROM memo")
						.append(" WHERE msg LIKE ?");
			String sql = buf.toString();
			
			ps = con.prepareStatement(sql);
			ps.setString(1, "%"+keyword+"%");
			
			rs = ps.executeQuery();
			List<MemoVO> arr = makeList(rs);
			return arr;
			
		}
		finally {
			close();
		}
		
	}	//--------------------------------------
	
	
	/**
	 * 메모 글 내용, 작성자를 수정하는 메소드
	 */
	public int updateMemo(MemoVO vo) throws SQLException {
		try {
			con = DBUtil.getCon();
			
			String sql = "UPDATE memo SET name=?, msg=? WHERE no=?";
			
			ps = con.prepareStatement(sql);
			ps.setString(1, vo.getName());
			ps.setString(2, vo.getMsg());
			ps.setInt(3, vo.getNo());
			
			return ps.executeUpdate();
		}
		finally {
			close();
		}
	}	//-------------------------------------
	
	
	/**
	 * 글 번호로 메모글을 삭제하는 메소드
	 */
	public int deleteMemo(int no) throws SQLException {
		try {
			con = DBUtil.getCon();
			String sql = "DELETE FROM memo WHERE no=?";
			
			ps=con.prepareStatement(sql);
			ps.setInt(1, no);
			return ps.executeUpdate();
			
		} finally {
			close();
		}
	}
	
	
	/**
	 * DB 관련한 자원들을 반납하는 메소드
	 */
	public void close() {
		try {
			if (rs!=null) rs.close();
			if (ps!=null) ps.close();
			if (con!=null) con.close();
			
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
	}

	
}
