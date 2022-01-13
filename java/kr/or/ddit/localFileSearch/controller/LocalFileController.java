package kr.or.ddit.localFileSearch.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import kr.or.ddit.localFileSearch.service.LocalFileService;
import kr.or.ddit.localFileSearch.vo.FileInfoVO;
import kr.or.ddit.localFileSearch.vo.FolderInfoVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping(value="/file")
@Controller
public class LocalFileController {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	LocalFileService localFileService;

	private final String ROOTPATH = "C:\\";		//dispatherServlet과 연결되는 패스
	
	@RequestMapping("/dragTest")
	public String dragTest() {
		
		return "file/dragTest";
		
	}
	
	@RequestMapping("")	// /file/search
	public String fileSearchTest(Model model) throws IOException {
		String topLevelPath = this.localFileService.topPath();	//디비에 있는 최상위 폴더 경로 가져옴
		String DATA_DIRECTORY = topLevelPath; 
		model.addAttribute("topLevelPath",topLevelPath);
		
		if(DATA_DIRECTORY.equals("C:")) {
			DATA_DIRECTORY += "\\";
		}
		
		File dir = new File(DATA_DIRECTORY); 
		System.out.println("topLevelPath : "+ topLevelPath);
		System.out.println("DATA_DIRECTORY : "+ DATA_DIRECTORY);
		
		List<FileInfoVO> fileInfoVOList = new ArrayList<FileInfoVO>();
		List<FolderInfoVO> folderInfoVOList = new ArrayList<FolderInfoVO>();
		//최종 수정일 조회
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd  HH:mm"); 
		//디렉토리 내 파일과 폴더 구분해서 뽑아내기
		if(null != dir.listFiles()) {
			int count1 = 0; //폴더카운트
			int count2 = 0;	//파일카운트
			for (File filename : dir.listFiles()) { 
				if (filename.isDirectory()) {
					count1++;
					FolderInfoVO folderInfoVO = new FolderInfoVO(); //현재 페이지의 폴더정보를 담을 VO 생성
		            folderInfoVO.setCount(String.valueOf(count1));		//임시 폴더번호
		            folderInfoVO.setFilenames(filename.getName());		//폴더이름
		            folderInfoVO.setLastModified(sf.format(filename.lastModified()));	//최종 수정일
		            folderInfoVOList.add(folderInfoVO);
		            
		        }
		        if (filename.isFile()) {
		        	FileInfoVO fileInfoVO = new FileInfoVO();		//현재 페이지의 파일정보를 담을 VO 생성
		        	count2++;
		            long bytes = filename.length();			//파일크기 byte
		    		long kilobyte = bytes / 1024;			//파일크기 kb
		    		String ext = FilenameUtils.getExtension(filename.getName());
		    		
		    		fileInfoVO.setCount(String.valueOf(count2));		//임시 파일번호
		    		fileInfoVO.setFilenames(filename.getName());		//파일 이름
		    		fileInfoVO.setLastModified(sf.format(filename.lastModified()));	//최종 수정일
		    		fileInfoVO.setFileSize(kilobyte);			//파일 사이즈
		    		fileInfoVO.setExtension(ext);						//파일 확장자
		    		fileInfoVOList.add(fileInfoVO);
		    		
		        }
			}
		}
		List<Map<String, Object>> folderList = sideBar();
		
		
		model.addAttribute("folderList",folderList);
		model.addAttribute("folderInfoVOList",folderInfoVOList);
		model.addAttribute("fileInfoVOList",fileInfoVOList);
		model.addAttribute("path",DATA_DIRECTORY);
		
		return "file/search";
	}
	
	/**
	 * @return 사이드바를 위한 최상위 경로 폴더들  
	 */
	public List<Map<String, Object>> sideBar() {
		String topLevelPath = this.localFileService.topPath();	//디비에 있는 최상위 폴더 경로 가져옴
		String DATA_DIRECTORY = topLevelPath; 
		if(DATA_DIRECTORY.equals("C:")) {
			DATA_DIRECTORY += "\\";
		}
		File dir = new File(DATA_DIRECTORY);
		List<Map<String, Object>> folderList = new ArrayList<Map<String, Object>>();
		if(null != dir.listFiles()) {
			for (File filename : dir.listFiles()) {
				Map<String, Object> map = new HashMap<>();
				if (filename.isDirectory()) {
					map.put("folderName", filename.getName());
					map.put("folderPath", filename);
					folderList.add(map);
		        }
			}
		}
		
		return folderList;
	}
	@RequestMapping("/searchKeyword")
	public String searchKeyword(@ModelAttribute FileInfoVO fileInfoVO,Model model) {
		String topLevelPath = this.localFileService.topPath();	//디비에 있는 최상위 폴더 경로 가져옴
		String DATA_DIRECTORY = topLevelPath; 
		if(DATA_DIRECTORY.equals("C:")) {
			DATA_DIRECTORY += "\\";
		}
		
		System.out.println("검색 : " + fileInfoVO.getKeyword()); //검색 조건	
		String nowDirPath = fileInfoVO.getNowDirPath();
		if(nowDirPath.equals("C:")) {
			nowDirPath += "\\";
		}
		
		List<Map<String, Object>> folderList = sideBar();
		List<FolderInfoVO> folderInfoVOList = new ArrayList<FolderInfoVO>();
		List<FileInfoVO> fileInfoVOList = new ArrayList<FileInfoVO>();
		List[] folderInfoVOList2 = searchKeyword2(nowDirPath,fileInfoVO.getKeyword(),folderInfoVOList,fileInfoVOList);
		
		
		model.addAttribute("folderInfoVOList",folderInfoVOList2[0]);
		model.addAttribute("fileInfoVOList",folderInfoVOList2[1]);
		model.addAttribute("path",nowDirPath);
		model.addAttribute("keyword",fileInfoVO.getKeyword());
		model.addAttribute("topLevelPath",topLevelPath);
		model.addAttribute("folderList",folderList);
		
		return "file/searchKeyword";
	}
	
	
	//재귀적 호출을 위한 메서드
	public static List[] searchKeyword2(String dirPath,String searchReq, List<FolderInfoVO> folderInfoVOList, List<FileInfoVO> fileInfoVOList) {
		FolderInfoVO folderInfoVO ; 
		FileInfoVO fileInfoVO ; 
	    File dir = new File(dirPath);
	    File files[] = dir.listFiles();
	    if(files != null) {
		    for (int i = 0; i < files.length; i++) {
		    	folderInfoVO = new FolderInfoVO();
		    	fileInfoVO = new FileInfoVO();
		        File file = files[i];
		        if (file.isDirectory()) {
		        	if (file.getName().startsWith(searchReq)){
		        		System.out.println("폴더 : " + file);
		        		folderInfoVO.setFilenames(file.getName());
		        		folderInfoVO.setNowDirPath(file.getAbsolutePath());
		        		folderInfoVOList.add(folderInfoVO);
		        	}
		        	
		        	searchKeyword2(file.getPath(),searchReq,folderInfoVOList,fileInfoVOList);
		        } else if (file.getName().startsWith(searchReq)){
		        	System.out.println("파일 : " + file);
		        	String ext = FilenameUtils.getExtension(file.getName());
		        	fileInfoVO.setFilenames(file.getName());
		        	fileInfoVO.setNowDirPath(file.getAbsolutePath());
		        	fileInfoVO.setExtension(ext);
		        	
		        	long bytes = file.length();         //파일크기 byte
	                long kilobyte = bytes / 1024;         //파일크기 kb
	                fileInfoVO.setFileSize(kilobyte);
	                fileInfoVOList.add(fileInfoVO);
		        }
		        
		    }
	    }
	    return new List[] {folderInfoVOList, fileInfoVOList};
	}
	
	@RequestMapping("/search")   // /file/search
	   public String fileSearchTest2(HttpSession session, Model model, @ModelAttribute FileInfoVO fileInfoVOParam,HttpServletRequest req) {
	      String topLevelPath = this.localFileService.topPath();   //디비에 있는 최상위 폴더 경로 가져옴
	      model.addAttribute("topLevelPath",topLevelPath);
	      System.out.println("topLevelPath : "+ topLevelPath);
	      String DATA_DIRECTORY ="";
	      System.out.println(fileInfoVOParam);
	      
	      //받는 부분
	      Map<String, ?> path = RequestContextUtils.getInputFlashMap(req);
	      if(path !=null && null == fileInfoVOParam.getClickDirName()) {  
	         String param = (String) path.get("path");
	         System.out.println("param : "+ param);
	         DATA_DIRECTORY = param;
	         session.setAttribute("path", DATA_DIRECTORY);
	         System.out.println("session : "+session.getAttribute("path"));
	      }else {
	         if(null == fileInfoVOParam.getClickDirName()) {
	            System.out.println(path);
	            DATA_DIRECTORY = (String) session.getAttribute("path");
	         }else { 
	            System.out.println("removeSession : "+session.getAttribute("path"));
	            if(fileInfoVOParam.getClickDirName()=="") {
	               DATA_DIRECTORY = fileInfoVOParam.getNowDirPath();
	            }else {
	               if(fileInfoVOParam.getNowDirPath().equals("C:\\")) {
	                  DATA_DIRECTORY = fileInfoVOParam.getNowDirPath() +fileInfoVOParam.getClickDirName();
	               }else {
	                  DATA_DIRECTORY = fileInfoVOParam.getNowDirPath() +"\\"+ fileInfoVOParam.getClickDirName();
	               }
	            }
	         }
	      }
	      if(null == DATA_DIRECTORY) {
	         return "redirect:/file?msg=off";
	      }
	      if(DATA_DIRECTORY.equals("C:")) {
	         DATA_DIRECTORY += "\\";
	      }
	      
	      System.out.println("DATA_DIRECTORY : "+ DATA_DIRECTORY);
	      File dir = new File(DATA_DIRECTORY); 
	      
	      List<FileInfoVO> fileInfoVOList = new ArrayList<FileInfoVO>();
	      List<FolderInfoVO> folderInfoVOList = new ArrayList<FolderInfoVO>();
	      //최종 수정일 조회
	      SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd  HH:mm"); 
	      //디렉토리 내 파일과 폴더 구분해서 뽑아내기
	      if(null != dir.listFiles()) {
	         int count1 = 0; //폴더카운트
	         int count2 = 0;   //파일카운트
	         for (File filename : dir.listFiles()) { 
	            if (filename.isDirectory()) {
	               count1++;
	               FolderInfoVO folderInfoVO = new FolderInfoVO(); //현재 페이지의 폴더정보를 담을 VO 생성
	                  folderInfoVO.setCount(String.valueOf(count1));      //임시 폴더번호
	                  String absolutePath = filename.getAbsolutePath();
	                  folderInfoVO.setNowDirPath(absolutePath.substring(ROOTPATH.length())); // 현재폴더 경로
	                  folderInfoVO.setFilenames(filename.getName());      //폴더이름
	                  folderInfoVO.setLastModified(sf.format(filename.lastModified()));   //최종 수정일
	                  folderInfoVOList.add(folderInfoVO);
	                  
	              }
	              if (filename.isFile()) {
	                 FileInfoVO fileInfoVO = new FileInfoVO();      //현재 페이지의 파일정보를 담을 VO 생성
	                 count2++;
	                 long bytes = filename.length();         //파일크기 byte
	                 long kilobyte = bytes / 1024;         //파일크기 kb
	                String ext = FilenameUtils.getExtension(filename.getName());
	                 System.out.println("filename.getPath() -- " + filename.getPath());
	                  System.out.println("filename.getAbsolutePath() -- " + filename.getAbsolutePath());
	                fileInfoVO.setCount(String.valueOf(count2));      //임시 파일번호
	                String absolutePath = filename.getAbsolutePath();
	                fileInfoVO.setNowDirPath(absolutePath.substring(ROOTPATH.length())); // 현재폴더 경로
	                fileInfoVO.setFilenames(filename.getName());      //파일 이름
	                fileInfoVO.setLastModified(sf.format(filename.lastModified()));   //최종 수정일
	                fileInfoVO.setFileSize(kilobyte);         //파일 사이즈
	                fileInfoVO.setExtension(ext);                  //파일 확장자
	                fileInfoVOList.add(fileInfoVO);
	                
	              }
	         }
	      }
	      //이름 정렬
	      if(null != fileInfoVOParam.getSortName()) {
	         if(fileInfoVOParam.getSortName().equals("up")) {
	            folderInfoVOList = folderInfoVOList.stream().sorted(Comparator.comparing(FolderInfoVO::getFilenames,String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
	            fileInfoVOList = fileInfoVOList.stream().sorted(Comparator.comparing(FileInfoVO::getFilenames,String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
	            model.addAttribute("sortName","up");
	         }else if(fileInfoVOParam.getSortName().equals("down")) {
	            folderInfoVOList = folderInfoVOList.stream().sorted(Comparator.comparing(FolderInfoVO::getFilenames,String.CASE_INSENSITIVE_ORDER).reversed()).collect(Collectors.toList());
	            fileInfoVOList = fileInfoVOList.stream().sorted(Comparator.comparing(FileInfoVO::getFilenames,String.CASE_INSENSITIVE_ORDER).reversed()).collect(Collectors.toList());
	            model.addAttribute("sortName","down");
	         }else {
	            model.addAttribute("sortName","");
	         }
	      }
	      //날짜 정렬
	      if(null != fileInfoVOParam.getSortDate()) {
	         if(fileInfoVOParam.getSortDate().equals("up")) {
	            folderInfoVOList = folderInfoVOList.stream().sorted(Comparator.comparing(FolderInfoVO::getLastModified,String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
	            fileInfoVOList = fileInfoVOList.stream().sorted(Comparator.comparing(FileInfoVO::getLastModified,String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
	            model.addAttribute("sortDate","up");
	         }else if(fileInfoVOParam.getSortDate().equals("down")) {
	            folderInfoVOList = folderInfoVOList.stream().sorted(Comparator.comparing(FolderInfoVO::getLastModified,String.CASE_INSENSITIVE_ORDER).reversed()).collect(Collectors.toList());
	            fileInfoVOList = fileInfoVOList.stream().sorted(Comparator.comparing(FileInfoVO::getLastModified,String.CASE_INSENSITIVE_ORDER).reversed()).collect(Collectors.toList());
	            model.addAttribute("sortDate","down");
	         }else {
	            model.addAttribute("sortDate","");
	         }
	      }
	      //파일유형 정렬
	      if(null != fileInfoVOParam.getSortType()) {
	         if(fileInfoVOParam.getSortType().equals("up")) {
	            fileInfoVOList = fileInfoVOList.stream().sorted(Comparator.comparing(FileInfoVO::getExtension,String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
	            model.addAttribute("sortType","up");
	         }else if(fileInfoVOParam.getSortType().equals("down")) {
	            fileInfoVOList = fileInfoVOList.stream().sorted(Comparator.comparing(FileInfoVO::getExtension,String.CASE_INSENSITIVE_ORDER).reversed()).collect(Collectors.toList());
	            model.addAttribute("sortType","down");
	         }else {
	            model.addAttribute("sortType","");
	         }
	      }
	      //크기 정렬
	      if(null != fileInfoVOParam.getSortSize()) {
	         if(fileInfoVOParam.getSortSize().equals("up")) {
	            fileInfoVOList = fileInfoVOList.stream().sorted(Comparator.comparing(FileInfoVO::getFileSize)).collect(Collectors.toList());
	            model.addAttribute("sortSize","up");
	         }else if(fileInfoVOParam.getSortSize().equals("down")) {
	            fileInfoVOList = fileInfoVOList.stream().sorted(Comparator.comparing(FileInfoVO::getFileSize).reversed()).collect(Collectors.toList());
	            model.addAttribute("sortSize","down");
	         }else {
	            model.addAttribute("sortSize","");
	         }
	      }
	      
	      List<Map<String, Object>> folderList = sideBar();
			
		  model.addAttribute("folderList",folderList);
	      model.addAttribute("folderInfoVOList",folderInfoVOList);
	      model.addAttribute("fileInfoVOList",fileInfoVOList);
	      model.addAttribute("path",DATA_DIRECTORY);
	      
	      return "file/search";
	   }
	
		@RequestMapping("/makeDir")
		public ModelAndView makeDir(MultipartHttpServletRequest mtfRequest, HttpServletRequest request,
				HttpSession session, Model model, ModelAndView mav, @ModelAttribute FolderInfoVO folderInfoVO,
				RedirectAttributes redirect) throws UnsupportedEncodingException {
			request.setCharacterEncoding("UTF-8");
			// 폴더 상대경로
			String path = folderInfoVO.getNowDirPath();
			String name = folderInfoVO.getNewFolderName();
			System.out.println("path : " + path);
			System.out.println("name : " + name);
			// nowDirPath
			String folderPath = path;
			File makeFolder = new File(folderPath, name);
			System.out.println("folderPath : " + folderPath);

			// 폴더를 생성합니다.
			boolean mkDir = makeFolder.mkdir();
			System.out.println("mkDir : " + mkDir);
			if (mkDir) {
				System.out.println("폴더를 생성합니다.");
			}

			redirect.addFlashAttribute("path", path);
			mav.setViewName("redirect:/file/search");

			return mav;
		}

		@RequestMapping("/changeName")
		public ModelAndView changeName(MultipartHttpServletRequest mtfRequest, HttpServletRequest request,
				HttpSession session, Model model, ModelAndView mav, @ModelAttribute FolderInfoVO folderInfoVO,
				RedirectAttributes redirect) throws UnsupportedEncodingException {
			request.setCharacterEncoding("UTF-8");
			logger.info("change folderInfoVO : " + folderInfoVO);

			String path = folderInfoVO.getNowDirPath();
			redirect.addFlashAttribute("path", path);
			path = path.replace("\\", "/");

			// 폴더인 경우
			if (folderInfoVO.getExtension().equals("")) {
				File file = new File(path + "\\" + folderInfoVO.getFilenames()); // 기존파일 경로 + 기존파일이름
				boolean success = file.renameTo(new File(path + "\\" + folderInfoVO.getNewFolderName()));

			} else { // 파일인경우
				System.out.println("path : " + path);
				File file = new File(path + "\\" + folderInfoVO.getFilenames()); // 기존파일 경로 + 기존파일이름
				boolean success = file.renameTo(
						new File(path + "\\" + folderInfoVO.getNewFolderName() + "." + folderInfoVO.getExtension()));
				System.out.println(success);
			}

			mav.setViewName("redirect:/file/search");

			return mav;
		}
	 
		@RequestMapping("/fileUpload")
		public ModelAndView fileUpload(HttpSession session, HttpServletRequest request,
				MultipartHttpServletRequest mtfRequest, ModelAndView mav, @ModelAttribute FolderInfoVO folderInfoVO,
				RedirectAttributes redirect) throws UnsupportedEncodingException {
			request.setCharacterEncoding("UTF-8");
			List<MultipartFile> fileList = mtfRequest.getFiles("file");
			logger.info("folderInfoVO : " + folderInfoVO);
			logger.info("fileList : " + fileList);

			String path = folderInfoVO.getNowDirPath();
			// 파일명 중복 방지
			File dir = new File(path);
			List<String> list = new ArrayList<>();
			if (null != dir.listFiles()) {
				System.out.println("현재 디렉토리의 폴더 및 파일 개수 :" + dir.listFiles().length);
				list = new ArrayList<>();
				for (File filename : dir.listFiles()) {
					if (filename.isDirectory()) {
						filename.getName(); // 폴더이름
					}
					if (filename.isFile()) {
						String ext = FilenameUtils.getExtension(filename.getName());
						filename.getName(); // 파일 이름
					}
					list.add(filename.getName());
				}
			} // if문 끝
			System.out.println("현재 디렉토리 파일명 리스트 : " + list);

			// VO에 List형식으로 담기위해 증가시킬 숫자
			int count = 0;
			List<FileInfoVO> temp1 = new ArrayList<FileInfoVO>();
			for (MultipartFile mf : fileList) {
				String originFileName = mf.getOriginalFilename(); // 원본 파일 명
				String extension = originFileName.substring(originFileName.lastIndexOf(".")); // 파일 확장자 가져오기
				String notExtFileName = originFileName.substring(0, originFileName.lastIndexOf("."));
				String savedFileName = ""; // 저장될 파일 명 : 랜덤수 + 파일 확장자
				System.out.println("원본파일 명 : " + originFileName);
				System.out.println("notExtFileName : " + notExtFileName);
				System.out.println("extension : " + extension);
				System.out.println("저장 위치 : " + path);
				System.out.println("저장될 파일 명 : " + savedFileName);
				for (int i = 0; i < list.size(); i++) {
					for (int j = 0; j < list.size(); j++) {
						if (originFileName.toLowerCase().equals(list.get(j).toLowerCase())) {
							System.out.println("originFileName : " + originFileName);
							System.out.println("list.get(j) : " + list.get(j) + "i :" + i + "j : " + j);
							originFileName = notExtFileName + "(" + (i + 1) + ")" + extension;
							break;
						}
					}
				}
				savedFileName = originFileName;
				System.out.println("중복제거 savedFileName :" + savedFileName);

				try {
					mf.transferTo(new File(path, savedFileName));
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			redirect.addFlashAttribute("path", folderInfoVO.getNowDirPath());
			mav.setViewName("redirect:/file/search");

			return mav;
		}
	 
		@RequestMapping("/deleteFiles")
		public String deleteFiles(HttpServletRequest request, HttpSession session,
				@ModelAttribute FolderInfoVO folderInfoVO, Model model, RedirectAttributes redirect)
				throws UnsupportedEncodingException {
			request.setCharacterEncoding("UTF-8");
			logger.info("delete folderInfoVO : " + folderInfoVO);
			// 로그인 세션 받기
			session = request.getSession();
			logger.info("로그인 아이디 세션 : " + session.getAttribute("adminVo"));
			// 폴더인 경우
			for (FolderInfoVO voList : folderInfoVO.getFolderInfoVOList()) {
				String fileName = voList.getNewFolderName();
				String extension = voList.getExtension();
				if (null != fileName && null != extension) {
					fileName = fileName.replace(",", ""); // 기본 jsp에서 FolderInfoVOList[0].newFolderName으로된 input과 같은
															// name의 append 시켜주는 input 때문에 2개이상 선택할 경우 0번째 인덱스가 ,가 붙어서
															// 나옴 때문에 ,를 없애줘야함
					extension = extension.replace(",", "");
					if (extension.equals("undefined")) {
						String path = folderInfoVO.getNowDirPath() + "/" + fileName;
						path = path.replace("\\", "/");
						logger.info("path :" + path);
						deleteFile(path);
					} else { // 파일인 경우
						String filePath = folderInfoVO.getNowDirPath() + "/" + fileName;
						filePath = filePath.replace("\\", "/"); // 웹 상 경로로 변경
						logger.info("filePath :" + filePath);

						// 파일의 경로 + 파일명
						File deleteFile = new File(filePath);
						// 파일이 존재하는지 체크 존재할경우 true, 존재하지않을경우 false
						if (deleteFile.exists()) {
							// 파일을 삭제합니다.
							boolean success = deleteFile.delete();
							System.out.println("파일삭제 : " + success);
						} else {
							System.out.println("파일이 존재하지 않습니다.");
						}
					}
				}
			}

			redirect.addFlashAttribute("path", folderInfoVO.getNowDirPath());

			return "redirect:/file/search";
		}
	 
	 
		public static void deleteFile(String path) {
			File deleteFolder = new File(path);

			if (deleteFolder.exists()) {
				File[] deleteFolderList = deleteFolder.listFiles();

				for (int i = 0; i < deleteFolderList.length; i++) {
					if (deleteFolderList[i].isFile()) {
						deleteFolderList[i].delete();
					} else {
						deleteFile(deleteFolderList[i].getPath());
					}
					deleteFolderList[i].delete();
				}
				deleteFolder.delete();
			}
		}
	 
		@RequestMapping("/pasteFile")
		public String copyFiles(HttpServletRequest request, HttpSession session,
				@ModelAttribute FolderInfoVO folderInfoVO, Model model, RedirectAttributes redirect)
				throws IOException {
			request.setCharacterEncoding("UTF-8");
			logger.info("복사할 위치 : " + folderInfoVO);
			logger.info("session copyPath : " + session.getAttribute("copyPath"));
			logger.info("session copyfileNames : " + session.getAttribute("copyfileNames"));
			folderInfoVO.setFolderInfoVOList((List<FolderInfoVO>) session.getAttribute("copyfileNames"));
			logger.info("session foldervoList : " + folderInfoVO.getFolderInfoVOList());

			String nowPath = (String) session.getAttribute("copyPath"); // 복사버튼 누른 폴더패스
			String newPath = folderInfoVO.getNowDirPath();
			for (FolderInfoVO voList : folderInfoVO.getFolderInfoVOList()) {
				String nowFileName = voList.getNewFolderName(); // 기존 파일 이름
				String originFile = nowPath + "\\" + nowFileName;
				File file = new File(originFile);
				System.out.println("기존 file : " + file);
				if (file.isDirectory()) {
					System.out.println("폴더 : " + originFile);
					String newFilePath = newPath + "\\" + nowFileName;
					// 2.폴더복사
					File destinationDirectory = new File(newFilePath);
					if (!file.equals(destinationDirectory)) {
						FileUtils.copyDirectory(file, destinationDirectory);
					}
				} else if (file.isFile()) {
					System.out.println("파일 : " + originFile);
					String newFilePath = newPath + "\\" + nowFileName;
					// 1. 파일복사
					File newFile = new File(newFilePath);
					if (!file.equals(newFile)) {
						FileUtils.copyFile(file, newFile);
					}
				}
			}
			redirect.addFlashAttribute("path", folderInfoVO.getNowDirPath());

			return "redirect:/file/search";
		}
	 
		@ResponseBody
		@RequestMapping(value = "/copyPath", method = RequestMethod.POST)
		public Map<String, Object> searchSchedule(@ModelAttribute FolderInfoVO folderInfoVO, Model model,
				HttpSession session, ModelAndView mav) {
			logger.info("folderInfoVO :" + folderInfoVO);
			logger.info("folderInfoVO.getFolderInfoVOList() :" + folderInfoVO.getFolderInfoVOList());
			// 복사한 dirPath 저장
			session.setAttribute("copyPath", folderInfoVO.getNowDirPath());
			// 복사한 fileName들 복사
			session.setAttribute("copyfileNames", folderInfoVO.getFolderInfoVOList());

			Map<String, Object> map = new HashMap<>();
			map.put("copyPath", folderInfoVO.getNowDirPath());
			map.put("copyfileNames", folderInfoVO.getFolderInfoVOList());

			return map;
		}

		@RequestMapping(value = "/download")
		public void download(HttpServletResponse response, HttpServletRequest request, FolderInfoVO fv,
				FileInfoVO fileVo) {

			String fullPath = request.getParameter("path");
			String fileName = request.getParameter("fileName");

			System.out.println("fullPath : " + fullPath);
			System.out.println("fileName : " + fileName);
			File dir = new File(fullPath);

			// 파일 다운로드
			FileInputStream fileInputStream = null;
			ServletOutputStream servletOutputStream = null;

			try {
				String downName = null;
				String browser = ((HttpServletRequest) request).getHeader("User-Agent");
				// 파일 인코딩
				if (browser.contains("MSIE") || browser.contains("Trident") || browser.contains("Chrome")) {// 브라우저

					downName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");

				} else {

					downName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");

				}

				response.setHeader("Content-Disposition", "attachment;filename=\"" + downName + "\"");
				response.setContentType("application/octer-stream");
				response.setHeader("Content-Transfer-Encoding", "binary;");

				fileInputStream = new FileInputStream(dir);
				servletOutputStream = response.getOutputStream();

				FileCopyUtils.copy(fileInputStream, servletOutputStream); // 추가 !!

				servletOutputStream.flush();// 출력

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (servletOutputStream != null) {
					try {
						servletOutputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (fileInputStream != null) {
					try {
						fileInputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	
	
	
}

