package etc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import model.OracleAttachFileList;
import model.OracleTBRDRecord;
import model.OracleTBStorg;

public class DBConnect {
	
	Connection conn;
	PreparedStatement psmt;
	ResultSet rs;
	
	OutputProcess op = new OutputProcess();
	
	
	public void initDB(String dbType, Properties props){
		try {
			if("mysql".equals(dbType)){
				
				Class.forName(props.getProperty("mysql.jdbc.driverClassName"));
				
				conn = null;
				conn = DriverManager.getConnection(
						props.getProperty("mysql.jdbc.url"),
						props.getProperty("mysql.jdbc.username"),
						props.getProperty("mysql.jdbc.password"));
				System.out.println("Mysql DB Connect..");
			}else if("oracle".equals(dbType)){
				Class.forName(props.getProperty("oracle.jdbc.driverClassName"));
				
				conn = null;
				conn = DriverManager.getConnection(
						props.getProperty("oracle.jdbc.url"),
						props.getProperty("oracle.jdbc.username"),
						props.getProperty("oracle.jdbc.password"));
				System.out.println("Oracle DB Connect..");
			}else{
				System.out.println("Err : InitDB Type Input Error");
			}
		
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insert(String query){
		try {
			psmt = null;
			psmt = conn.prepareStatement(query);
			psmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("오류남 : " + e);
			//OutputProcess.errorLog("Err Query | "+query);
			if(query.length() >= 300)
				query = query.substring(0,300);
			op.errorLog("Err Query | "+e );
			op.errorLog("\t\t"+query + " ...");
		} 
	}

	public int workLogGetId(String workNM){
		int idNum = 0;
		try {
			psmt = null;
			psmt = conn.prepareStatement("SELECT ID FROM worklog where WORK_NM = '"+workNM
					+"' and state=0 and PID is null "
					+ "order by id desc" );
			rs = null;
			rs = psmt.executeQuery();
			while (rs.next()) {
				idNum = rs.getInt(1);
				break;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return idNum;
	}

	public ArrayList<OracleTBStorg> oracle_TB_STORG_List(String cyoc){
		ArrayList<OracleTBStorg> otbStoreList = new ArrayList<OracleTBStorg>();
		try {
			psmt = null;
			System.out.println("Make TB_STORG List..");
			psmt = conn.prepareStatement("select * from TB_STORG");
			rs = null;
			rs = psmt.executeQuery();
			OracleTBStorg otbs = null;
			String recordCenterId;
			String orgCd;
			String orgNm;
			String orgAbbrNm;
			String fullOrgNm;
			String hupperOrgCd;
			String upperOrgCd;
			String levl;
			String orgSeq;
			String closeFlag;
			String applyStartYmd;
			String applyCloseYmd;
			String takeOrgFlag;
			String linkTrgtId;
			String takeTakovrMngNo;
			int count = 0;
			while (rs.next()) {
				otbs = new OracleTBStorg();
				recordCenterId = rs.getString(1);
				orgCd = rs.getString(2);
				orgNm = rs.getString(3);
				orgAbbrNm = rs.getString(4);
				fullOrgNm = rs.getString(5);
				hupperOrgCd = rs.getString(6);
				upperOrgCd = rs.getString(7);
				levl = rs.getString(8);
				orgSeq = rs.getString(9);
				closeFlag = rs.getString(10);
				applyStartYmd = rs.getString(11);
				applyCloseYmd = rs.getString(12);
				takeOrgFlag = rs.getString(13);
				linkTrgtId = rs.getString(14);
				takeTakovrMngNo = rs.getString(15);
				System.out.println("   ~~" + orgCd + " , " + orgNm);
				otbs.setApplyCloseYmd(applyCloseYmd);
				otbs.setApplyStartYmd(applyStartYmd);
				otbs.setCloseFlag(closeFlag);
				otbs.setFullOrgNm(fullOrgNm);
				otbs.setHupperOrgCd(hupperOrgCd);
				otbs.setLevl(levl);
				otbs.setLinkTrgtId(linkTrgtId);
				otbs.setOrgAbbrNm(orgAbbrNm);
				otbs.setOrgCd(upperOrgCd);
				otbs.setOrgNm(fullOrgNm);
				otbs.setOrgSeq(orgSeq);
				otbs.setRecordCenterId(recordCenterId);
				otbs.setTakeOrgFlag(takeOrgFlag);
				otbs.setTakeTakovrMngNo(takeTakovrMngNo);
				otbs.setUpperOrgCd(upperOrgCd);
				otbStoreList.add(otbs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return otbStoreList;
	}
	
	
	public ArrayList<OracleAttachFileList> oracle_attachFileList(String rootPath, String cyoc){
		ArrayList<OracleAttachFileList> oafList = new ArrayList<OracleAttachFileList>();
		try {
			psmt = null;
			//psmt = conn.prepareStatement("SELECT MEDIUM_PATH FROM TB_STMEDIUM C");
			System.out.println("Make AttachFileList..");
			
			// 인천남구청 테스트
			/*
			psmt = conn.prepareStatement(
					"SELECT DISTINCT A.STORE_FILE_NM, ((SELECT MEDIUM_PATH FROM TB_STMEDIUM C WHERE C.MEDIUM_ID = A.MEDIUM_ID AND C.RECORD_CENTER_ID = A.RECORD_CENTER_ID ) || '/' || 'orign/' || CASE WHEN A.FILE_PATH_RULE = '1' THEN SUBSTR (A.ORIGN_ID, 3, 4) || '/' || SUBSTR (A.ORIGN_ID, 7, 4) || '/' || SUBSTR (A.ORIGN_ID, 11, 4) ELSE SUBSTR (A.ORIGN_ID, 3, 10) END || '/' || B.ERECORD_FLAG || '-' || B.RECORD_ID ) AS PATH ,B.RECORD_ID,(SELECT CREAT_YYYY  FROM tb_rdrecord  WHERE RECORD_ID = B.RECORD_ID) As creat_YYYY "
					+ "FROM TB_STORIGNFILE A, TB_STRECORDORIGN B "
					+ "WHERE A.RECORD_CENTER_ID = B.RECORD_CENTER_ID "
					+ "AND A.ORIGN_ID = B.ORIGN_ID "
					+ "AND INSTR(STORE_FILE_NM,'.',-1,1) IS NOT NULL "
					+ "and A.orign_id in (SELECT orign_id   FROM tb_rdrecord  WHERE creat_YYYY = 2014 AND org_cd = '3510140')"
					);
			*/
			// 강릉 2009, 2010 년도
			psmt = conn.prepareStatement(
					"SELECT DISTINCT A.STORE_FILE_NM, ((SELECT MEDIUM_PATH FROM TB_STMEDIUM C WHERE C.MEDIUM_ID = A.MEDIUM_ID AND C.RECORD_CENTER_ID = A.RECORD_CENTER_ID ) || '/' || 'orign/' || CASE WHEN A.FILE_PATH_RULE = '1' THEN SUBSTR (A.ORIGN_ID, 3, 4) || '/' || SUBSTR (A.ORIGN_ID, 7, 4) || '/' || SUBSTR (A.ORIGN_ID, 11, 4) ELSE SUBSTR (A.ORIGN_ID, 3, 10) END || '/' || B.ERECORD_FLAG || '-' || B.RECORD_ID ) AS PATH ,B.RECORD_ID,(SELECT CREAT_YYYY  FROM tb_rdrecord  WHERE RECORD_ID = B.RECORD_ID) As creat_YYYY , (SELECT org_cd FROM tb_rdrecord WHERE RECORD_ID = B.RECORD_ID) AS org_cd , A.FILE_NM "
					+ "FROM TB_STORIGNFILE A, TB_STRECORDORIGN B "
					+ "WHERE A.RECORD_CENTER_ID = B.RECORD_CENTER_ID "
					+ "AND A.ORIGN_ID = B.ORIGN_ID "
					+ "AND INSTR(STORE_FILE_NM,'.',-1,1) IS NOT NULL "
					+ "and A.orign_id in (SELECT orign_id   FROM tb_rdrecord  WHERE 1=1 and creat_YYYY between 2009 and 2010)"
					);
			
			rs = null;
			rs = psmt.executeQuery();
			OracleAttachFileList oaf = null;
			String localFilePath = "";
			String serverFilePath = "";
			String fileName = "";
			String recordId = "";
			String createYYYY = "";
			String orgCd = "";
			String localFileNm = "";
			int count = 0;
			while (rs.next()) {
				oaf = new OracleAttachFileList();
				fileName = rs.getString(1);
				serverFilePath = rs.getString(2) + "/"; // 마지막 / 필요
				recordId = rs.getString(3);
				createYYYY = rs.getString(4);
				orgCd = rs.getString(5);
				localFileNm = rs.getString(6);
				localFilePath = rootPath+"/"+createYYYY+"/"+orgCd+"/"+recordId+"";
				System.out.println("   ~~" + localFilePath);
				oaf.setLocalFilePath(localFilePath);
				oaf.setServerFilePath(serverFilePath);
				oaf.setFileName(fileName);
				oaf.setRecordId(recordId);
				oaf.setLocalFileName(localFileNm);
				oafList.add(oaf);
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return oafList;
	}
	
	public ArrayList<OracleTBRDRecord> oracle_TB_RDRECORD_List(String cyoc){
		ArrayList<OracleTBRDRecord> otbrdRecordList = new ArrayList<OracleTBRDRecord>();
		try {
			psmt = null;
			System.out.println("Make TB_RDRECORD List..");
			
			// 인천남구청 테스트
			psmt = conn.prepareStatement(
					"SELECT A.RECORD_ID,A.TITLE,A.OPEN_DIV_CD,A.OPEN_GRADE,A.PART_OPEN_RSN,A.OPEN_LIMIT_PART,A.ORG_CD,(select org_nm from tb_storg where org_cd = A.org_cd ) ORG_NM "
					+ "FROM TB_RDRECORD A "
					+ "WHERE creat_YYYY = 2014 AND org_cd = '3510140'");
			
			// 강릉 2009, 2010
			psmt = conn.prepareStatement(
					"SELECT A.RECORD_ID,A.TITLE,A.OPEN_DIV_CD,A.OPEN_GRADE,A.PART_OPEN_RSN,A.OPEN_LIMIT_PART,A.ORG_CD,(select org_nm from tb_storg where org_cd = A.org_cd ) ORG_NM "
					+ "FROM TB_RDRECORD A "
					+ "WHERE creat_YYYY = 2009 OR creat_YYYY = 2010");
			
			rs = null;
			rs = psmt.executeQuery();
			OracleTBRDRecord orf = null;
			String recordId;
			String title;
			String openDivCd;
			String openGrade;
			String partOpenRsn;
			String openLimitPart;
			String orgCd;
			String orgNm;
			int count = 0;
			while (rs.next()) {
				orf = new OracleTBRDRecord();
				recordId = rs.getString(1);
				title = rs.getString(2).replaceAll("'", "").replaceAll("\\\\", "￦"); 
				openDivCd = rs.getString(3);
				openGrade = rs.getString(4);
				partOpenRsn = rs.getString(5);
				openLimitPart = rs.getString(6);
				orgCd = rs.getString(7);
				orgNm = rs.getString(8);
				System.out.println("   ~~" + recordId + " , " + title);
				orf.setRecordId(recordId);
				orf.setTitle(title);
				orf.setOpenDivCd(openDivCd);
				orf.setOpenGrade(openGrade);
				orf.setPartOpenRsn(partOpenRsn);
				orf.setOpenLimitPart(openLimitPart);
				orf.setOrgCd(orgCd);
				orf.setOrgNm(orgNm);
				otbrdRecordList.add(orf);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return otbrdRecordList;
	}
	

	public void closeDB(){
		try {
			if(rs != null){
				rs.close();
				rs = null;
			}
			if(psmt != null){
				psmt.close();
				psmt = null;
			}
			if(conn != null){
				conn.close();
				conn = null;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void closeExceptProps(){
		try {
			if(rs != null){
				rs.close();
				rs = null;
			}
			if(psmt != null){
				psmt.close();
				psmt = null;
			}
			if(conn != null){
				conn.close();
				conn = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int getSelectCount(String query){
		int count = 0;
		try {
			psmt = null;
			psmt = conn.prepareStatement(query);
			rs = null;
			rs = psmt.executeQuery();
			while (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}
	
	
	
}
