package etc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

public class OutputProcess {
	
	String LOG_PATH;
	

	public String workLog(int start, String workName , int pid , String errNum , String errRecordId , String errComment){
		String query = "";
		SimpleDateFormat formatter = new SimpleDateFormat ( "yyyyMMdd_HHmmss", Locale.KOREA );
		Date currentTime = new Date ( );
		String dTime = formatter.format ( currentTime );
		String[] currentDayTime = dTime.split("_");
		
		if(currentDayTime[1].length() == 5) currentDayTime[1] += "0"+currentDayTime[1];
		
		if(start==1){ // 시작쿼리
			query = "insert into sroc.worklog (DATE , TIME , WORK_NM , RESULT , STATE , PID) values ('"+currentDayTime[0]+"','"+currentDayTime[1]+"','"+workName+"','시작' , 0 ,"+pid+" )";
		}else if(start==2){ // 끝 쿼리
			query = "insert into sroc.worklog (DATE , TIME , WORK_NM , RESULT , STATE , PID) values ('"+currentDayTime[0]+"','"+currentDayTime[1]+"','"+workName+"','완료' , 2 ,"+pid+" )";
		}else if(start==3){ // 시작 (부모)
			query = "insert into sroc.worklog (DATE , TIME , WORK_NM , RESULT , STATE) values ('"+currentDayTime[0]+"','"+currentDayTime[1]+"','"+workName+"','시작',0)";
		}else if(start==4){ // 에러
			errComment = errComment.replaceAll("'", "").replaceAll("\\\\", "￦");
			query = "insert into sroc.worklog (DATE , TIME , WORK_NM , RESULT , STATE , PID , ERROR_NUM , ERROR_RECORD_ID , ERROR_COMMENT) values ('"+currentDayTime[0]+"','"+currentDayTime[1]+"','"+workName+"','실패' , 3 ,"+pid+",'"+errNum+"','"+errRecordId+"','"+errComment+"')";
		}else if(start==5){ // 끝 쿼리
			query = "insert into sroc.worklog (DATE , TIME , WORK_NM , RESULT , STATE) values ('"+currentDayTime[0]+"','"+currentDayTime[1]+"','"+workName+"','완료',2)";
		}
		return query;
	}
	
	public void errorLog(String getLine){
		String localIp="";
		settingLogPath(); // LOG_PATH를 property파일에서 가져옴
		try {
			//System.out.println("!!!!! 아이피 : " + InetAddress.getLocalHost().getHostAddress());
			localIp = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
		}
        try{
            BufferedWriter fw = new BufferedWriter(new FileWriter(LOG_PATH+localIp+"_"+"errFileList.txt", true));
            
            // 파일안에 문자열 쓰기
            fw.write(getLine, 0, getLine.length());
            fw.newLine();
 
            // 객체 닫기
            if(fw != null){
            	fw.close(); 
            	fw = null;
            }
            getLine = null;
            localIp = null;
        }catch(Exception e){
            e.printStackTrace();
        }
	}
	
	public void settingLogPath(){
		File propertyFile = new File(AIUtil.propertyPath);
		Properties getProps = new Properties();
		InputStream propertyFis;
		try {
			propertyFis = new FileInputStream(propertyFile);
			getProps.load(propertyFis);
		}catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		LOG_PATH = getProps.getProperty("log.path");
		if(propertyFis != null) propertyFis = null;
		if(propertyFile != null) propertyFile = null;
		getProps.clear();
	}
	
	public  String workLogUpdate(int target,int workNum, int state , String result , String percentage, String error){
		if(percentage!=null) percentage = "'"+percentage+"'";
		if(error!=null) error = "'"+error+"'";
		
		String query = "";
		SimpleDateFormat formatter = new SimpleDateFormat ( "yyyyMMdd_HHmmss", Locale.KOREA );
		Date currentTime = new Date ( );
		String dTime = formatter.format ( currentTime );
		String[] currentDayTime = dTime.split("_");
		
		if(currentDayTime[1].length() == 5) currentDayTime[1] += "0"+currentDayTime[1];
		
		if(target==1){ // 진행 상태 업데이트
			query = "update worklog set DATE='"+currentDayTime[0]+"',TIME='"+currentDayTime[1]+"', state = "+state+" , result = '"+result+"'  , percentage = "+percentage+" , error_comment = "+error+" where id = "+workNum;
		}else if(target==2){ // ftp 에러 업데이트
			percentage = percentage.replaceAll("'", "");
			query = "update worklog set DATE='"+currentDayTime[0]+"',TIME='"+currentDayTime[1]+"', state = "+state+" , result = '"+result+"'  , error_num = '"+percentage+"'  , error_comment = "+error+" where id = "+workNum;
		}
		return query;
	}
	
}
