package quartz.empl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import quartz.BaseJob;

import service.impl.StudAffairManager;
import service.impl.base.BaseIOImpl;

/**
 * 差勤系統通知
 * @author John
 *
 */
public class AMSignWarn extends BaseJob implements Job {		
	
	public void execute(JobExecutionContext context)throws JobExecutionException {	
		ApplicationContext springContext=new ClassPathXmlApplicationContext("classpath:../applicationContext.xml");	
		BaseIOImpl im = (BaseIOImpl) springContext.getBean("BaseIOImpl");
		
		StudAffairManager sam= (StudAffairManager) springContext.getBean("StudAffairManager");
		String host=sam.getDataFinder().sqlGetStr("SELECT Value FROM Parameter p WHERE p.Category='smtp' AND p.Name='mailServer'");
		String username=sam.getDataFinder().sqlGetStr("SELECT Value FROM Parameter p WHERE p.Category='smtp' AND p.Name='username'");
		String password=sam.getDataFinder().sqlGetStr("SELECT Value FROM Parameter p WHERE p.Category='smtp' AND p.Name='password'");
		
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
		
		List<Map>empls=sam.getDataFinder().sqlGet(sb.toString());
		String Email;		
		StringBuilder sb1=new StringBuilder();
		StringBuilder sb2=new StringBuilder();
		for(int i=0; i<empls.size(); i++){
			try{
				if(sam.getDataFinder().sqlGetInt("SELECT COUNT(*)FROM AMS_DocApply WHERE idno='"+empls.get(i).get("idno")+"' AND startDate LIKE '"+date+"%'")>0){
					continue;
				}
				
				//Email="hsiao@cc.cust.edu.tw";
				sb=new StringBuilder("<p>"+empls.get(i).get("cname")+" 同仁您好</p>");
				sb.append("<p>今日班表上班時間為: "+empls.get(i).get("set_in")+", 至目前尚未有刷卡記錄<br>請儘快處理請假或補登事宜。</p>");
				sb.append("<p>如班別或排班時間有誤，請洽人事單位。</p>");
				
				try {
					Email=empls.get(i).get("Email").toString();
					//im.sendMail(host, Email, "CIS@cc.cust.edu.tw", "差勤系統通知", sb.toString(), username, password);
					this.sendMail(Email, "差勤系統通知", sb.toString());
					sb1.append(empls.get(i).get("cname")+",");
				} catch (Exception e) {				
					sb2.append(empls.get(i).get("cname")+",");
				}
				
			}catch(Exception e){
				sb2.append(empls.get(i).get("cname")+", "+e+", ");				
			}			
		}
		sam.getDataFinder().exSql("INSERT INTO SYS_SCHEDULE_LOG(subject,note)VALUES('未刷卡通知','已寄出:"+sb1+"<br>未寄出:"+sb2+"');");		
    }
}