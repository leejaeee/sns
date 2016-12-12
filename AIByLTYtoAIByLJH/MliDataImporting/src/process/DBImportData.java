package process;

import java.util.ArrayList;
import java.util.Properties;

import etc.DBConnect;
import model.OracleAttachFileList;
import model.OracleTBRDRecord;
import model.OracleTBStorg;

public class DBImportData {
	DBConnect db = null;
	Properties props = null;
	String dbTable = "";
	String oracleCreateYYYYOrgCd = "";
	ArrayList<OracleAttachFileList> attachFileList = null;
	ArrayList<OracleTBRDRecord> otbrdRecordList = null;
	ArrayList<OracleTBStorg> otbStorgList = null;
	
	public DBImportData(DBConnect db, Properties props, String dbTable, String oracleCreateYYYYOrgCd) {
		this.db = db;
		this.props = props;
		this.dbTable = dbTable;
		this.oracleCreateYYYYOrgCd = oracleCreateYYYYOrgCd;
	}

	public void settingDBOrg(){
		otbStorgList = db.oracle_TB_STORG_List(oracleCreateYYYYOrgCd);
		
		db.closeExceptProps();
		db.initDB("mysql", props);
		
		int count = 0;
		String insertQuery = "";
		String insertQuerySub = "";
		db.insert("delete from tb_storg");
		System.out.println("# TB_STORG delete ......");
		System.out.println("########################################################################");
		insertQuery = "insert into tb_storg (RECORD_CENTER_ID,ORG_CD,ORG_NM,ORG_ABBR_NM,FULL_ORG_NM,HUPPER_ORG_CD,UPPER_ORG_CD,LEVL,ORG_SEQ,CLOSE_FLAG,APPLY_START_YMD,APPLY_CLOSE_YMD,TAKE_ORG_FLAG,LINK_TRGT_ID,TAKE_TAKOVR_MNG_NO) values ";
		insertQuerySub = "";
		for(OracleTBStorg Storg : otbStorgList){
			count++;
			//insertQuerySub += "('" + Storg.getRecordCenterId() + "' , '" + Storg.getOrgCd() + "' , '" + Storg.getOrgNm() + "' , '" + Storg.getOrgAbbrNm()+ "' , '" + Storg.getFullOrgNm() + "' , '" + Storg.getHupperOrgCd() + "' , '" + Storg.getUpperOrgCd() + "' , '" + Storg.getLevl() + "' , '" + Storg.getOrgSeq() + "' , '" + Storg.getCloseFlag() + "' , '" + Storg.getApplyStartYmd() + "' , '" + Storg.getApplyCloseYmd() + "' , '" + Storg.getTakeOrgFlag() + "' , '" + Storg.getLinkTrgtId() + "' , '" + Storg.getTakeTakovrMngNo() + "')";
			insertQuerySub += "(" + Storg.getRecordCenterId() + " , " + Storg.getOrgCd() + " , '" + Storg.getOrgNm() + "' , '" + Storg.getOrgAbbrNm()+ "' , '" + Storg.getFullOrgNm() + "' , " + Storg.getHupperOrgCd() + " , " + Storg.getUpperOrgCd() + " , " + Storg.getLevl() + " , " + Storg.getOrgSeq() + " , " + Storg.getCloseFlag() + " , " + Storg.getApplyStartYmd() + " , " + Storg.getApplyCloseYmd() + " , " + Storg.getTakeOrgFlag() + " , '" + Storg.getLinkTrgtId() + "' , " + Storg.getTakeTakovrMngNo() + ")";
			if(count!=otbStorgList.size()) insertQuerySub += " , ";
		}
		db.insert(insertQuery + insertQuerySub);
		System.out.println("# TB_STORG Table Insert ......");
		System.out.println("########################################################################");
		System.out.println("# TB_STORG List Size: " + count);
		System.out.println("# TB_STORG Table Insert Success !!");
		System.out.println("########################################################################");
		count = 0;
	}
	
	public void settingDBFileList(){
		attachFileList = db.oracle_attachFileList(props.getProperty("ftp.getfile.root.path"), oracleCreateYYYYOrgCd);
		otbrdRecordList = db.oracle_TB_RDRECORD_List(oracleCreateYYYYOrgCd);
		
		db.closeExceptProps();
		db.initDB("mysql", props);
		
		int count = 0;
		String insertQuery = "insert into "+ dbTable +"_attachfile (RECORD_ID , FILE_NM , SERVER_FILEPATH , LOCAL_FILEPATH) values ";
		String insertQuerySub = "";
		for(OracleAttachFileList file : attachFileList){
			count++;
			
			insertQuerySub += "('" + file.getRecordId() + "' , '" + file.getLocalFileName() + "' , '" + file.getServerFilePath() + "' , '" + file.getLocalFilePath() + "')";
			if(count%5000 == 0){
				System.out.println("[" + count + " / " + attachFileList.size() + "]");
				System.out.println("# AttachFile Table Insert ...");
				System.out.println("########################################################################");
				db.insert(insertQuery + insertQuerySub);
				insertQuerySub = "";
			}else if(count==attachFileList.size()){
				System.out.println("[" + count + " / " + attachFileList.size() + "]");
				System.out.println("# AttachFile Table Insert ......");
				System.out.println("########################################################################");
				db.insert(insertQuery + insertQuerySub);
				insertQuerySub = "";
			}else{
				insertQuerySub += " , ";
			}
		}
		
		System.out.println("# AttachFile List Size : " + count);
		System.out.println("# AttachFile Table Insert Success !!");
		System.out.println("########################################################################");
		count = 0;
		
		if(dbTable.indexOf("mli") > -1 || dbTable.indexOf("MLI") > -1)
			// dbTable이 mli 일 경우실행
			insertQuery = "insert into " + dbTable + "_record_openinfo (RECORD_ID,RECORD_TITLE,OPEN_DIV_CD,OPEN_GRADE,PART_OPEN_RSN,OPEN_LIMIT_PART,ORG_NM,ORG_CD) values ";
		else
			insertQuery = "insert into " + dbTable + "_record_openinfo (RECORD_ID,RECORD_TITLE,OPEN_DIV_CD_OLD,OPEN_GRADE_OLD,PART_OPEN_RSN,OPEN_LIMIT_PART,ORG_NM,ORG_CD) values ";
		insertQuerySub = "";
		for(OracleTBRDRecord record : otbrdRecordList){
			count++;
			String partOpenRsn = record.getPartOpenRsn();
			//if("null".equals(partOpenRsn)) partOpenRsn = "null";
			if(record.getPartOpenRsn() == null) partOpenRsn = "null";
			else partOpenRsn = "'"+partOpenRsn+"'";
			
			String openLimitPart = record.getOpenLimitPart();
			if(record.getOpenLimitPart() == null) openLimitPart = "null";
			else openLimitPart = "'"+openLimitPart+"'";
			
			insertQuerySub += "('" + record.getRecordId() + "' , '" + record.getTitle() + "' , '" + record.getOpenDivCd() + "' , '" + record.getOpenGrade() + "' , " + partOpenRsn + " , " + openLimitPart + " , '" + record.getOrgNm() + "' , '" + record.getOrgCd() + "')";
			if(count%5000 == 0){
				System.out.println("[" + count + " / " + otbrdRecordList.size() + "]");
				System.out.println("# OpenInfo Table Insert ...");
				System.out.println("########################################################################");
				db.insert(insertQuery + insertQuerySub);
				insertQuerySub = "";
			}else if(count==otbrdRecordList.size()){
				System.out.println("[" + count + " / " + otbrdRecordList.size() + "]");
				System.out.println("# OpenInfo Table Insert ......");
				System.out.println("########################################################################");
				db.insert(insertQuery + insertQuerySub);
				insertQuerySub = "";
			}else{
				insertQuerySub += " , ";
			}
		}
		
		System.out.println("# OpenInfo List Size : " + count);
		System.out.println("# OpenInfo Table Insert Success !!");
		System.out.println("########################################################################");
		count = 0;
	}
	
	public 	ArrayList<OracleAttachFileList> getAttachFileList(){
		return attachFileList;
	}
	public void dbRevert(){
		db.closeExceptProps();
		db.initDB("oracle", props);
	}
	public void close_attachFileList(){
		attachFileList.clear();
		attachFileList = null;
	}
	public void close_otbrdRecordList(){
		otbrdRecordList.clear();
		otbrdRecordList = null;
	}
	public void close_otbStorgList(){
		otbStorgList.clear();
		otbStorgList = null;
	}
}
