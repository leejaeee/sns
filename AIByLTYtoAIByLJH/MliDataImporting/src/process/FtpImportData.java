package process;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Properties;

import model.OracleAttachFileList;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import etc.DBConnect;
import etc.OutputProcess;

public class FtpImportData {
	FTPClient client = null;
	String url = ""; // 서버 ip
	String id = ""; // ftp 접속 id
	String pwd = ""; // ftp 접속 비밀번호
	String port = ""; // ftp 포트
	BufferedOutputStream bos = null;
	ArrayList<OracleAttachFileList> attachFileList = null; 
	Properties props = null;
	String oracleCreateYYYYOrgCd = "";
	DBConnect db = null;
	OutputProcess op = new OutputProcess();
	
	public FtpImportData(String mysqlDBTable, String oracleCreateYYYYOrgCd,Properties props) {
		this.oracleCreateYYYYOrgCd = oracleCreateYYYYOrgCd; // oracle attachfile 이나 openinfo의 쿼리 날릴때 where 입력
		System.out.println("init start");
		this.props = props;
		db = new DBConnect();
		db.initDB("oracle", props);
		
		/** attachfile, openinfo 테이블 insert */
		DBImportData dbid = new DBImportData(db, props, mysqlDBTable, oracleCreateYYYYOrgCd);
		dbid.settingDBFileList();
		attachFileList = dbid.getAttachFileList();
		
		url = props.getProperty("ftp.url");
		id = props.getProperty("ftp.id");
		pwd = props.getProperty("ftp.pwd");
		port = props.getProperty("ftp.port");
		//System.out.println(" ## " + url + " , "+id+ " , " + pwd + " , " + port);
	}
	
	
	public Properties getProps(){
		return props;
	}
	
	public ArrayList<OracleAttachFileList> getAttachFileList(){
		return attachFileList;
	}
	
	public void close_dataSet(){
		db.closeDB();
		try {
			client.logout();
			if (bos != null)
				bos.close();
			if (client != null && client.isConnected())
				client.disconnect();
		} catch (Exception e) {
		}
	}
	
	public void errorRegister(int worklogId ,int nowCountnum, String errMsg){
		db.insert(op.workLogUpdate(2, worklogId, 3,"실패", nowCountnum+"" , errMsg));
		op.errorLog("FTP Err |" +" number : "+nowCountnum + " , " + errMsg);
	}
	
	public int download(String localFilePath ,String serverFilePath ,String fileName ,int worklogId ,int nowCountnum){
		File fDir = null;
		File file = null;
		int result = -1;
		
		try{
			client = new FTPClient();
			client.setControlEncoding("UTF-8");
			client.connect(url, Integer.parseInt(port));
			
			int resultCode = client.getReplyCode();
			if(FTPReply.isPositiveCompletion(resultCode) == false){
				client.disconnect();
				//System.out.println("FTP 서버에 연결할 수 없습니다.");
				errorRegister(worklogId, nowCountnum, fileName + " : FTP 서버에 연결할 수 없습니다.");
			}else{
				//client.setSoTimeout(5000);
				boolean isLogin = client.login(id, pwd);
				if(isLogin == false){
					//System.out.println("FTP 서버에 로그인 할 수 없습니다.");
					errorRegister(worklogId, nowCountnum, fileName + " : FTP 서버에 로그인 할 수 없습니다.");
				}
				client.setFileType(FTP.BINARY_FILE_TYPE);
			}
			// download 경로에 해당하는 디렉토리 생성
			fDir = new File(localFilePath);
			fDir.mkdirs();
			file = new File(localFilePath, fileName);

			client.changeWorkingDirectory(serverFilePath);
			bos = new BufferedOutputStream(new FileOutputStream(file));
			boolean isSuccess = client.retrieveFile(fileName, bos);

			if(isSuccess){
				result = 1; // 성공
			}else{
				//System.out.println("파일 다운로드를 할 수 없습니다.");
				errorRegister(worklogId, nowCountnum, fileName + " : 파일 다운로드를 할 수 없습니다.");
				result = 0; // 실패
			}
			
			try {
				client.logout();
				if (bos != null)
					bos.close();
				if (client != null && client.isConnected())
					client.disconnect();
			} catch (Exception e) {
			}
		}catch(Exception e){
			//System.out.println("FTP Exception : " + e);
			String errMsg = "";
			if(e.toString().length()>100){
				errMsg = e.toString().toString().substring(0,100);
			}else{
				errMsg = e.toString().toString();
			}
			errorRegister(worklogId, nowCountnum, fileName + " : " + errMsg);
			return result;
		}
		return result; // 에러
	}
	
	
	
}