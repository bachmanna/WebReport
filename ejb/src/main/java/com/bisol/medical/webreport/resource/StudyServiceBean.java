package com.bisol.medical.webreport.resource;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.jboss.annotation.ejb.LocalBinding;

import com.bisol.medical.webreport.ReportStatus;
import com.bisol.medical.webreport.persistence.StudyDao;
import com.bisol.medical.webreport.persistence.StudyDto;

@Stateless
@LocalBinding(jndiBinding="StudyServiceLocal")
public class StudyServiceBean implements StudyService {
	
	@EJB
	private StudyDao studyDao;
	
	@Override
	public List<StudyDto> query(String patientID, String patientName, String accessionNumber, Date startDate, Date endDate, String modality, String reportStatusStr, Integer offset, Integer limit){
		offset = Math.max(0, offset == null ? 0 : offset);
		limit = Math.min(100, limit == null ? 100 : limit);
		ReportStatus reportStatus = parseReportStatus(reportStatusStr);
		
		List<StudyDto> studies = studyDao.query(offset, limit, patientID, patientName, accessionNumber, startDate, endDate, modality, reportStatus);
		return studies;
	}

	private ReportStatus parseReportStatus(String reportStatusStr) {
		switch (reportStatusStr) {
		case "1": return ReportStatus.typed; 
		case "2": return ReportStatus.released;
		default: return null;
		}
	}
}