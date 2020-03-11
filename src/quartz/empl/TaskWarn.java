package quartz.empl;

import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import action.BaseAction;
import model.Mail;
import model.MailReceiver;
import quartz.BaseJob;

/**
 * 工作管理通知
 * @author John
 *
 */
public class TaskWarn extends BaseAction{		
	
	public void doit(){
		//ApplicationContext springContext=new ClassPathXmlApplicationContext("classpath:../applicationContext.xml");	
		//BaseIOImpl im = (BaseIOImpl) springContext.getBean("BaseIOImpl");
		
		//StudAffairManager sam= (StudAffairManager) springContext.getBean("StudAffairManager");
		//String host=df.sqlGetStr("SELECT Value FROM Parameter p WHERE p.Category='smtp' AND p.Name='mailServer'");
		//String username=df.sqlGetStr("SELECT Value FROM Parameter p WHERE p.Category='smtp' AND p.Name='username'");
		//String password=df.sqlGetStr("SELECT Value FROM Parameter p WHERE p.Category='smtp' AND p.Name='password'");
		
		StringBuilder sb=new StringBuilder();
		
		List<Map>tasks=df.sqlGet("SELECT e.cname, e.Email, (COUNT(*))as cnt FROM Task_hist th, empl e WHERE th.empl=e.idno AND th.open!='0' GROUP BY th.empl");
		Mail m;
		for(int i=0; i<tasks.size(); i++){
			sb.append(tasks.get(i).get("cname")+":"+tasks.get(i).get("cnt")+"個意見反應未完成<br>");
			m=new Mail();					
			m.setContent(tasks.get(i).get("cname")+"同仁您好<br><br>您有"+tasks.get(i).get("cnt")+"個未處理的意件反應，請至資訊系統點選「意見反應單」處理。");
			m.setFrom_addr("CIS@cc.cust.edu.tw");
			m.setSender("中華科技大學資訊系統");
			m.setSubject("意見反應未處理通知");
			m.setSend("0");			
			df.update(m);
			
			MailReceiver r=new MailReceiver();
			r.setMail_oid(m.getOid());
			r.setAddr(tasks.get(i).get("Email").toString());
			r.setName(tasks.get(i).get("cname").toString());
			r.setType("to");									
			df.update(r);
			
			
		}
		
		
		
		
		Map unit=df.sqlGetMap("SELECT e.cname, e.Email FROM CODE_UNIT cu, empl e WHERE cu.leader=e.idno AND cu.id ='114'");
		
		m=new Mail();					
		m.setContent(unit.get("cname")+"同仁您好<br><br>以下為意見反應未處理統計:<br>"+sb);
		m.setFrom_addr("CIS@cc.cust.edu.tw");
		m.setSender("中華科技大學資訊系統");
		m.setSubject("意見反應未處理通知");
		m.setSend("0");
		
		df.update(m);		
		MailReceiver r=new MailReceiver();
		r.setMail_oid(m.getOid());
		r.setAddr(unit.get("Email").toString());
		r.setName(unit.get("cname").toString());
		r.setType("to");									
		df.update(r);		
		
		df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('工作單通知','已寄出:"+tasks.size()+"份通知, 並且寄出名單至秘書室主管');");	
		
    }
}