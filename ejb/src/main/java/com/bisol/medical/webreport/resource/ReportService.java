package com.bisol.medical.webreport.resource;

import javax.ejb.Local;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.bisol.medical.webreport.persistence.Report;

@Local
@Path(ResourcePath.report)
public interface ReportService {

	@POST
	@Path(ResourcePath.reportByStudySubmit + "/{studyPk}")
	String submitReport(@PathParam("studyPk") long studyPk, @QueryParam("rel") String release, String report);

//	@POST
//	@Path(ResourcePath.reportAmend + "/{reportPk}")
//	String amendReport(@PathParam("reportPk") long reportPk, @QueryParam("rel") String release, String report);
//
	@GET
	@Path(ResourcePath.reportByStudyFind + "/{studyPk}")
	Report findByStudy(@PathParam("studyPk") long studyPk);
}