package kr.or.ddit.localFileSearch.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.or.ddit.localFileSearch.mapper.LocalFileMapper;
import kr.or.ddit.localFileSearch.service.LocalFileService;

@Service
public class LocalFileServiceImpl implements LocalFileService{

	@Autowired
	LocalFileMapper localFileMapper;
	
	
	@Override
	public String topPath() {
		return this.localFileMapper.topPath();	
	}
}
