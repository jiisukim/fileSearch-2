package kr.or.ddit.login.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.or.ddit.login.mapper.LoginMapper;
import kr.or.ddit.login.service.LoginService;
import kr.or.ddit.login.vo.LoginVO;

@Service("loginService")
public class LoginServiceImpl implements LoginService {

   @Autowired
   private LoginMapper loginMapper;

   // 정상로그인
   @Override
   public LoginVO login(LoginVO loginVo) throws Exception {

      LoginVO checkLoginVo = loginMapper.login(loginVo);
      return checkLoginVo;
   }
   
}