package com.bisol.medical.webreport.resource;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.bisol.medical.webreport.persistence.StudyDto;

@Path("/study")
@Local
public interface StudyService {
	
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	List<StudyDto> query(@QueryParam("pid") String patientID,
					@QueryParam("pna") String patientName,
					@QueryParam("acc") String accessionNumber,
					@QueryParam("sd") Date startDate,
					@QueryParam("ed") Date endDate,
					@QueryParam("sm") String modality,
					@QueryParam("of") Integer offset,
					@QueryParam("lm") Integer limit);

	@GET
	@Path("/t")
	@Produces(MediaType.APPLICATION_JSON)
	String test();
}
