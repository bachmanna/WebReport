package com.bisol.medical.webreport.persistence;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.bisol.medical.webreport.ReportStatus;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name="webreport_amendment ")
public class ReportAmendment {

	@Id
	@Column(name="pk")
	public long	pk;
	
	@Column(name="webreport_fk")
	public long	reportPk;
	
	@Column(name="report_data")
	public String report;
	
	@Column(name="amendment_datetime")
	public Date	amendmentDatetime;
	
	@Column(name="status")
	@Enumerated(EnumType.STRING)
	public ReportStatus status;

	public ReportAmendment(){
	}

	public ReportAmendment(long reportPk, String report, Date amendmentDatetime){
		this.reportPk = reportPk;
		this.report = report;
		this.amendmentDatetime = amendmentDatetime;
		this.status = ReportStatus.typed;
	}
}