package quartz.empl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import model.Mail;
import model.MailReceiver;
import quartz.BaseJob;

/**
 * 差勤系統通知
 * @author John
 *
 */
public class AMSignWarn extends BaseJob implements Job {		
	
	public void execute(JobExecutionContext context)throws JobExecutionException {	
		//ApplicationContext springContext=new ClassPathXmlApplicationContext("classpath:../applicationContext.xml");	
		//BaseIOImpl im = (BaseIOImpl) springContext.getBean("BaseIOImpl");
		
		//StudAffairManager sam= (StudAffairManager) springContext.getBean("StudAffairManager");
		String host=df.sqlGetStr("SELECT Value FROM Parameter p WHERE p.Category='smtp' AND p.Name='mailServer'");
		String username=df.sqlGetStr("SELECT Value FROM Parameter p WHERE p.Category='smtp' AND p.Name='username'");
		String password=df.sqlGetStr("SELECT Value FROM Parameter p WHERE p.Category='smtp' AND p.Name='password'");
		
		SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
		SimpleDateFormat sf1=new SimpleDateFormat("yyyy-MM-dd");
		
		Date now=new Date();
		String date=sf1.format(now);
		
		StringBuilder sb=new StringBuilder("SELECT d.*, e.cname, e.Email FROM AMS_Workdate d, empl e WHERE " +
		"e.idno=d.idno AND e.WorkShift!='NO' AND d.wdate='"+date+"' AND d.set_in>'04:00' AND d.set_in<'10:00' AND d.real_in IS NULL");
		
		try {
			//下午
			if(now.getTime()>sf.parse(date+" 14:00").getTime()){
				sb=new StringBuilder("SELECT d.*, e.cname, e.Email FROM AMS_Workdate d, empl e WHERE " +
				"e.idno=d.idno AND e.WorkShift!='NO' AND d.wdate='"+date+"'AND d.set_in>'10:00' AND d.set_in<'15:00' AND d.real_in IS NULL");
			}				
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		System.out.println(sb);
		List<Map>empls=df.sqlGet(sb.toString());
		StringBuilder sb1=new StringBuilder();
		StringBuilder sb2=new StringBuilder();
		for(int i=0; i<empls.size(); i++){
			
			//當日請假
			if(df.sqlGetInt("SELECT COUNT(*)FROM AMS_DocApply WHERE idno='"+empls.get(i).get("idno")+"' AND startDate LIKE '"+date+"%'")>0){
				continue;
			}
			
			try{
				
				sb=new StringBuilder("<p>"+empls.get(i).get("cname")+" 同仁您好</p>");
				sb.append("<p>今日班表上班時間為: "+empls.get(i).get("set_in")+", 至目前尚未有刷卡記錄<br>請儘快處理請假或補登事宜。</p>");
				sb.append("<p>如班別或排班時間有誤，請洽人事單位。</p>");
				
				Mail m=new Mail();					
				m.setContent(sb.toString());
				m.setFrom_addr("CIS@cc.cust.edu.tw");
				m.setSender("中華科技大學資訊系統");
				m.setSubject("未刷卡通知");
				if(!isDebug){
					m.setSend("0");
				}else{
					m.setSend("1");
				}
				df.update(m);
				
				MailReceiver r=new MailReceiver();
				r.setMail_oid(m.getOid());
				r.setAddr(empls.get(i).get("Email").toString());
				r.setName(empls.get(i).get("cname").toString());
				r.setType("to");									
				df.update(r);
			}catch(Exception e){
				
				sb2.append(empls.get(i).get("cname")+"<"+empls.get(i).get("Email")+">,");
			}
					
					
		}
		df.exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('未刷卡通知','已寄出:"+sb1+"<br>未寄出:"+sb2+"');");	
		
    }
}