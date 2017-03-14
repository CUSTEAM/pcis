package model;

public class Mail implements java.io.Serializable{	
	
	private String from_addr, sender, subject, content, send, error_message;
	private Integer Oid;
	
	public String getSender() {
		return sender;
	}
	public String getFrom_addr() {
		return from_addr;
	}
	public void setFrom_addr(String from_addr) {
		this.from_addr = from_addr;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getSend() {
		return send;
	}
	public void setSend(String send) {
		this.send = send;
	}
	public String getError_message() {
		return error_message;
	}
	public void setError_message(String error_message) {
		this.error_message = error_message;
	}
	public Integer getOid() {
		return Oid;
	}
	public void setOid(Integer oid) {
		Oid = oid;
	}
	public Mail(){
		
	}
	public Mail(String from_addr, String sender, String subject, String content, String send, String error_message,
			Integer oid) {
		this.from_addr = from_addr;
		this.sender = sender;
		this.subject = subject;
		this.content = content;
		this.send = send;
		this.error_message = error_message;
		Oid = oid;
	}
}