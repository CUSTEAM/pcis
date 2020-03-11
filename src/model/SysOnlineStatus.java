package model;

import java.sql.Timestamp;

/**
 * SysOnlineStatus entity. @author MyEclipse Persistence Tools
 */

public class SysOnlineStatus implements java.io.Serializable {

	// Fields

	private Integer oid;
	private Timestamp inspection;
	private Short onlineTeacher;
	private Integer onlineStudent;
	private Integer onlineOther;

	// Constructors

	/** default constructor */
	public SysOnlineStatus() {
	}

	/** full constructor */
	public SysOnlineStatus(Timestamp inspection, Short onlineTeacher, Integer onlineStudent, Integer onlineOther) {
		this.inspection = inspection;
		this.onlineTeacher = onlineTeacher;
		this.onlineStudent = onlineStudent;
		this.onlineOther = onlineOther;
	}

	// Property accessors

	public Integer getOid() {
		return this.oid;
	}

	public void setOid(Integer oid) {
		this.oid = oid;
	}

	public Timestamp getInspection() {
		return this.inspection;
	}

	public void setInspection(Timestamp inspection) {
		this.inspection = inspection;
	}

	public Short getOnlineTeacher() {
		return this.onlineTeacher;
	}

	public void setOnlineTeacher(Short onlineTeacher) {
		this.onlineTeacher = onlineTeacher;
	}

	public Integer getOnlineStudent() {
		return this.onlineStudent;
	}

	public void setOnlineStudent(Integer onlineStudent) {
		this.onlineStudent = onlineStudent;
	}

	public Integer getOnlineOther() {
		return this.onlineOther;
	}

	public void setOnlineOther(Integer onlineOther) {
		this.onlineOther = onlineOther;
	}

}