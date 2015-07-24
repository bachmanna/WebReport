package com.bisol.medical.webreport.resource;

import java.util.Date;
import java.util.List;
import java.util.UUID;

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
	public String lockStudy(long pk){
		StudyLock lock = StudyLockService.lockStudy(pk, 5*60*1000);
		return lock.getId().toString();
	}
	
	@Override
	public void renewStudyLock(long pk, String lockId){
		StudyLockService.renewLock(pk, UUID.fromString(lockId), 5*60*1000);
	}
	
	@Override
	public void unlockStudy(long pk, String lockId){
		StudyLockService.unlockStudy(pk, UUID.fromString(lockId));
	}
	
	@Override
	public List<StudyDto> query(String patientID, String patientName, String accessionNumber, Date startDate, Date endDate, String modality, String reportStatusStr, Integer offset, Integer limit){
		offset = Math.max(0, offset == null ? 0 : offset);
		limit = Math.min(100, limit == null ? 100 : limit);
		ReportStatus reportStatus = parseReportStatus(reportStatusStr);
		
		List<StudyDto> studies = studyDao.query(offset, limit, patientID, patientName, accessionNumber, startDate, endDate, modality, reportStatus);
		for(StudyDto study : studies){//TODO shoud be a join in the DB
			if(StudyLockService.isLocked(study)){
				study.locked = true;
			}
		}
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