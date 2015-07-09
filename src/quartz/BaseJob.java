package quartz;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import service.impl.StudAffairManager;
import service.impl.base.BaseIOImpl;


/**
 * 基本電子郵件排程
 * @author John *
 */
public class BaseJob {	
	
	//判斷是否為debug模式
	protected boolean isDebug=java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
	
	ApplicationContext springContext=new ClassPathXmlApplicationContext("classpath:../applicationContext.xml");	
	BaseIOImpl im = (BaseIOImpl) springContext.getBean("BaseIOImpl");
	StudAffairManager sam= (StudAffairManager) springContext.getBean("StudAffairManager");
	
	protected String host=sam.getDataFinder().sqlGetStr("SELECT Value FROM Parameter p WHERE p.Category='smtp' AND p.Name='mailServer'");
	protected String username=sam.getDataFinder().sqlGetStr("SELECT Value FROM Parameter p WHERE p.Category='smtp' AND p.Name='username'");
	protected String password=sam.getDataFinder().sqlGetStr("SELECT Value FROM Parameter p WHERE p.Category='smtp' AND p.Name='password'");
	
	//寄
	protected void sendMail(String mail, String title, String content) throws AddressException, UnsupportedEncodingException, MessagingException{		
		if(!isDebug){
			im.sendMail(host, mail, "CIS@cc.cust.edu.tw", title, content, username, password);
		}else{
			System.out.println(title+":"+mail);
			System.out.println(content);
		}
		
	}
}
