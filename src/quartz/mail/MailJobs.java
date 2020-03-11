package quartz.mail;

import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import model.Mail;
import model.MailAttache;
import model.MailReceiver;
import quartz.BaseJob;

/**
 * disabled
 * @author John
 *
 */
public class MailJobs extends BaseJob implements Job{		
	
	public void execute(JobExecutionContext context)throws JobExecutionException {
		
		
		Map<String, String>smtp=df.sqlGetMap("SELECT * FROM SYS_HOST WHERE useid='SysMail'");
		MailSender sender;
		List<Mail>m=df.hqlGetListBy("FROM Mail WHERE send=0");
		if(m.size()>100)m=m.subList(0, 59);
		List<MailReceiver>r;
		List<MailAttache>a;
		//HtmlEmail email;
		for(int i=0; i<m.size(); i++){
			
			m.get(i).setSend("1");
		    df.update(m.get(i));
			r=df.hqlGetListBy("FROM MailReceiver WHERE mail_oid="+m.get(i).getOid());
			a=df.hqlGetListBy("FROM MailAttache WHERE mail_oid="+m.get(i).getOid());;
			
			sender=new MailSender(m.get(i), r, a, 
			smtp.get("username"), smtp.get("password"), 
			smtp.get("host_runtime"), smtp.get("port"),df);
			sender.start();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
    }
}
