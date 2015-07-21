package com.bisol.medical.webreport.persistence;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.bisol.medical.webreport.ReportStatus;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name="webreport")
@NamedQueries({
	@NamedQuery(name="webreport.byStudy", query="FROM Report report WHERE report.studyPk = :studyPk"),
	@NamedQuery(name="webreport.exists", query="SELECT report.pk FROM Report report WHERE report.pk = :pk"),
	@NamedQuery(name="webreport.isReported", query="SELECT report.pk FROM Report report WHERE report.studyPk = :studyPk AND status = 'released'")
})
@SequenceGenerator(name="ReportSequence", initialValue=1, sequenceName="webreport_pk_seq", allocationSize=1)
public class Report {

	@Id
	@Column(name="pk")
	@GeneratedValue(generator="ReportSequence")
	public long pk;
	
	@Column(name="study_fk")
	public long studyPk;

	@Column(name="report_datetime")
	public Date reportDatetime;
	
	@Column(name="report_data")
	public String report;
	
	@Column(name="status")
	@Enumerated(EnumType.STRING)
	public ReportStatus status;

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
	@JoinColumn(name="webreport_fk")
	public List<ReportAmendment> amendments;

	public Report() {
	}
	
	public Report(String report, Date reportDatetime, long studyPk) {
		this.reportDatetime = reportDatetime;
		this.report = report;
		this.studyPk = studyPk;
		this.status = ReportStatus.typed;
	}
}