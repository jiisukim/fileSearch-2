package kr.or.ddit.login.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import kr.or.ddit.login.service.LoginService;
import kr.or.ddit.login.vo.LoginVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class LoginController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired 
	private LoginService loginService;

	@RequestMapping(value = "/login/login", method = RequestMethod.GET)
	public String login() {
		System.out.println("1");
		return "login/login";
	}
	
	@RequestMapping(value = "/login/login", method = RequestMethod.POST)
	   public String loginPost(@ModelAttribute LoginVO loginVo, HttpServletRequest request)throws Exception{
	      String inputId = loginVo.getId();
	      logger.info("입력받은 아이디 >> : " + inputId);
	      
	      String inputPw = loginVo.getPw();
	      logger.info("입력받은 아이디 >> : " + inputPw);
	      
	      // 비밀번호 검증 시작
	      
	      LoginVO checkLoginVo = loginService.login(loginVo);
	      
	      if(checkLoginVo == null) { //사용이 불가한 id or 비밀번호 오류
	         request.setAttribute("msg", "fail");
	         return "login/login";   
	      }
	      
	      HttpSession session = request.getSession();
	      session.setAttribute("loginVo", checkLoginVo);
	      
	      session.setAttribute("adminVo", loginVo.getId());
	      session.setAttribute("accessRight", checkLoginVo.getAccessRight());
	      logger.info("adminVo" + loginVo.getId());
	      logger.info("accessRight" + checkLoginVo.getAccessRight());
	      logger.info("sessionVO" + session.getAttribute("adminVo"));
	      
	      return "redirect:/file/";
	   }
	
	// 로그아웃
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public ModelAndView logout(HttpSession session) {

		session.invalidate();
		ModelAndView mav = new ModelAndView("redirect:/login/login");

		return mav;
	}

}
