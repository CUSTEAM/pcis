package quartz.dilg;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import model.Mail;
import model.MailReceiver;
import quartz.BaseJob;

/**
 * 曠課通知
 * @author John
 *
 */
public class Abs2Alert extends BaseJob{
	
	
	
	public void doit(){	
		//StudAffairManager sam= (StudAffairManager) springContext.getBean("StudAffairManager");	
		List<Map>empls=df.sqlGet("SELECT e.idno, e.cname, e.Email FROM Class c, empl e WHERE c.tutor=e.idno GROUP BY e.idno");		
    	
		List<Map>tmp;
		StringBuilder sb;
		List<Map>std;
		
		StringBuilder a=new StringBuilder();
		//StringBuilder f=new StringBuilder();
		
		SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd");	
		Date school_term_begin;
		Date school_term_end;
		Date today=new Date();
		
		try {
			school_term_begin = sf.parse(sam.school_term_begin());
			school_term_end=sf.parse(sam.school_term_end());
		} catch (ParseException e1) {			
			school_term_begin = today;
			school_term_end=today;
		}
		
		//若為開學期間
		if(today.getTime()>=school_term_begin.getTime() && today.getTime()<=school_term_end.getTime()){
			for(int i=0; i<empls.size(); i++){
				sb=new StringBuilder(empls.get(i).get("cname")+"老師您好<br><br>您的導師班級缺曠記錄如下:<br><br>"    );
				tmp=sam.getDataFinder().sqlGet("SELECT c.ClassNo, c.ClassName FROM Class c WHERE c.tutor='"+empls.get(i).get("idno")+"'");			
				for(int j=0; j<tmp.size(); j++){
					std=sam.getDataFinder().sqlGet("SELECT s.student_no, s.student_name, COUNT(*)as cnt FROM Dilg d, stmd s WHERE " +
					"d.abs='2' AND d.student_no=s.student_no AND s.depart_class='"+tmp.get(j).get("ClassNo")+"' GROUP BY s.student_no ORDER BY cnt DESC ");
					sb.append(tmp.get(j).get("ClassName")+"<br>"   );				
					for(int k=0; k<std.size(); k++){					
						sb.append(std.get(k).get("student_no")+" "+std.get(k).get("student_name")+", 累計: "+std.get(k).get("cnt")+"節曠課<br>");					
					}				
				}
				
				Mail m=new Mail();					
				m.setContent(sb.toString());
				m.setFrom_addr("CIS@cc.cust.edu.tw");
				m.setSender("中華科技大學資訊系統");
				m.setSubject("班級缺曠記錄通知");
				if(!isDebug){
					m.setSend("0");
				}else{
					m.setSend("1");
				}
				df.update(m);
				
				MailReceiver r=new MailReceiver();
				r.setMail_oid(m.getOid());
				r.setAddr(empls.get(i).get("Email").toString());
				r.setName(empls.get(i).get("cname").toString());
				r.setType("to");									
				df.update(r);
				a.append(empls.get(i).get("cname")+", ");
			}			
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('班級缺曠記錄通知','寄給:"+a+"共"+empls.size()+"位老師');");
		}else{
			df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('班級缺曠記錄通知','非點名期間');");
		}
		//springContext.registerShutdownHook();		
	}
}
