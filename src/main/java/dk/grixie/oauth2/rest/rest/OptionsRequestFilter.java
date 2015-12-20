package dk.grixie.oauth2.rest.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@PreMatching
@Provider
public class OptionsRequestFilter implements ContainerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(OptionsRequestFilter.class);

    public OptionsRequestFilter() {
    }

    @Override
    public void filter(ContainerRequestContext request) {
        if (request.getMethod().equals(HttpMethod.OPTIONS)) {
            Response response = Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).build();
            request.abortWith(response);
        }
    }
}
