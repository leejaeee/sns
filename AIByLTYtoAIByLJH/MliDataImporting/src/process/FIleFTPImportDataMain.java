package process;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import model.OracleAttachFileList;
import etc.DBConnect;
import etc.OutputProcess;
import etc.AIUtil;


public class FIleFTPImportDataMain {

	static int totalNowWorkCount = 0;
	
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		if(args.length < 1){
			System.out.println("�Ű������� ���� �����մϴ�....");
			System.exit(1);
		}
		
		OutputProcess op = new OutputProcess();
		
		AIUtil.propertyPath  = args[0];
		String dbTable = "mli";
		String workNM = dbTable+"_data_importing";

		
		/** DB ���� */
		File propertyFile = new File(AIUtil.propertyPath);
		Properties props = new Properties();
		InputStream propertyFis;
		
		try {
			propertyFis = new FileInputStream(propertyFile);
			props.load(propertyFis);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		propertyFile = null;
		propertyFis = null;
		
		DBConnect dbConnect = new DBConnect(); 
		dbConnect.initDB("mysql",props);
		
		dbConnect.insert(op.workLog(3, workNM,0,null,null,null)); // Work Log �ۼ�
		dbConnect.insert(op.workLog(3, workNM,0,null,null,null)); // Work Log �ۼ�
		int workNum = dbConnect.workLogGetId(workNM);
		dbConnect.insert(op.workLogUpdate(1, workNum, 1,"����", "0.0" , null));
		
		try {
			/** FTP */
			FtpImportData ds = new FtpImportData(dbTable,"createYYYYOrgCD",props);
			int count = 0;
			int totalSize = ds.getAttachFileList().size();
			
			/** percentage ���� ���� */
			FTPPercentage wp = new FTPPercentage(totalSize, dbConnect, dbTable, workNum, workNM);
			wp.start();
			
			long start = System.currentTimeMillis();
			System.out.println("=============================================================================================");
			for( OracleAttachFileList oaf : ds.getAttachFileList()){
				totalNowWorkCount++;
				count++;
				ds.download(oaf.getLocalFilePath(),oaf.getServerFilePath(),oaf.getFileName(),workNum,totalNowWorkCount);
			}
			System.out.println("=============================================================================================");
			long end = System.currentTimeMillis();
			System.out.println( "���� �ð� : " + ( end - start )/1000.0 );
			
			ds.close_dataSet();
		} catch (Throwable e) {
			String errMsg = "";
			if(e.toString().length()>100){
				errMsg = e.toString().toString().substring(0,100);
			}else{
				errMsg = e.toString().toString();
			}
			dbConnect.insert(op.workLogUpdate(2, workNum, 3,"����", totalNowWorkCount+"" , "Throwable Exception "+errMsg));
			op.errorLog("FTP Err | Throwable Exception " +" number : "+totalNowWorkCount + " , " + errMsg);
		}
		
		
		
	}

}