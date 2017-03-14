package model;

public class MailAttache {
	
	private Integer Oid, size, mail_oid;
	private String file_name, path;
	public Integer getOid() {
		return Oid;
	}
	public void setOid(Integer oid) {
		Oid = oid;
	}
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	public Integer getMail_oid() {
		return mail_oid;
	}
	public void setMail_oid(Integer mail_oid) {
		this.mail_oid = mail_oid;
	}
	public String getFile_name() {
		return file_name;
	}
	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public MailAttache(){
		
	}
	public MailAttache(Integer oid, Integer size, Integer mail_oid, String file_name, String path) {
		super();
		Oid = oid;
		this.size = size;
		this.mail_oid = mail_oid;
		this.file_name = file_name;
		this.path = path;
	}	
}