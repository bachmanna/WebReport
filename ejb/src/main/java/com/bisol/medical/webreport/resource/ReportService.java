package com.bisol.medical.webreport.resource;

import javax.ejb.Local;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Local
@Path("/report")
public interface ReportService {

	@PUT
	@Path("/{study}")
	Response submit(@PathParam("study") int studyPk, @FormParam("report") String report);
	
	@GET
	@Path("/")
	Response query(String patienID);
}