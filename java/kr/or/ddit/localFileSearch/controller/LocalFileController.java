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

	private final String ROOTPATH = "C:\\";		//dispatherServlet??? ???????????? ??????
	
	@RequestMapping("/dragTest")
	public String dragTest() {
		
		return "file/dragTest";
		
	}
	
	@RequestMapping("")	// /file/search
	public String fileSearchTest(Model model) throws IOException {
		String topLevelPath = this.localFileService.topPath();	//????????? ?????? ????????? ?????? ?????? ?????????
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
		//?????? ????????? ??????
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd  HH:mm"); 
		//???????????? ??? ????????? ?????? ???????????? ????????????
		if(null != dir.listFiles()) {
			int count1 = 0; //???????????????
			int count2 = 0;	//???????????????
			for (File filename : dir.listFiles()) { 
				if (filename.isDirectory()) {
					count1++;
					FolderInfoVO folderInfoVO = new FolderInfoVO(); //?????? ???????????? ??????????????? ?????? VO ??????
		            folderInfoVO.setCount(String.valueOf(count1));		//?????? ????????????
		            folderInfoVO.setFilenames(filename.getName());		//????????????
		            folderInfoVO.setLastModified(sf.format(filename.lastModified()));	//?????? ?????????
		            folderInfoVOList.add(folderInfoVO);
		            
		        }
		        if (filename.isFile()) {
		        	FileInfoVO fileInfoVO = new FileInfoVO();		//?????? ???????????? ??????????????? ?????? VO ??????
		        	count2++;
		            long bytes = filename.length();			//???????????? byte
		    		long kilobyte = bytes / 1024;			//???????????? kb
		    		String ext = FilenameUtils.getExtension(filename.getName());
		    		
		    		fileInfoVO.setCount(String.valueOf(count2));		//?????? ????????????
		    		fileInfoVO.setFilenames(filename.getName());		//?????? ??????
		    		fileInfoVO.setLastModified(sf.format(filename.lastModified()));	//?????? ?????????
		    		fileInfoVO.setFileSize(kilobyte);			//?????? ?????????
		    		fileInfoVO.setExtension(ext);						//?????? ?????????
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
	 * @return ??????????????? ?????? ????????? ?????? ?????????  
	 */
	public List<Map<String, Object>> sideBar() {
		String topLevelPath = this.localFileService.topPath();	//????????? ?????? ????????? ?????? ?????? ?????????
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
		String topLevelPath = this.localFileService.topPath();	//????????? ?????? ????????? ?????? ?????? ?????????
		String DATA_DIRECTORY = topLevelPath; 
		if(DATA_DIRECTORY.equals("C:")) {
			DATA_DIRECTORY += "\\";
		}
		
		System.out.println("?????? : " + fileInfoVO.getKeyword()); //?????? ??????	
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
	
	
	//????????? ????????? ?????? ?????????
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
		        		System.out.println("?????? : " + file);
		        		folderInfoVO.setFilenames(file.getName());
		        		folderInfoVO.setNowDirPath(file.getAbsolutePath());
		        		folderInfoVOList.add(folderInfoVO);
		        	}
		        	
		        	searchKeyword2(file.getPath(),searchReq,folderInfoVOList,fileInfoVOList);
		        } else if (file.getName().startsWith(searchReq)){
		        	System.out.println("?????? : " + file);
		        	String ext = FilenameUtils.getExtension(file.getName());
		        	fileInfoVO.setFilenames(file.getName());
		        	fileInfoVO.setNowDirPath(file.getAbsolutePath());
		        	fileInfoVO.setExtension(ext);
		        	
		        	long bytes = file.length();         //???????????? byte
	                long kilobyte = bytes / 1024;         //???????????? kb
	                fileInfoVO.setFileSize(kilobyte);
	                fileInfoVOList.add(fileInfoVO);
		        }
		        
		    }
	    }
	    return new List[] {folderInfoVOList, fileInfoVOList};
	}
	
	@RequestMapping("/search")   // /file/search
	   public String fileSearchTest2(HttpSession session, Model model, @ModelAttribute FileInfoVO fileInfoVOParam,HttpServletRequest req) {
	      String topLevelPath = this.localFileService.topPath();   //????????? ?????? ????????? ?????? ?????? ?????????
	      model.addAttribute("topLevelPath",topLevelPath);
	      System.out.println("topLevelPath : "+ topLevelPath);
	      String DATA_DIRECTORY ="";
	      System.out.println(fileInfoVOParam);
	      
	      //?????? ??????
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
	      //?????? ????????? ??????
	      SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd  HH:mm"); 
	      //???????????? ??? ????????? ?????? ???????????? ????????????
	      if(null != dir.listFiles()) {
	         int count1 = 0; //???????????????
	         int count2 = 0;   //???????????????
	         for (File filename : dir.listFiles()) { 
	            if (filename.isDirectory()) {
	               count1++;
	               FolderInfoVO folderInfoVO = new FolderInfoVO(); //?????? ???????????? ??????????????? ?????? VO ??????
	                  folderInfoVO.setCount(String.valueOf(count1));      //?????? ????????????
	                  String absolutePath = filename.getAbsolutePath();
	                  folderInfoVO.setNowDirPath(absolutePath.substring(ROOTPATH.length())); // ???????????? ??????
	                  folderInfoVO.setFilenames(filename.getName());      //????????????
	                  folderInfoVO.setLastModified(sf.format(filename.lastModified()));   //?????? ?????????
	                  folderInfoVOList.add(folderInfoVO);
	                  
	              }
	              if (filename.isFile()) {
	                 FileInfoVO fileInfoVO = new FileInfoVO();      //?????? ???????????? ??????????????? ?????? VO ??????
	                 count2++;
	                 long bytes = filename.length();         //???????????? byte
	                 long kilobyte = bytes / 1024;         //???????????? kb
	                String ext = FilenameUtils.getExtension(filename.getName());
	                 System.out.println("filename.getPath() -- " + filename.getPath());
	                  System.out.println("filename.getAbsolutePath() -- " + filename.getAbsolutePath());
	                fileInfoVO.setCount(String.valueOf(count2));      //?????? ????????????
	                String absolutePath = filename.getAbsolutePath();
	                fileInfoVO.setNowDirPath(absolutePath.substring(ROOTPATH.length())); // ???????????? ??????
	                fileInfoVO.setFilenames(filename.getName());      //?????? ??????
	                fileInfoVO.setLastModified(sf.format(filename.lastModified()));   //?????? ?????????
	                fileInfoVO.setFileSize(kilobyte);         //?????? ?????????
	                fileInfoVO.setExtension(ext);                  //?????? ?????????
	                fileInfoVOList.add(fileInfoVO);
	                
	              }
	         }
	      }
	      //?????? ??????
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
	      //?????? ??????
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
	      //???????????? ??????
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
	      //?????? ??????
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
			// ?????? ????????????
			String path = folderInfoVO.getNowDirPath();
			String name = folderInfoVO.getNewFolderName();
			System.out.println("path : " + path);
			System.out.println("name : " + name);
			// nowDirPath
			String folderPath = path;
			File makeFolder = new File(folderPath, name);
			System.out.println("folderPath : " + folderPath);

			// ????????? ???????????????.
			boolean mkDir = makeFolder.mkdir();
			System.out.println("mkDir : " + mkDir);
			if (mkDir) {
				System.out.println("????????? ???????????????.");
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

			// ????????? ??????
			if (folderInfoVO.getExtension().equals("")) {
				File file = new File(path + "\\" + folderInfoVO.getFilenames()); // ???????????? ?????? + ??????????????????
				boolean success = file.renameTo(new File(path + "\\" + folderInfoVO.getNewFolderName()));

			} else { // ???????????????
				System.out.println("path : " + path);
				File file = new File(path + "\\" + folderInfoVO.getFilenames()); // ???????????? ?????? + ??????????????????
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
			// ????????? ?????? ??????
			File dir = new File(path);
			List<String> list = new ArrayList<>();
			if (null != dir.listFiles()) {
				System.out.println("?????? ??????????????? ?????? ??? ?????? ?????? :" + dir.listFiles().length);
				list = new ArrayList<>();
				for (File filename : dir.listFiles()) {
					if (filename.isDirectory()) {
						filename.getName(); // ????????????
					}
					if (filename.isFile()) {
						String ext = FilenameUtils.getExtension(filename.getName());
						filename.getName(); // ?????? ??????
					}
					list.add(filename.getName());
				}
			} // if??? ???
			System.out.println("?????? ???????????? ????????? ????????? : " + list);

			// VO??? List???????????? ???????????? ???????????? ??????
			int count = 0;
			List<FileInfoVO> temp1 = new ArrayList<FileInfoVO>();
			for (MultipartFile mf : fileList) {
				String originFileName = mf.getOriginalFilename(); // ?????? ?????? ???
				String extension = originFileName.substring(originFileName.lastIndexOf(".")); // ?????? ????????? ????????????
				String notExtFileName = originFileName.substring(0, originFileName.lastIndexOf("."));
				String savedFileName = ""; // ????????? ?????? ??? : ????????? + ?????? ?????????
				System.out.println("???????????? ??? : " + originFileName);
				System.out.println("notExtFileName : " + notExtFileName);
				System.out.println("extension : " + extension);
				System.out.println("?????? ?????? : " + path);
				System.out.println("????????? ?????? ??? : " + savedFileName);
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
				System.out.println("???????????? savedFileName :" + savedFileName);

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
			// ????????? ?????? ??????
			session = request.getSession();
			logger.info("????????? ????????? ?????? : " + session.getAttribute("adminVo"));
			// ????????? ??????
			for (FolderInfoVO voList : folderInfoVO.getFolderInfoVOList()) {
				String fileName = voList.getNewFolderName();
				String extension = voList.getExtension();
				if (null != fileName && null != extension) {
					fileName = fileName.replace(",", ""); // ?????? jsp?????? FolderInfoVOList[0].newFolderName????????? input??? ??????
															// name??? append ???????????? input ????????? 2????????? ????????? ?????? 0?????? ???????????? ,??? ?????????
															// ?????? ????????? ,??? ???????????????
					extension = extension.replace(",", "");
					if (extension.equals("undefined")) {
						String path = folderInfoVO.getNowDirPath() + "/" + fileName;
						path = path.replace("\\", "/");
						logger.info("path :" + path);
						deleteFile(path);
					} else { // ????????? ??????
						String filePath = folderInfoVO.getNowDirPath() + "/" + fileName;
						filePath = filePath.replace("\\", "/"); // ??? ??? ????????? ??????
						logger.info("filePath :" + filePath);

						// ????????? ?????? + ?????????
						File deleteFile = new File(filePath);
						// ????????? ??????????????? ?????? ??????????????? true, ???????????????????????? false
						if (deleteFile.exists()) {
							// ????????? ???????????????.
							boolean success = deleteFile.delete();
							System.out.println("???????????? : " + success);
						} else {
							System.out.println("????????? ???????????? ????????????.");
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
			logger.info("????????? ?????? : " + folderInfoVO);
			logger.info("session copyPath : " + session.getAttribute("copyPath"));
			logger.info("session copyfileNames : " + session.getAttribute("copyfileNames"));
			folderInfoVO.setFolderInfoVOList((List<FolderInfoVO>) session.getAttribute("copyfileNames"));
			logger.info("session foldervoList : " + folderInfoVO.getFolderInfoVOList());

			String nowPath = (String) session.getAttribute("copyPath"); // ???????????? ?????? ????????????
			String newPath = folderInfoVO.getNowDirPath();
			for (FolderInfoVO voList : folderInfoVO.getFolderInfoVOList()) {
				String nowFileName = voList.getNewFolderName(); // ?????? ?????? ??????
				String originFile = nowPath + "\\" + nowFileName;
				File file = new File(originFile);
				System.out.println("?????? file : " + file);
				if (file.isDirectory()) {
					System.out.println("?????? : " + originFile);
					String newFilePath = newPath + "\\" + nowFileName;
					// 2.????????????
					File destinationDirectory = new File(newFilePath);
					if (!file.equals(destinationDirectory)) {
						FileUtils.copyDirectory(file, destinationDirectory);
					}
				} else if (file.isFile()) {
					System.out.println("?????? : " + originFile);
					String newFilePath = newPath + "\\" + nowFileName;
					// 1. ????????????
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
			// ????????? dirPath ??????
			session.setAttribute("copyPath", folderInfoVO.getNowDirPath());
			// ????????? fileName??? ??????
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

			// ?????? ????????????
			FileInputStream fileInputStream = null;
			ServletOutputStream servletOutputStream = null;

			try {
				String downName = null;
				String browser = ((HttpServletRequest) request).getHeader("User-Agent");
				// ?????? ?????????
				if (browser.contains("MSIE") || browser.contains("Trident") || browser.contains("Chrome")) {// ????????????

					downName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");

				} else {

					downName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");

				}

				response.setHeader("Content-Disposition", "attachment;filename=\"" + downName + "\"");
				response.setContentType("application/octer-stream");
				response.setHeader("Content-Transfer-Encoding", "binary;");

				fileInputStream = new FileInputStream(dir);
				servletOutputStream = response.getOutputStream();

				FileCopyUtils.copy(fileInputStream, servletOutputStream); // ?????? !!

				servletOutputStream.flush();// ??????

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

