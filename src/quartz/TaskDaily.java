package quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


import quartz.score.StavgRerank;
import quartz.score.StavgRerank;
import quartz.sys.MaintainTable;
import quartz.dilg.RollCallAlert;
import quartz.dilg.LeaveAlert;
//import quartz.teacher.TutorTimeOffAlert;


/**
 * 每日排程
 * @author John
 *
 */
public class TaskDaily implements Job {		
	
	public void execute(JobExecutionContext context)throws JobExecutionException {		
		
		ApplicationContext springContext=new ClassPathXmlApplicationContext("classpath:../applicationContext.xml");		
		
		//Stavg計算並重新排名
		StavgRerank sr=new StavgRerank(springContext);
		sr.doit();
		
		//資料表維護
		System.out.println("資料表維護");
		MaintainTable mt=new MaintainTable(springContext);
		mt.doit();
		
		//未點名通知
		System.out.println("未點名通知");
		RollCallAlert rca=new RollCallAlert(springContext);
		rca.doit();
		
		//未核假通知
		System.out.println("未核假通知");
		LeaveAlert la=new LeaveAlert(springContext);
		la.doit();
    }
}
