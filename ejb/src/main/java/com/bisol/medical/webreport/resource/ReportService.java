package com.bisol.medical.webreport.resource;

import javax.ejb.Local;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Local
@Path("/report")
public interface ReportService {

	@POST
	@Path("/{study}")
	Response submit(@PathParam("study") int studyPk, @FormParam("report") String report);
}