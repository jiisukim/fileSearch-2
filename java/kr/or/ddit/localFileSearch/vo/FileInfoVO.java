package kr.or.ddit.localFileSearch.vo;

public class FileInfoVO {
	private String count;
	private String filenames;
	private String lastModified;
	private long fileSize;
	private String extension;
	
	private String topLevelPath;
	private String clickDirName;
	private String nowDirPath;
	private String newFolderName;
	
	private String sortName;
	private String sortDate;
	private String sortType;
	private String sortSize;
	private String keyword;
	private String clickEvent;
	
	
	
	public String getClickEvent() {
		return clickEvent;
	}
	public void setClickEvent(String clickEvent) {
		this.clickEvent = clickEvent;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String getSortName() {
		return sortName;
	}
	public void setSortName(String sortName) {
		this.sortName = sortName;
	}
	public String getSortDate() {
		return sortDate;
	}
	public void setSortDate(String sortDate) {
		this.sortDate = sortDate;
	}
	public String getSortType() {
		return sortType;
	}
	public void setSortType(String sortType) {
		this.sortType = sortType;
	}
	public String getSortSize() {
		return sortSize;
	}
	public void setSortSize(String sortSize) {
		this.sortSize = sortSize;
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
	public String getNewFolderName() {
		return newFolderName;
	}
	public void setNewFolderName(String newFolderName) {
		this.newFolderName = newFolderName;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public String getFilenames() {
		return filenames;
	}
	public void setFilenames(String filenames) {
		this.filenames = filenames;
	}
	public String getLastModified() {
		return lastModified;
	}
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	@Override
	public String toString() {
		return "FileInfoVO [count=" + count + ", filenames=" + filenames + ", lastModified=" + lastModified
				+ ", fileSize=" + fileSize + ", extension=" + extension + ", topLevelPath=" + topLevelPath
				+ ", clickDirName=" + clickDirName + ", nowDirPath=" + nowDirPath + ", newFolderName=" + newFolderName
				+ ", sortName=" + sortName + ", sortDate=" + sortDate + ", sortType=" + sortType + ", sortSize="
				+ sortSize + ", keyword=" + keyword + ", clickEvent=" + clickEvent + "]";
	}

	
	
}
