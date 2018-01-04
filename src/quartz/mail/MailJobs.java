package quartz.mail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeUtility;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import model.Mail;
import model.MailAttache;
import model.MailReceiver;
import quartz.BaseJob;
import quartz.sys.WeatherManager;
import service.impl.StudAffairManager;
import service.impl.base.BaseAccessImpl;
import service.impl.base.BaseIOImpl;

/**
 * 排程工作報告
 * @author John
 *
 */
public class MailJobs extends BaseJob implements Job{		
	
	public void execute(JobExecutionContext context)throws JobExecutionException {
		
		//AbstractApplicationContext springContext=new ClassPathXmlApplicationContext("classpath:../applicationContext.xml");
		//BaseAccessImpl df= (BaseAccessImpl) springContext.getBean("DataManager");
		//BaseIOImpl im = (BaseIOImpl) springContext.getBean("BaseIOImpl");
		//StudAffairManager sam= (StudAffairManager) springContext.getBean("StudAffairManager");
		//List<Mail>t=getDf().hqlGetListBy("FROM Mail WHERE send=1");
		/*Mail m=(Mail) df.hqlGetListBy("FROM Mail WHERE Oid=1").get(0);
		for(int i=0; i<58; i++){
			
			
			try {
				System.out.print("更新前"+m.getSender()+", ");
				df.getHibernateDAO().getHibernateTemplate().refresh(m);
				System.out.println("更新後"+m.getSender());
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}*/		
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
