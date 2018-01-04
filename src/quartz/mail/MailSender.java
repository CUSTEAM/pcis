package quartz.mail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.List;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;

import model.Mail;
import model.MailAttache;
import model.MailReceiver;
import service.impl.base.BaseAccessImpl;

public class MailSender extends Thread {
	private Mail m;
	private List<MailReceiver>r;
	private List<MailAttache>a;
	private BaseAccessImpl df;
	private String username, password, host;
	private String port;
	
	public MailSender(Mail m, List<MailReceiver>r, List<MailAttache>a, 
			String username, String password, String host, String port, BaseAccessImpl df){
		this.m=m;
		this.r=r;
		this.a=a;
		this.username=username;
		this.password=password;
		this.host=host;
		this.df=df;
		this.port=port;
	}
	
	public void run() {
		HtmlEmail email=new HtmlEmail();			
		try {
			
			email.setHostName(host);
			email.setAuthentication(username, password);				
			email.setSendPartial(true);
			email.setCharset("UTF-8");
			if(port.equals("465")){
				email.setSSLOnConnect(true);
				email.setSslSmtpPort(port); //SSL
			}
			
			email.setFrom(m.getFrom_addr(), m.getSender());				
			email.setSubject(m.getSubject());
			email.setMsg(m.getContent());
			
			
			//email.smtp.auth=true
					//email.smtp.starttls.enable=true
			
			//收件人們
			//r=df.hqlGetListBy("FROM MailReceiver WHERE mail_oid="+m.get(i).getOid());
			for(int j=0; j<r.size(); j++){
				
				//System.out.println(r.get(j));
				if(r.get(j).getType().equals("to")){
					email.addTo(r.get(j).getAddr(), r.get(j).getName());
				}
				if(r.get(j).getType().equals("cc")){
					email.addCc(r.get(j).getAddr(), r.get(j).getName());
				}
				if(r.get(j).getType().equals("bcc")){
					email.addBcc(r.get(j).getAddr(), r.get(j).getName());
				}
				//email.addBcc("hsiao@cc.cust.edu.tw");
			}
			
			//附件檔案
			//a=df.hqlGetListBy("FROM MailAttache WHERE mail_oid="+m.get(i).getOid());
			System.out.println(a.size());
			if(a.size()>0){
				EmailAttachment attachment;
				for(int j=0; j<a.size(); j++){				
					attachment = new EmailAttachment();
					attachment.setURL(new URL(a.get(j).getPath()));//遠端文件			
					//attachment.setDescription("a");
					attachment.setName(a.get(j).getFile_name());
					email.attach(attachment);
				}
			}
			email.send();
			
		}catch(Exception e){
			e.printStackTrace();
			Writer result = new StringWriter();
		    PrintWriter printWriter = new PrintWriter(result);
		    e.printStackTrace(printWriter);
		    String ex=result.toString();
		    System.out.println(ex);
		    m.setError_message(ex);
		    m.setSend("1");
		    df.update(m);
		}		
	    System.out.println("已寄出");
	}

}
