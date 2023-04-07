package memo.app;

import java.awt.event.*;
import java.sql.SQLException;
import java.util.*;

/**
 * @author min_y
 * Controller
 * 	View 쪽에서 이벤트가 발생하면 이벤트를 처리한다. ==> DB 관련 작업이 있으면
 * 	DAO 객체 통해 작업을 수행한다. ==> 그 처리 결과를 View에 전달한다.
 * 	Model과 View 사이에서 제어하는 역할을 수행한다.
 */
public class MemoHandler implements ActionListener {
	MemoAppView app;	// View
	MemoDAO dao = new MemoDAO();	// Model
	
	public MemoHandler(MemoAppView mav) {
		this.app = mav;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// System.out.println("event 처리 중...");
		// MemoAppView 타이틀에 "event 처리 중..." 출력하고자 한다면

		// app.setTitle("event 처리 중...");
		
		Object obj = e.getSource();
		if (obj == app.btAdd) {
			addMemo();
			app.clearInput();
			listMemo();	// 전체 목록 출력
		}
		else if(obj == app.btList) {
			listMemo();
		}
		else if(obj == app.btDel) {
			deleteMemo();
			listMemo();
		}
		else if(obj == app.btEdit) {	// 글 수정
			editMemo();
		}
		else if(obj == app.btEditEnd) {	// 글 수정 처리
			editMemoEnd();
			app.clearInput();
			listMemo();
		}
		else if(obj == app.btFind) {	// 글 내용 검색 - 키워드 검색
			findMemo();
			
		}
	}	//-----------------------------
	
	
	public void findMemo() {
		String keyword = app.showInputDiaolog("검색할 키워드를 입력하세요.");
		if(keyword==null) return;
		
		if(keyword.trim().isEmpty()) {
			findMemo();	// 재귀 호출
			return;
		}
		try {
			List<MemoVO> arr = dao.findMemo(keyword);
			if(arr==null || arr.size()==0) {
				app.showMessage(keyword+"로 검색한 결과, 해당 글은 없습니다.");
				return;
			}
			app.showTextArea(arr);
		}
		catch(SQLException e) {
			e.printStackTrace();
			app.showMessage(e.getMessage());
		}
		
	}
	
	
	public void editMemo() {
		// 글 번호로 해당 글 내용 가져오기 => SELECT문 WHERE절 (PK) ==> 결과는 단일행 레코드
		String noStr = app.showInputDiaolog("수정할 글 번호를 입력하세요.");
		if(noStr==null) return;
		
		try {
			MemoVO vo = dao.selectMemo(Integer.parseInt(noStr.trim()));
			if(vo == null) {
				app.showMessage(noStr+"번 글은 존재하지 않아요!");
				return;
			}
			app.setText(vo);
			
		}
		catch(SQLException e) {
			app.showMessage(e.getMessage());
		}
		
	}	//-----------------------------
	
	
	public void editMemoEnd() {
		// 1. 사용자가 입력한 값 받아오기 (no, name, msg)
		String noStr = app.tfNo.getText();
		String name = app.tfName.getText();
		String msg = app.tfMsg.getText();
		
		// 2. 유효성 체크
		if(noStr==null || name==null || msg==null || noStr.trim().isEmpty() || name.trim().isEmpty()) {
			app.showMessage("글번호와 작성자, 내용을 입력하세요.");
			app.tfName.requestFocus();
			return;
		}
		
		// 3. 1번에서 얻은 값들을 MemoVO 객체에 담아주기
		int no = Integer.parseInt(noStr.trim());
		MemoVO memo = new MemoVO(no, name, msg, null);
		
		try {
			// 4. dao의 updateMemo() 호출하기
			int res = dao.updateMemo(memo);
			
			// 5. 그 결과 메시지 처리
			String str = (res>0)?"글 수정 성공":"글 수정 실패";
			app.showMessage(str);
		}
		catch(SQLException e) {
			e.printStackTrace();
			app.showMessage(e.getMessage());
		}
		
		
	}	//-----------------------------------
	
	
	public void deleteMemo() {
		String noStr = app.showInputDiaolog("삭제할 글 번호를 입력하세요.");
		if (noStr==null) return;
		
		try {
			int n = dao.deleteMemo(Integer.parseInt(noStr.trim()));
			String res = (n>0)?"글 삭제 성공":"글 삭제 실패";
			app.showMessage(res);
		}
		catch(SQLException e) {
			app.showMessage(e.getMessage());
		}
	}	//------------------------------------------
	
	
	public void listMemo() {
		try {
			List<MemoVO> arr = dao.listMemo();
			app.setTitle("전체 글 개수: "+arr.size()+"개");
			// 모델(dao)를 통해서 받아온 데이터를 화면단(app)에게 넘겨준다.
			app.showTextArea(arr);
		}
		catch(SQLException e) {
			app.showMessage(e.getMessage());
		}
	}	//--------------------------------------------
	
	
	public void addMemo() {
		// 1) app의 tfName, tfMsg에 입력한 값 얻어오기
		String name = app.tfName.getText();
		String msg = app.tfMsg.getText();
		
		// 2) 유효성 체크 (null, 빈 문자열 체크)
		if (name == null || msg == null || name.trim().isEmpty()) {
			app.showMessage("작성자와 메모 내용을 입력하세요.");
			app.tfName.requestFocus();
			return;
		}
		
		// 3) 1번에서 받은 값을 MemoVO 객체에 담아준다.
		MemoVO memo = new MemoVO(0, name, msg, null);
		
		// 4) DAO의 insertMemo()를 호출한다.
		try {
			int result = dao.insertMemo(memo);
			
			// 5) 그 결과값에 따라 메시지 처리
			if(result > 0) {
				app.setTitle("글 등록 성공");
			}
			else {
				app.showMessage("글 등록 실패");
			}
		}
		catch(SQLException e) {
			app.showMessage(e.getMessage());
		}
		
		
	}
	
}
