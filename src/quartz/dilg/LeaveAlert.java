package quartz.dilg;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import model.Mail;
import model.MailReceiver;
import quartz.BaseJob;

/**
 * 未核假通知
 * @author John
 *
 */
public class LeaveAlert extends BaseJob{
	
	
	
	public void doit(){		
		//BaseLiteralImpl si= (BaseLiteralImpl) springContext.getBean("BaseLiteralImpl");		
		//BaseAccessImpl df= (BaseAccessImpl) springContext.getBean("DataManager");
		//StudAffairManager sam= (StudAffairManager) springContext.getBean("StudAffairManager");
		List<Map>empl=sam.getDataFinder().sqlGet("SELECT e.idno, e.cname, COUNT(*)as cnu, e.Email FROM empl e, Dilg_apply d WHERE e.idno=d.auditor AND d.result IS NULL GROUP BY e.idno");
		String email;
		List<Map>tmp;
		StringBuilder sb, a,f;
		a=new StringBuilder("寄送完成:");
		f=new StringBuilder("寄送失敗:");
		
		SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd");	
		Date school_term_begin=null;
		Date school_term_end=null;		
		
		//此處取開學期間
		try {
			school_term_begin = sf.parse(sam.school_term_begin());
			school_term_end=sf.parse(sam.school_term_end());
		} catch (ParseException e1) {			
			e1.printStackTrace();
		}
		
		Date today=new Date();
		//若為開學期間
		if(today.getTime()>=school_term_begin.getTime() && today.getTime()<=school_term_end.getTime()){
			for(int i=0; i<empl.size(); i++){
				
					sb=new StringBuilder("<p>"+empl.get(i).get("cname")+"老師您好:</p><br>"  );
					sb.append("<p>目前尚未審核的假單如下:</p>"); 
					
					tmp=sam.getDataFinder().sqlGet("SELECT da.Oid, dr.name, c.ClassName, s.student_no, s.student_name,(SELECT COUNT(*)FROM Dilg, Dilg_apply WHERE " +
					"Dilg.Dilg_app_oid=Dilg_apply.Oid AND Dilg_apply.Oid=da.Oid)as cls FROM Dilg_apply da, stmd s, Class c, Dilg_rules dr  " +
					"WHERE dr.id=da.abs AND c.ClassNo=s.depart_class AND da.result IS NULL AND s.student_no=da.student_no AND da.auditor='"+
					empl.get(i).get("idno")+"'");
					
					for(int j=0; j<tmp.size(); j++){
						sb.append(tmp.get(j).get("name")+" - "+tmp.get(j).get("ClassName")+" - "+tmp.get(j).get("student_name")+", 共"+tmp.get(j).get("cls")+"節<br>");
					}					
					
					email=empl.get(i).get("Email").toString();
	    			if(!bl.validateEmail(email)||email.trim().equals("")||email.trim().length()<10||email==null){  
	    				f.append(empl.get(i).get("cname")+",");
	    				continue;
	    			}			
	    			
	    			
	    			Mail m=new Mail();					
	    			m.setContent(sb.toString());
	    			m.setFrom_addr("CIS@cc.cust.edu.tw");
	    			m.setSender("中華科技大學資訊系統");
	    			m.setSubject("學生請假待審通知");
	    			if(!isDebug){
						m.setSend("0");
					}else{
						m.setSend("1");
					}
	    			df.update(m);
	    			
	    			MailReceiver r=new MailReceiver();
	    			r.setMail_oid(m.getOid());
	    			r.setAddr(empl.get(i).get("Email").toString());
	    			r.setName(empl.get(i).get("cname").toString());
	    			r.setType("to");									
	    			df.update(r);	
	    			
	    			a.append(empl.get(i).get("cname")+",");
				
			}
			
			try {
				sam.getDataFinder().exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('學生請假待審通知','寄給:"+a+"共"+empl.size()+"位老師');");
			} catch (Exception e) {				
				e.printStackTrace();
			} 
		}else{
			sam.getDataFinder().exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('學生請假待審通知','非點名期間');");
		}
		//springContext.registerShutdownHook();
	}
	
	
	
}
