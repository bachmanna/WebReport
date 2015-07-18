package com.bisol.medical.webreport.resource;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.jboss.annotation.ejb.LocalBinding;

import com.bisol.medical.webreport.persistence.Report;
import com.bisol.medical.webreport.persistence.ReportDao;

@Stateless
@LocalBinding(jndiBinding="ReportServiceLocal") 
public class ReportServiceBean implements ReportService {
	
	@EJB
	private ReportDao reportDao; 
	
	@Override
	public String submit(long studyPk, String report){
		if(reportDao.isReported(studyPk)){
			throw new WebApplicationException("Study of id '" + studyPk + "' already reported", Status.CONFLICT);
		}
		
		//TODO create a 'find report by id' service, and return an appropriate URI here
		return "" + reportDao.create(studyPk, report, new Date());
	}
	
	@Override
	public String amend(long reportPk, String report){
		if(!reportDao.exists(reportPk)){
			throw new WebApplicationException("Report of id '" + reportPk + "' not found", Status.CONFLICT);
		}
		
		//TODO create a 'find amendment by id' service, and return an appropriate URI here
		return "" + reportDao.amend(reportPk, report, new Date());
	}

	@Override
	public Report findByStudy(long studyPk){
		return reportDao.byStudy(studyPk);
	}
}