package com.bisol.medical.webreport.persistence;

import java.sql.Timestamp;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StudyDto {

	public Date studyDateTime;
	public String accessionNumber;
	public String modalities;
	public String patientId;
	public String patientName;
	public String studyInstanceUid;
	public String reportStatus;
	public long pk;
	public long reportPk;
	public boolean locked = false;

	public StudyDto(long pk2, String patientID, String patientName, String accessionNumber, Timestamp studyDateTime, String modalities, String studyInstanceUid, long reportPk, String reportStatus) {
		this.pk = pk2;
		this.patientId = patientID;
		this.patientName = patientName;
		this.accessionNumber = accessionNumber;
		this.studyDateTime = studyDateTime;
		this.modalities = modalities;
		this.studyInstanceUid = studyInstanceUid;
		this.reportPk = reportPk;
		this.reportStatus = reportStatus;
	}
}