package com.bisol.medical.webreport.resource;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.bisol.medical.webreport.persistence.StudyDto;

@Local
@Path(ResourcePath.study)
public interface StudyService {
	
	@GET
	@Path(ResourcePath.studyQuery)
	@Produces(MediaType.APPLICATION_JSON)
	List<StudyDto> query(@QueryParam("pid") String patientID,
					@QueryParam("pna") String patientName,
					@QueryParam("acc") String accessionNumber,
					@QueryParam("sd") Date startDate,
					@QueryParam("ed") Date endDate,
					@QueryParam("sm") String modality,
					@QueryParam("rs") String reportStatus,
					@QueryParam("of") Integer offset,
					@QueryParam("lm") Integer limit);

	@GET
	@Path("/{pk}" + ResourcePath.studyLock)
	@Produces(MediaType.TEXT_PLAIN)
	String lockStudy(@PathParam("pk") long pk);
	
	@DELETE
	@Path("/{pk}" + ResourcePath.studyLock + "/{lid}")
	@Produces(MediaType.TEXT_PLAIN)
	void unlockStudy(@PathParam("pk") long pk, @PathParam("lid") String lockId);

	@POST
	@Path("/{pk}" + ResourcePath.studyLock + "/{lid}")
	void renewStudyLock(@PathParam("pk") long pk, @PathParam("lid") String lockId);
}