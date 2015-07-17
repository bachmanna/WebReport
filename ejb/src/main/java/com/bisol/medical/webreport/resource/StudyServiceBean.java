package com.bisol.medical.webreport.resource;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.jboss.annotation.ejb.LocalBinding;

import com.bisol.medical.webreport.persistence.StudyDao;
import com.bisol.medical.webreport.persistence.StudyDto;

@Stateless
@LocalBinding(jndiBinding="StudyServiceLocal")
public class StudyServiceBean implements StudyService {
	
	@EJB
	private StudyDao studyDao;
	
//	@Override
//	public StudyDao query(String patientID, String patientName, String accessionNumber, Date startDate, Date endDate, String modality, Integer offset, Integer limit){
//		return studyDao;
//	}
	
	@Override
	public List<StudyDto> query(String patientID, String patientName, String accessionNumber, Date startDate, Date endDate, String modality, Integer offset, Integer limit){
		offset = Math.max(0, offset == null ? 0 : offset);
		limit = Math.min(100, limit == null ? 100 : limit);
		
		List<StudyDto> studies = studyDao.query(offset, limit, patientID, patientName, accessionNumber, startDate, endDate, modality);
		return studies;
//		return new StudyListDto(studies);
	}
	
	@Override
	public String test(){
		return "{}";
	}
}