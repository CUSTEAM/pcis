package quartz.mail;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import quartz.BaseJob;

/**
 * 當日寄送失敗郵件每小時重新寄送
 * @author John
 *
 */
public class MailReSender extends BaseJob{		
	
	public void doit(JobExecutionContext context)throws JobExecutionException {
		//SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd");
		//Date d=new Date();
		//System.out.println("UPDATE Mail_main SET send=0, error_message=null WHERE error_message IS NOT NULL AND send_time>='"+sf.format(new Date())+"'");
		//df.exSql("UPDATE Mail_main SET send=0, error_message=null WHERE error_message IS NOT NULL AND send_time>='"+sf.format(new Date())+"'");
		
		
    }
}
