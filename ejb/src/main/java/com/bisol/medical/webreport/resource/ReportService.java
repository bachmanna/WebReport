package com.bisol.medical.webreport.resource;

import javax.ejb.Local;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.bisol.medical.webreport.persistence.Report;

@Local
@Path(ResourcePath.report)
public interface ReportService {

	@POST
	@Path(ResourcePath.reportByStudySubmit + "/{studyPk}")
	String submit(@PathParam("studyPk") long studyPk, String report);

	@POST
	@Path(ResourcePath.reportAmend + "/{reportPk}")
	String amend(@PathParam("reportPk") long reportPk, String report);

	@GET
	@Path(ResourcePath.reportByStudyFind + "/{studyPk}")
	Report findByStudy(@PathParam("studyPk") long studyPk);
}