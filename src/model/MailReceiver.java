package model;

public class MailReceiver {
	
	public Integer getOid() {
		return Oid;
	}
	public void setOid(Integer oid) {
		Oid = oid;
	}
	public Integer getMail_oid() {
		return mail_oid;
	}
	public void setMail_oid(Integer mail_oid) {
		this.mail_oid = mail_oid;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	private Integer Oid, mail_oid;
	private String type, addr, name;
	public MailReceiver(){
		
	}
	public MailReceiver(Integer oid, Integer mail_oid, String type, String addr, String name) {
		super();
		Oid = oid;
		this.mail_oid = mail_oid;
		this.type = type;
		this.addr = addr;
		this.name = name;
	}
	
	

}
