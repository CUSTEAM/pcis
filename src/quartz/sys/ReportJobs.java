package quartz.sys;

import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import model.Mail;
import model.MailReceiver;
import quartz.BaseJob;


/**
 * 排程工作報告
 * @author John
 *
 */
public class ReportJobs extends BaseJob implements Job {		
	
	public void execute(JobExecutionContext context)throws JobExecutionException {
		
		List<Map>members=df.sqlGet("SELECT Email, cname FROM empl WHERE unit='102'");//取本單位
		
		//String host=sam.getDataFinder().sqlGetStr("SELECT Value FROM Parameter p WHERE p.Category='smtp' AND p.Name='mailServer'");
		//String username=df.sqlGetStr("SELECT Value FROM Parameter p WHERE p.Category='smtp' AND p.Name='username'");
		//String password=df.sqlGetStr("SELECT Value FROM Parameter p WHERE p.Category='smtp' AND p.Name='password'");
		
		StringBuilder sb=new StringBuilder();
		
		
		sb.append("系統學年:"+sam.school_year()+" 學年第 "+sam.school_term()+"學期<br>");
		sb.append("開學期間:"+sam.school_term_begin()+" - "+sam.school_term_end()+"<br>");
		sb.append("點名期間:"+sam.RollCall_begin()+" - "+sam.RollCall_end()+"" +
		"<br>--------------------------------------------------<br>");
		
		List<Map>rep=df.sqlGet("SELECT * FROM SYS_SCHEDULE_LOG WHERE send IS NULL");//取未寄送
		for(int i=0; i<rep.size(); i++){			
			sb.append(rep.get(i).get("subject")+"<br>"+rep.get(i).get("note")+"<br>"+rep.get(i).get("exe_time")+
			"<br>--------------------------------------------------<br>");
		}
		df.exSql("UPDATE SYS_SCHEDULE_LOG SET send='1', exe_time=exe_time");//標記全部為已寄送		
		
		Mail m=new Mail();					
		m.setContent(sb.toString());
		m.setFrom_addr("CIS@cc.cust.edu.tw");
		m.setSender("中華科技大學資訊系統");
		m.setSubject("排程工作報告");
		if(!isDebug){
			m.setSend("0");
		}else{
			m.setSend("1");
		}
		df.update(m);		
		
		MailReceiver r;
		for(int i=0; i<members.size(); i++){
			
			r=new MailReceiver();
			r.setMail_oid(m.getOid());
			r.setAddr(members.get(i).get("Email").toString());
			r.setName(members.get(i).get("cname").toString());
			r.setType("to");									
			df.update(r);
			
		}		
		
		/*for(int i=0; i<members.size(); i++){
			try {
				im.sendMail(host, members.get(i).get("Email").toString(), "CIS@cc.cust.edu.tw", "排程工作報告", 
				members.get(i).get("cname")+"同仁您好<br>昨日排程工作報告如下:<br><br>"+sb.toString(), username, password);
			
				
			
			
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/	
		
    }
}
