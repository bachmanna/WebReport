package com.bisol.medical.webreport.persistence;

import java.util.Date;

public interface ReportDao {
	boolean exists(long pk);
	boolean isReported(long studyPk);
	Report create(long studyPk, String report, Date reportDatetime);
	ReportAmendment createAmendment(long reportPk, String report, Date reportDatetime);
	Report findByStudy(long studyPk);
	Report findById(long reportPk);
	ReportAmendment findReportAmendmentById(long amendmentPk);
}