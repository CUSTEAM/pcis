package quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import quartz.seld.SeldBackup;

/**
 * 週排程
 * @author John
 *
 */
public class TaskHour implements Job {		
	
	public void execute(JobExecutionContext context)throws JobExecutionException {		
		ApplicationContext springContext=new ClassPathXmlApplicationContext("classpath:../applicationContext.xml");		
		System.out.println("begin...");
		SeldBackup sb=new SeldBackup(springContext);
		sb.doit();
				
    }
}
