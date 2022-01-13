package kr.or.ddit.localFileSearch.vo;

import java.util.List;


public class FolderInfoVO {
	private String topLevelPath;
	private String clickDirName;
	private String nowDirPath;
	private String lastModified;
	private String filenames;
	private String count;
	private String newFolderName;
	private String extension;
	
	private List<FolderInfoVO> FolderInfoVOList;
	
	
	
	
	public List<FolderInfoVO> getFolderInfoVOList() {
		return FolderInfoVOList;
	}
	public void setFolderInfoVOList(List<FolderInfoVO> folderInfoVOList) {
		FolderInfoVOList = folderInfoVOList;
	}
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	public String getNewFolderName() {
		return newFolderName;
	}
	public void setNewFolderName(String newFolderName) {
		this.newFolderName = newFolderName;
	}
	public String getTopLevelPath() {
		return topLevelPath;
	}
	public void setTopLevelPath(String topLevelPath) {
		this.topLevelPath = topLevelPath;
	}
	public String getClickDirName() {
		return clickDirName;
	}
	public void setClickDirName(String clickDirName) {
		this.clickDirName = clickDirName;
	}
	public String getNowDirPath() {
		return nowDirPath;
	}
	public void setNowDirPath(String nowDirPath) {
		this.nowDirPath = nowDirPath;
	}
	public String getLastModified() {
		return lastModified;
	}
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	public String getFilenames() {
		return filenames;
	}
	public void setFilenames(String filenames) {
		this.filenames = filenames;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	@Override
	public String toString() {
		return "FolderInfoVO [topLevelPath=" + topLevelPath + ", clickDirName=" + clickDirName + ", nowDirPath="
				+ nowDirPath + ", lastModified=" + lastModified + ", filenames=" + filenames + ", count=" + count
				+ ", newFolderName=" + newFolderName + ", extension=" + extension + ", FolderInfoVOList="
				+ FolderInfoVOList + "]";
	}
	
	
}
