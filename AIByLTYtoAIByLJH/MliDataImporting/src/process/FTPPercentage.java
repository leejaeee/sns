package process;

import etc.AIUtil;
import etc.DBConnect;
import etc.OutputProcess;

public class FTPPercentage extends Thread{
	boolean stopFlag = false;
	double percentage = 0;
	int total;
	int nowCount;
	int errCount;
	int pid;
	DBConnect db;
	String dbTable;
	String workNM;
	
	OutputProcess op = new OutputProcess();
	
	public FTPPercentage(int total ,DBConnect db ,String dbTable ,int pid ,String workNM) {
		this.total = total;
		this.db = db;
		this.dbTable = dbTable;
		this.pid = pid;
		this.workNM = workNM;
	}
	public void run(){
		while(!stopFlag){
			nowCount = FIleFTPImportDataMain.totalNowWorkCount;
			percentage = AIUtil.percentageCal(nowCount, total);
			errCount = db.getSelectCount("select count(*) from sroc.worklog where id = "+pid+" and ERROR_NUM is not null");
			
			System.out.println(workNM + " �۾���Ȳ : " + percentage +" %");
			
			if(percentage==100 || errCount > 0){
				if(percentage==100){
					db.insert(op.workLogUpdate(1, pid, 2,"�Ϸ�", String.valueOf(percentage) , null)); // Work Log �ۼ�
				}
				/** ftp err ���� ��, ���� �޼����� �Է��ϱ����� download() ���� ó������ 
				 * else if(errCount > 0)*/
				stoped();
			}else{
				db.insert(op.workLogUpdate(1, pid, 1,"����", String.valueOf(percentage) , null));
			}
			
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public void stoped(){
		db.closeDB();
		this.stopFlag = true;
	}
}