package quartz;

import java.io.IOException;
import java.text.ParseException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import quartz.mail.MailReSender;
import quartz.sys.WeatherManager;

/**
 * 小時排程
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
		WeatherManager weatherManager=new WeatherManager();
		try {
			weatherManager.doit(context);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		weatherManager=null;		
    }
}
