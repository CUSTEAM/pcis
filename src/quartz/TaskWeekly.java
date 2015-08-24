package quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import quartz.dilg.Abs2Alert;
import quartz.score.StavgRerank;

/**
 * 週排程
 * @author John
 *
 */
public class TaskWeekly implements Job {		
	
	public void execute(JobExecutionContext context)throws JobExecutionException {		
		ApplicationContext springContext=new ClassPathXmlApplicationContext("classpath:../applicationContext.xml");		
		
		//嚴重缺曠導師通知
		Abs2Alert aa=new Abs2Alert(springContext);
		aa.doit();		
				
    }
}
