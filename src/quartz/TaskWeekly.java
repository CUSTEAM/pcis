package quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import quartz.dilg.Abs2Alert;

/**
 * 週排程
 * @author John
 *
 */
public class TaskWeekly implements Job {		
	
	public void execute(JobExecutionContext context)throws JobExecutionException {		
		//AbstractApplicationContext springContext=new ClassPathXmlApplicationContext("classpath:../applicationContext.xml");		
		System.out.println("每週排程工作");
		Abs2Alert aa=new Abs2Alert();
		aa.doit();		
		//springContext.registerShutdownHook();
    }
}
