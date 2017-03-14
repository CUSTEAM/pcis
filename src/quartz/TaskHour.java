package quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import quartz.mail.MailReSender;

/**
 * 週排程
 * @author John
 *
 */
public class TaskHour extends BaseJob implements Job {		
	
	public void execute(JobExecutionContext context)throws JobExecutionException {		
		//ApplicationContext springContext=new ClassPathXmlApplicationContext("classpath:../applicationContext.xml");		
		System.out.println("每小時排程工作");
		//SeldBackup sb=new SeldBackup(springContext);
		//sb.doit();
		MailReSender mailReSender=new MailReSender();	
		mailReSender.doit(context);
		mailReSender=null;
    }
}
