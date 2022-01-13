package kr.or.ddit.login.mapper;

import egovframework.rte.psl.dataaccess.mapper.Mapper;
import kr.or.ddit.login.vo.LoginVO;

@Mapper("loginMapper")

public interface LoginMapper {

   LoginVO login(LoginVO loginVo);

}
