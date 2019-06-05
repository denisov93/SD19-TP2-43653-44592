package microgram.impl.rest.replication;

import java.io.IOException;
import java.util.Base64;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

/**
 * 
 * Intercepts client requests to retrieve version information from the request headers; 
 * Intercepts server replies to append version information into the response headers;
 * 
 * When version information is sent to the client (tester), via Version.set(...), the client does not process it, but promises to include it in the next request.
 * 
 * @author smd
 *
 */
public class ClientVersionAppender implements ContainerResponseFilter, ContainerRequestFilter{
	
	private static final String X_VERSION_HDR = "X-Version";

	@Override
	public void filter(ContainerRequestContext reqCtx) throws IOException {
		String value = reqCtx.getHeaderString(X_VERSION_HDR);
		if( value != null && ! value.isEmpty()) {
			Version.jsonVersionIn.set( new String(Base64.getDecoder().decode(value)));
		}
	}

	
	@Override
	public void filter(ContainerRequestContext reqCtx, ContainerResponseContext resCtx) throws IOException {
		String version = Version.jsonVersionOut.get();
		if( version != null ) {
			String value = Base64.getEncoder().encodeToString(version.getBytes());
			resCtx.getHeaders().add(X_VERSION_HDR, value);
		}
	}


}
