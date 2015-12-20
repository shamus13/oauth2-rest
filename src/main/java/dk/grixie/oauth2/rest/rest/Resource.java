package dk.grixie.oauth2.rest.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.jws.WebParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.text.SimpleDateFormat;
import java.util.Date;

@Path("/test")
public class Resource {
    private static final Logger log = LoggerFactory.getLogger(Resource.class);

    private static String message = "";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Path("time")
    public CurrentTime time() {
        return new CurrentTime(new SimpleDateFormat("yyyyMMdd-HH:mm:ss").format(new Date()));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"USER_ID", "FULL_ACCESS"})
    @Path("message")
    public synchronized Message getMessage(@Context SecurityContext context) {
        log.info("getMessage called by: {}", context.getUserPrincipal().getName());

        Message response = new Message();

        response.setMessage(message);

        return response;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"USER_ID", "FULL_ACCESS"})
    @Path("message")
    public synchronized void setMessage(@Context SecurityContext context, @WebParam Message request) {
        log.info("setMessage called by: {}", context.getUserPrincipal().getName());

        message = request.getMessage();
    }
}
