package model;

public class OracleAttachFileList {
	private String localFilePath;
	private String serverFilePath;
	private String fileName;
	private String recordId;
	private String localFileName;
	
	
	public String getLocalFileName() {
		return localFileName;
	}
	public void setLocalFileName(String localFileName) {
		this.localFileName = localFileName;
	}
	public String getRecordId() {
		return recordId;
	}
	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}
	public String getLocalFilePath() {
		return localFilePath;
	}
	public void setLocalFilePath(String localFilePath) {
		this.localFilePath = localFilePath;
	}
	public String getServerFilePath() {
		return serverFilePath;
	}
	public void setServerFilePath(String serverFilePath) {
		this.serverFilePath = serverFilePath;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}