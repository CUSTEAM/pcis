package quartz.dilg;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import quartz.BaseJob;
import service.impl.StudAffairManager;
import service.impl.base.BaseIOImpl;
import service.impl.base.BaseLiteralImpl;

/**
 * 曠課通知
 * @author John
 *
 */
public class Abs2Alert extends BaseJob{
	
	ApplicationContext springContext;
	
	public Abs2Alert(ApplicationContext springContext){
		this.springContext=springContext;
	}	
	
	public void doit(){	
		StudAffairManager sam= (StudAffairManager) springContext.getBean("StudAffairManager");	
		List<Map>empls=sam.getDataFinder().sqlGet("SELECT e.idno, e.cname, e.Email FROM Class c, empl e WHERE c.tutor=e.idno GROUP BY e.idno");		
    	
		List tmp;
		StringBuilder sb;
		List std;
		
		StringBuilder a=new StringBuilder();
		StringBuilder f=new StringBuilder();
		
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
					"d.abs='2' AND d.student_no=s.student_no AND s.depart_class='"+((Map)tmp.get(j)).get("ClassNo")+"' GROUP BY s.student_no ORDER BY cnt DESC ");
					sb.append( ((Map)tmp.get(j)).get("ClassName")+"<br>"   );				
					for(int k=0; k<std.size(); k++){					
						sb.append(((Map)std.get(k)).get("student_no")+" "+((Map)std.get(k)).get("student_name")+", 累計: "+((Map)std.get(k)).get("cnt")+"節曠課<br>");					
					}				
				}
				
				try {
					this.sendMail(empls.get(i).get("Email").toString(), "班級缺曠記錄通知", sb.toString());
					a.append(empls.get(i).get("cname")+", ");
				} catch (Exception e) {	
					f.append(empls.get(i).get("cname")+", ");
					e.printStackTrace();
				}		
			}
			sam.getDataFinder().exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('班級缺曠記錄通知','寄送完成:"+a+"<br>寄送失敗:"+f+"');");
		}else{
			sam.getDataFinder().exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('班級缺曠記錄通知','非點名期間');");
		}
		
		
	}
	
	
	
}
