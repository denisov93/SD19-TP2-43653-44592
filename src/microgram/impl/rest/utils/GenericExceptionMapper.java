package microgram.impl.rest.utils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

	
	@Override
	public Response toResponse(Throwable ex) {

		if (ex instanceof WebApplicationException) {
			Response r = ((WebApplicationException) ex).getResponse();
			
			if( r.getStatus() == Status.INTERNAL_SERVER_ERROR.getStatusCode())
				ex.printStackTrace();

			return r;
		}

		ex.printStackTrace();

		return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).type(MediaType.APPLICATION_JSON).build();
	}
}