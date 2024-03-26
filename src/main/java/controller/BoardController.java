package controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.beanutils.BeanUtils;

import DAO.BoardDAO;
import DTO.Board;

/**
 * Servlet implementation class BoardController
 */

//웹 어플리케이션에서 발생하는 모든 request는 전부 BoardController로 온다.
@WebServlet("/")
@MultipartConfig(maxFileSize=1024*1024*2, location="c:/Temp/img")
public class BoardController extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private BoardDAO dao;
    private ServletContext ctx;
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		//init은 서블릿 객체 생성시 딱 한번만 실행되므로 객체를 한번만 생성해 공유한다.
		dao = new BoardDAO();
		ctx = getServletContext(); //ServletContext: 웹 어플리케이션의 자원관리
	}
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8"); //request 객체 한글깨짐 방지
		
		String command = req.getServletPath();
		String site = null;
		System.out.println("command: "+ command);
		
		switch (command) {
		case "/index": site =getList(req); break; //index.jsp로 이동
		case "/view": site = getView(req); break; //view.jsp로 이동
		case "/write": site= "write.jsp"; break; //write.jsp로 이동
		case "/insert": site= insertBoard(req); break;
		case "/edit": site= getViewForEdit(req); break; //edit.jsp로 이동
		case "/update": site = updateBoard(req); break; //게시글 수정
		case "/delete": site = deleteBoard(req); break;
		
	}
		//redirect:/list
		/*
		 -공통점: 둘 다 페이지를 이동한다.
		 redirect: 객체(req,resp)를 가지고 이동하지 X ,URL의 변화 
		  	*DB에 변화가 생기는 요청에 사용(insert,update,delete)
		  	-글쓰기, 글수정, 글삭제, 회원가입..
		 forward:객체(req,resp)를 가지고 이동 ,URL의 변화 X
		 *단순조회에 사용(select)
		 *-게시글 목록보기,게시글 상세페이지, 검색..
		 */
		
		if(site.startsWith("redirect:/")) {
			String rview = site.substring("redirect:/".length()); //index: 10
			resp.sendRedirect(rview); //rview: /index
		} else {
			 ctx.getRequestDispatcher("/"+site).forward(req, resp);
		}
   

	}
	
	//BoardDao객체의 getList 메소드 실행: 게시물 전체 목록을 가져온 후 request 객체에 넣어준다.
	public String getList(HttpServletRequest req) {
		List<Board> list;
		try {
			list = dao.getList();
			req.setAttribute("boardList",list);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return "index.jsp";
	}
	public String getView(HttpServletRequest req) {
		int board_no = Integer.parseInt(req.getParameter("board_no"));
		try {
			dao.updateViews(board_no);
			Board b = dao.getView(board_no);
			req.setAttribute("board", b);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "view.jsp";
	}
	
	//수정할 게시물의 기존 데이터를 가지고 와서 request 객체에 넣어준다.
	public String getViewForEdit(HttpServletRequest req) {
		int board_no = Integer.parseInt(req.getParameter("board_no"));
		try {
			Board b = dao.getView(board_no);
			req.setAttribute("board", b);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "edit.jsp";
	}
	
	//게시글을 등록해준다.
		public String  insertBoard(HttpServletRequest req) {
			Board b = new Board();
			try {
				BeanUtils.populate(b, req.getParameterMap());
				
				//1.이미지 파일 서버(c:/Temp/img)에 저장
				Part part =req.getPart("file"); //파일에 대한 정보
				String fileName= getFileName(part); //파일명 얻음
				//fileName이 null이 아니고 length()도 0이 아닌지
				//업로드도 파일이 있는지 확인
				if(fileName != null && !fileName.isEmpty()){
					part.write(fileName); //서버에 파일 업로드
				}
				//2.이미지 파일 이름을 Board객체에 저장
				b.setImg("/img/"+fileName);
				dao.insertBoard(b);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			return "redirect:/index";
		}
		
		//게시글을 수정해준다.
		public String updateBoard(HttpServletRequest req) {
			Board b = new Board();
			try {
				BeanUtils.populate(b, req.getParameterMap());
				//1.이미지 파일 서버(c:/Temp/img)에 저장
				Part part =req.getPart("file"); //파일에 대한 정보
				String fileName= getFileName(part); //파일명 얻음
				//fileName이 null이 아니고 length()도 0이 아닌지
				//업로드도 파일이 있는지 확인
				if(fileName != null && !fileName.isEmpty()){
					part.write(fileName); //서버에 파일 업로드
					//2.이미지 파일 이름을 Board객체에 저장
					b.setImg("/img/"+fileName);
				} else {
					b.setImg(null); //업로드한 이미지가 없을 경우 빈문자열 저장
				}
						
					dao.updateBoard(b);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "redirect:/view?board_no="+b.getBoard_no();
		}
		//게시글을 삭제해준다
		public String deleteBoard(HttpServletRequest req) {
			int board_no = Integer.parseInt(req.getParameter("board_no"));
			try {
				dao.deleteBoard(board_no);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return "redirect:/index";
		}
		//파일에서 이미지명을 추출하는 메소드
		private String getFileName(Part part) {
			String fileName = null;
			//파일이름이 들어있는 헤더 영역을 가지고옴
			String header = part.getHeader("content-disposition");
			
			//form-data; name="img"; filename="사진5.jpg"
			System.out.println("Header=> "+ header);
			
			//파일 이름이 들어있는 속성부분의 시작위치(인덱스 번호)를 가지고옴
			int start = header.indexOf("filename=");
			//쌍따옴표 사이의 이미지명 부분만 가지고 옴
			fileName =header.substring(start+10,header.length()-1);
			
			return fileName;
		}
}
