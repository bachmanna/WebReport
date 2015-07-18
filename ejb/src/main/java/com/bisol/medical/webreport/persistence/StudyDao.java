package com.bisol.medical.webreport.persistence;

import java.util.Date;
import java.util.List;

import com.bisol.medical.webreport.ReportStatus;

public interface StudyDao {
	List<StudyDto> query(int offset, int limit, String patientID, String patientName, String accessionNumber, Date startDate, Date endDate, String modality, ReportStatus reportStatus);
}