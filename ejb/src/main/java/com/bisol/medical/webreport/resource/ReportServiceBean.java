package com.bisol.medical.webreport.resource;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;
import org.jboss.annotation.ejb.LocalBinding;

import com.bisol.medical.webreport.ReportStatus;
import com.bisol.medical.webreport.persistence.Report;
import com.bisol.medical.webreport.persistence.ReportAmendment;
import com.bisol.medical.webreport.persistence.ReportDao;

@Stateless
@LocalBinding(jndiBinding="ReportServiceLocal") 
public class ReportServiceBean implements ReportService {

	private final Logger logger = Logger.getLogger(getClass().getSimpleName());
	
	private final Comparator<ReportAmendment> amendmentPkComparator = new Comparator<ReportAmendment>() {
		@Override
		public int compare(ReportAmendment o1, ReportAmendment o2) {
			return (int) (o1.pk - o2.pk);
		}
	};
	
	@EJB
	private ReportDao reportDao; 
	
	@EJB(mappedName="StudyServiceLocal")
	private StudyService studyService; 
	
	@Override
	public Report findByStudy(long studyPk){
		return reportDao.findByStudy(studyPk);
	}
	
	@Override
	public String submitReport(long studyPk, String lockId, String releaseStr, String reportText){
		boolean release = "1".equals(releaseStr);
		Report reportEntity = reportDao.findByStudy(studyPk);
		
		if(reportEntity == null){
			logger.info("Creating new report for study " + studyPk);
			reportEntity = reportDao.create(studyPk, reportText, new Date());
		} else if(reportEntity.status == ReportStatus.released){
			amendReport(studyPk, reportText, release, reportEntity);
		} else {
			logger.info("Updating report for study " + studyPk);
			reportEntity.report = reportText;
			reportEntity.reportDatetime = new Date();
		}

		if(release){
			reportEntity.status = ReportStatus.released;
		}
	
		StudyLockService.unlockStudy(studyPk, UUID.fromString(lockId));
		
		//TODO create a 'find report by id' service, and return an appropriate URI here
		return "" + reportEntity.pk;
	}

	private void amendReport(long studyPk, String reportText, boolean release, Report reportEntity) {
		ReportAmendment amendment;
		
		if(reportEntity.amendments.isEmpty()){
			logger.info("Creating new report amendment for study " + studyPk);
			amendment = reportDao.createAmendment(reportEntity.pk, reportText, new Date());
		} else {
			Collections.sort(reportEntity.amendments, amendmentPkComparator);
			amendment = reportEntity.amendments.get(reportEntity.amendments.size()-1);
			if(amendment.status == ReportStatus.released){
				logger.info("Creating new report amendment for study " + reportEntity.pk);
				amendment = reportDao.createAmendment(reportEntity.pk, reportText, new Date());
			} else {
				logger.info("Updating report amendment for study " + reportEntity.pk);
				amendment.report = reportText;
				amendment.amendmentDatetime = new Date();
			}
		}
		
		reportEntity.status = ReportStatus.typed;
		
		if(release){
			amendment.status = ReportStatus.released;
		}
	}
}