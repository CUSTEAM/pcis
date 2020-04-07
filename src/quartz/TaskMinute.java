package quartz;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import model.Mail;
import model.MailAttache;
import model.MailReceiver;
import model.SysOnlineStatus;
import quartz.mail.MailReSender;
import quartz.mail.MailSender;
import quartz.score.StavgRerank;
import quartz.sys.WeatherManager;
import service.impl.DataFinder;

/**
 * 分鐘級排程
 * @author John
 *
 */
public class TaskMinute extends BaseJob implements Job {		
	
	int tea, std, tot;
	String lastmin;
	SimpleDateFormat sf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public void execute(JobExecutionContext context)throws JobExecutionException {
		//System.out.println("每分鐘排程工作");
		Calendar c=Calendar.getInstance();
    	c.add(Calendar.MINUTE, -30);
    	lastmin=sf.format(c.getTime());
    	AbstractApplicationContext springContext = new ClassPathXmlApplicationContext("classpath:../applicationContext.xml");
    	DataFinder df =(DataFinder)springContext.getBean("DataFinder");
    	springContext.registerShutdownHook();
    	
    	
		//System.out.println("教師人數: onlineTeacher");
		/*tea=df.sqlGetInt("SELECT COUNT(*) FROM wwpass w, empl e WHERE w.username=e.idno AND e.category='1' AND w.online>'"+lastmin+"'");
		//System.out.println("學生人數: onlineStudent");
		std=df.sqlGetInt("SELECT COUNT(*) FROM wwpass w, stmd s WHERE w.username=s.student_no AND w.online>'"+lastmin+"'");
		//System.out.println("其他人數: onlineOther");
		tot=df.sqlGetInt("SELECT COUNT(*) FROM wwpass w WHERE w.online>'"+lastmin+"'");
		
		SysOnlineStatus o=new SysOnlineStatus();
		o.setInspection(new Timestamp(c.getTimeInMillis()));
		o.setOnlineOther(tot-(tea+std));
		o.setOnlineStudent(std);
		o.setOnlineTeacher((short) tea);
		df.update(o);
		*/
		
		//寄件工作最低優先		
		Map<String, String>smtp=df.sqlGetMap("SELECT * FROM SYS_HOST WHERE useid='SysMail'");
		MailSender sender;
		List<Mail>m=df.hqlGetListBy("FROM Mail WHERE send=0");
		System.out.println("排程郵件掃瞄到"+m.size()+"封待寄郵件");
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
		
		springContext.close();
    }
}
