package quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import quartz.dilg.LeaveAlert;
//import quartz.teacher.TutorTimeOffAlert;
import quartz.dilg.RollCallAlert;
import quartz.score.StavgRerank;
import quartz.sys.MaintainTable;


/**
 * 每日排程
 * @author John
 *
 */
public class TaskDaily implements Job {		
	
	public void execute(JobExecutionContext context)throws JobExecutionException {		
		
		System.out.println("每日排程工作");
		
		/*Mail m=(Mail)df.hqlGetListBy("FROM Mail").get(0);
		m.setSender("gIS");
		System.out.println("變更");
		df.update(m);
		
		System.out.println(m.getSender());*/
		
		//Stavg計算並重新排名
		//StavgRerank sr=new StavgRerank();
		//sr.doit();
		
		//資料表維護
		System.out.println("資料表維護");
		MaintainTable mt=new MaintainTable();
		mt.doit();
		
		//未點名通知
		System.out.println("未點名通知");
		RollCallAlert rca=new RollCallAlert();
		rca.doit();
		
		//未核假通知
		System.out.println("未核假通知");
		LeaveAlert la=new LeaveAlert();
		la.doit();
		
    }
}
