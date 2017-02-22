package quartz.sys;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import service.impl.base.BaseAccessImpl;
import service.impl.base.BaseLiteralImpl;

/**
 * 資料表關連維護
 * @author John
 *
 */
public class MaintainTable {
	
	ApplicationContext springContext;
	
	public MaintainTable(ApplicationContext springContext){
		this.springContext=springContext;
	}	
	public void doit(){	
		
		BaseAccessImpl df= (BaseAccessImpl) springContext.getBean("DataManager");		
		StringBuilder sb=new StringBuilder();
		int cnt;
		List<Map>list;
		String school_year=df.sqlGetStr("SELECT Value FROM Parameter WHERE Name='School_year'");
		String school_term=df.sqlGetStr("SELECT Value FROM Parameter WHERE Name='School_term'");		
		SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date school_term_begin;
		try {
			school_term_begin = sf.parse(df.sqlGetStr("SELECT cdate FROM SYS_CALENDAR WHERE Name='school_term_begin'"));
		} catch (ParseException e1) {
			school_term_begin=new Date();
		}
		//Date school_term_end=sf.parse(df.sqlGetStr("SELECT cdate FROM SYS_CALENDAR WHERE Name='school_term_end'"));		
		Calendar stb=Calendar.getInstance();
		stb.setTime(school_term_begin);
		
		

		//BATCH_DILG_CLASS
		try{//統計目前學生
			//df.exSql("DELETE FROM ");
			df.exSql("UPDATE Class SET stds=(SELECT COUNT(*)FROM stmd WHERE depart_class=Class.ClassNo)");
			stb=Calendar.getInstance(); 
			Calendar stb1=Calendar.getInstance();			
			df.exSql("UPDATE Class SET stds=(SELECT COUNT(*)FROM stmd WHERE depart_class=Class.ClassNo)");
			df.exSql("DELETE FROM BATCH_DILG_CLASS");			
			//統計每週人數與缺曠
			List<Map>cls=df.sqlGet("SELECT ClassNo FROM Class WHERE stds>0");			
			for(int i=0; i<cls.size(); i++){
				stb.setTime(sf.parse(sf.format(school_term_begin)));		
				stb1.setTime(sf.parse(sf.format(school_term_begin)));
				stb1.add(Calendar.DAY_OF_YEAR, 7);
				for(int j=1; j<=18; j++){
					df.exSql("DELETE FROM BATCH_DILG_CLASS WHERE week="+j+" AND ClassNo='"+cls.get(i).get("ClassNo")+"'");
					df.exSql("INSERT INTO BATCH_DILG_CLASS(ClassNo,week,stds,dilgs)SELECT ClassNo, "+j+", stds,"
					+ "(SELECT COUNT(*)FROM Dilg WHERE abs<5 AND date>='"+sf.format(stb.getTime())+"'AND date<'"+sf.format(stb1.getTime())+"'AND "
					+ "student_no IN(SELECT student_no FROM stmd WHERE depart_class=Class.ClassNo))"
					+ "FROM Class WHERE ClassNo='"+cls.get(i).get("ClassNo")+"'");								
					stb.add(Calendar.DAY_OF_YEAR, 7);
					stb1.add(Calendar.DAY_OF_YEAR, 7);
				}				
			}			
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('BATCH_DILG_CLASS表Class表人數維護','完成');");
			System.out.println("BATCH_DILG_CLASS完成");
		}catch(Exception e){
			e.printStackTrace();
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('BATCH_DILG_CLASS表Class表人數維護','失敗');");
		}
		
		
		//wwpass維護
		try{			
			//學生
			list=df.sqlGet("SELECT student_no, idno FROM stmd WHERE student_no NOT IN(SELECT username FROM wwpass)");
			for(int i=0; i<list.size(); i++){
				df.exSql("INSERT INTO wwpass(username, password, priority)VALUES('"+list.get(i).get("student_no")+"','"+list.get(i).get("idno")+"', 'C');");
			}
			sb.append("建立 "+list.size()+"個學生帳號<br>");
			sb.append("刪除 "+df.sqlGetInt("SELECT COUNT(*) FROM wwpass WHERE priority='C' AND username NOT IN(SELECT student_no FROM stmd)")+"個學生帳號<br>");
			df.exSql("DELETE FROM wwpass WHERE priority='C' AND username NOT IN(SELECT student_no FROM stmd)");
			//教職員
			BaseLiteralImpl bl = (BaseLiteralImpl)springContext.getBean("BaseLiteralImpl");
			list=df.sqlGet("SELECT idno, bdate FROM empl WHERE idno NOT IN(SELECT username FROM wwpass)");
			for(int i=0; i<list.size(); i++){
				df.exSql("INSERT INTO wwpass(username, password, priority)VALUES('"+
				list.get(i).get("idno")+"','"+
				bl.getBirthdayNum(list.get(i).get("bdate").toString())+
				"', 'A');");
			}
			sb.append("建立 "+list.size()+"個教職員帳號<br>");
			sb.append("刪除 "+df.sqlGetInt("SELECT COUNT(*) FROM wwpass WHERE priority='A' AND username NOT IN(SELECT idno FROM empl)")+"個教職員帳號");
			df.exSql("DELETE FROM wwpass WHERE priority='A' AND username NOT IN(SELECT idno FROM empl)");
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('帳號維護','"+sb.toString()+"');");			
			
		}catch(Exception e){
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('帳號維護','失敗');");
		}
		try{
			df.exSql("INSERT INTO CardNo(uid,cid)SELECT username, inco FROM wwpass ON DUPLICATE KEY UPDATE cid=inco");
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('悠遊卡內碼備份','完成');");
		}catch(Exception e){
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('悠遊卡內碼備份','失敗');");
		}		
		
		//Seld維護
		sb=new StringBuilder(df.sqlGetStr("SELECT COUNT(*) FROM Seld WHERE student_no NOT IN(SELECT student_no FROM stmd)"));
		try{		
			df.exSql("DELETE FROM Seld WHERE student_no NOT IN(SELECT student_no FROM stmd)");
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('刪除休退學選課記錄','已刪除"+sb+"筆');");
		}catch(Exception e){
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('刪除休退學選課記錄','失敗');");
		}		
		
		//Dilg維護
		sb=new StringBuilder(df.sqlGetStr("SELECT COUNT(*) FROM Dilg WHERE student_no NOT IN(SELECT student_no FROM Seld WHERE Dtime_oid=Dilg.Dtime_oid)"));
		try{		
			df.exSql("DELETE FROM Dilg WHERE student_no NOT IN(SELECT student_no FROM Seld WHERE Dtime_oid=Dilg.Dtime_oid)");
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('刪除退選缺課記錄','已刪除"+sb+"筆');");
		}catch(Exception e){
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('刪除退選缺課記錄','失敗');");
		}
		
		try{
			sb=new StringBuilder(df.sqlGetStr("SELECT COUNT(*) FROM Dilg  WHERE Dilg_app_oid NOT IN (SELECT Oid FROM Dilg_apply)"));
			df.exSql("UPDATE Dilg  SET Dilg_app_oid=null WHERE Dilg_app_oid NOT IN (SELECT Oid FROM Dilg_apply)");
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('無假單指標缺課記錄','已清除"+sb+"筆');");
		}catch(Exception e){
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('清除無假單指標缺課記錄','失敗');");
		}
		
		//Dilg_apply維護
		try{
			list=df.sqlGet("SELECT da.Oid FROM Dilg_apply da LEFT OUTER JOIN Dilg d ON da.Oid=d.Dilg_app_oid WHERE d.Oid IS NULL");
			for(int i=0; i<list.size(); i++){
				df.exSql("DELETE FROM Dilg_apply WHERE Oid="+list.get(i).get("Oid"));
			}
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('刪除學生空白假單','已刪除"+list.size()+"筆');");
		}catch(Exception e){
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('刪除學生空白假單','失敗');");
		}
		
		//Just維護
		try{
			df.exSql("DELETE FROM Just WHERE student_no NOT IN (SELECT student_no FROM stmd);");
			df.exSql("INSERT INTO Just (student_no) SELECT stmd.student_no FROM stmd ON DUPLICATE KEY UPDATE Just.student_no=Just.student_no;");
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('Just表維護','完成');");
		}catch(Exception e){
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('Just表維護','失敗');");
		}
		
		//Class維護
		/*try{
			cnt=0;
			list=df.sqlGet("SELECT ClassNo FROM Class WHERE Type='P'");
			for(int i=0; i<list.size(); i++){
				if(df.sqlGetInt("SELECT COUNT(*)FROM stmd WHERE depart_class='"+list.get(i).get("ClassNo")+"'")<1){
					cnt++;
					df.exSql("UPDATE Class SET Type='O'WHERE ClassNo='"+list.get(i).get("ClassNo")+"'");
				}
			}
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('廢止班級維護','轉換"+cnt+"個班為廢止班級');");
		}catch(Exception e){
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('廢止班級維護','失敗');");
		}*/
		
		//BATCH_SELD_DILG_STAT 維護
		try{								
			stb.add(Calendar.WEEK_OF_YEAR, 8);
			df.exSql("DELETE FROM BATCH_SELD_DILG_STAT");			
			df.exSql("INSERT INTO BATCH_SELD_DILG_STAT(student_no,Dtime_oid,dilg,dilg8,score,score1,score2,score3)"
			+ "SELECT s.student_no,s.Dtime_oid,(SELECT COUNT(*)FROM Dilg, Dilg_rules WHERE Dilg.abs=Dilg_rules.id AND "
			+ "Dilg.student_no=s.student_no AND Dilg.Dtime_oid=dt.Oid)as dilg,(SELECT COUNT(*)FROM Dilg, Dilg_rules WHERE "
			+ "Dilg.date<'"+sf.format(stb.getTime())+"'AND Dilg.abs=Dilg_rules.id AND Dilg.student_no=s.student_no AND "
			+ "Dilg.Dtime_oid=dt.Oid)as dilg8,s.score,s.score1,s.score2,s.score3 FROM Seld s, Dtime dt WHERE dt.Sterm='"+
			school_term+"'AND dt.Oid=s.Dtime_oid GROUP BY s.student_no,s.Dtime_oid");					
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('BATCH_SELD_DILG_STAT表維護','完成');");
		}catch(Exception e){
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('BATCH_SELD_DILG_STAT表維護','失敗');");
		}
		
		//BATCH_DTIME_DILG_STAT 維護
		try{
			df.exSql("DELETE FROM BATCH_DTIME_DILG_STAT");
			df.exSql("INSERT INTO BATCH_DTIME_DILG_STAT(Dtime_oid,dilg_avg,dilg_avg8,score_avg,score1_avg,score2_avg,score3_avg)"
			+ "SELECT d.Oid,AVG(s.dilg)as dilg,AVG(s.dilg8)as dilg8,AVG(s.score)as score,AVG(s.score1)as score1,AVG(s.score2)as score2,"
			+ "AVG(s.score3)as score3 FROM BATCH_SELD_DILG_STAT s, Dtime d WHERE d.Sterm='"+school_term+"'AND d.Oid=s.Dtime_oid GROUP BY d.Oid");
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('BATCH_DTIME_DILG_STAT表維護','完成');");
		}catch(Exception e){
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('BATCH_DTIME_DILG_STAT表維護','失敗');");
		}		
		
		//BATCH_SELD_STAT維護
		try{
			df.exSql("DELETE FROM BATCH_SELD_STAT");
			df.exSql("INSERT INTO BATCH_SELD_STAT(student_no,credit,thour,selds,failed_score2,failed_credit2,failed_score,failed_credit)"
					+ "SELECT stmd.student_no,SUM(Dtime.credit)as credit,SUM(Dtime.thour)as thour,COUNT(*)as selds,"
					+ "IFNULL(SUM(CASE WHEN Seld.score2<60 THEN 1 ELSE NULL END),0)as failed_score2,"
					+ "IFNULL(SUM(CASE WHEN Seld.score2<60 THEN Dtime.thour ELSE NULL END),0)as failed_credit2,"
					+ "IFNULL(SUM(CASE WHEN Seld.score<60 THEN 1 ELSE NULL END),0)as failed_score,"
					+ "IFNULL(SUM(CASE WHEN Seld.score<60 THEN Dtime.thour ELSE NULL END),0)as failed_credit "
					+ "FROM stmd, Seld, Dtime WHERE stmd.student_no=Seld.student_no AND "
					+ "Seld.Dtime_oid=Dtime.Oid AND Dtime.Sterm='"+school_term+"'GROUP BY stmd.student_no");
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('BATCH_SELD_STAT表維護','完成');");
		}catch(Exception e){
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('BATCH_SELD_STAT表維護','失敗');");
		}
		
		
		//班會任課老師維護
		try{			
			df.exSql("UPDATE Dtime SET techid=(SELECT tutor FROM Class WHERE ClassNo=Dtime.depart_class) WHERE cscode='50000' AND Sterm='"+school_term+"'");			
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('班會任課老師維護','完成');");
		}catch(Exception e){
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('班會任課老師維護','失敗');");
		}
		
		
		//Seld學分時數重計
		try{
			//刪除無Dtime_oid的Seld
			df.exSql("DELETE FROM Seld WHERE Dtime_oid NOT IN(SELECT Oid FROM Dtime)");
			//將Dtime資訊複製Seld
			df.exSql("UPDATE Seld SET credit=(SELECT credit FROM Dtime WHERE Oid=Seld.Dtime_oid),"
			+ "stdepart_class=(SELECT depart_class FROM stmd WHERE student_no=Seld.student_no),"
			+ "csdepart_class=(SELECT depart_class FROM Dtime WHERE Oid=Seld.Dtime_oid),"
			+ "cscode=(SELECT cscode FROM Dtime WHERE Oid=Seld.Dtime_oid),"
			+ "opt=(SELECT opt FROM Dtime WHERE Oid=Seld.Dtime_oid)");
			//碩士班學分歸0
			df.exSql("UPDATE Seld SET credit=0 WHERE student_no IN(SELECT student_no FROM stmd st, Class c WHERE "
			+ "st.depart_class=c.ClassNo AND c.SchNo='M')AND Dtime_oid IN(SELECT d.Oid FROM Dtime d, Class c1 WHERE d.depart_class=c1.ClassNo AND c1.SchNo!='M')");
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('Seld學分時數重計','完成');");
		}catch(Exception e){
			
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('Seld學分時數重計','失敗:"+e+"');");
		}
		
		
		//教學評量重新計算
		try{			
			df.exSql("UPDATE Dtime SET effsamples=0, samples=0, coansw=0");
			df.exSql("UPDATE Seld SET coansw_invalid=null");		
			//尋找偵錯題與被偵錯題 TODO 動態定位
			List<Map>c=df.sqlGet("SELECT Oid, Dtime_oid, SUBSTRING(coansw, 1, 10)as ans,"
			+ "SUBSTRING(coansw, 4, 1) as que, SUBSTRING(coansw, 11, 1)as bug FROM Seld WHERE coansw IS NOT NULL");		
			int que, bug, abs;		
			for(int i=0; i<c.size(); i++){
				que=Integer.parseInt(c.get(i).get("que").toString());//計分被偵錯題
				bug=Integer.parseInt(c.get(i).get("bug").toString());//不計分偵錯題			
				if((que==3 && bug==3)){
					//有效
					df.exSql("UPDATE Dtime SET samples=samples+1, effsamples=effsamples+1, coansw=coansw+"+sum(c.get(i).get("ans").toString())+" WHERE Oid="+c.get(i).get("Dtime_oid"));
					c.get(i).put("check", true);
					
				}else{				
					//排除
					if(c.get(i).get("ans").toString().equals("1111111111")){
					//if(c.get(i).get("ans").toString().equals("1111111111")||c.get(i).get("ans").toString().equals("2222222222")||c.get(i).get("ans").toString().equals("3333333333")||c.get(i).get("ans").toString().equals("4444444444")||c.get(i).get("ans").toString().equals("5555555555")){
						//無效
						df.exSql("UPDATE Dtime SET samples=samples+1 WHERE Oid="+c.get(i).get("Dtime_oid"));
						df.exSql("UPDATE Seld SET coansw_invalid='*'WHERE Oid="+c.get(i).get("Oid"));
						c.get(i).put("check", false);
						continue;
					}				
					abs=Math.abs(que-bug);
					if(abs>1){
						//有效
						df.exSql("UPDATE Dtime SET samples=samples+1, effsamples=effsamples+1, coansw=coansw+"+sum(c.get(i).get("ans").toString())+" WHERE Oid="+c.get(i).get("Dtime_oid"));
						c.get(i).put("check", true);
					}else{
						//無效
						df.exSql("UPDATE Dtime SET samples=samples+1 WHERE Oid="+c.get(i).get("Dtime_oid"));
						df.exSql("UPDATE Seld SET coansw_invalid='*'WHERE Oid="+c.get(i).get("Oid"));
						c.get(i).put("check", false);
					}
				}			
			}
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('教學評量重計','完成');");
		}catch(Exception e){
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('教學評量重計','失敗:"+e+"');");
		}		
		
		
	}
	
	
	private Float sum(String ans){		
		int s=0;
		for(int i=0; i<ans.length(); i++){
			s+=Integer.parseInt(ans.substring(i, i+1));
		}		
		return (float)s/ans.length();
	}

}