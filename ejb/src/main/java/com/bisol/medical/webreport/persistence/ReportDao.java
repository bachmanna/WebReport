package com.bisol.medical.webreport.persistence;

import java.util.Date;

public interface ReportDao {
	boolean exists(long pk);
	boolean isReported(long studyPk);
	long create(long studyPk, String report, Date reportDatetime);
	long amend(long reportPk, String report, Date reportDatetime);
	Report byStudy(long studyPk);
}