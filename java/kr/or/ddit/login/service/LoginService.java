package kr.or.ddit.login.service;

import kr.or.ddit.login.vo.LoginVO;

public interface LoginService {

//   정상로그인
   public LoginVO login(LoginVO loginVo) throws Exception;

}