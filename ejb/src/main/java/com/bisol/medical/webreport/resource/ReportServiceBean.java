package com.bisol.medical.webreport.resource;

import javax.ejb.Stateless;
import javax.ws.rs.core.Response;

import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@LocalBinding(jndiBinding="ReportServiceLocal") 
public class ReportServiceBean implements ReportService {
	
	@Override
	public Response submit(int studyPk, String report){
		return Response.serverError().build();
	}
}