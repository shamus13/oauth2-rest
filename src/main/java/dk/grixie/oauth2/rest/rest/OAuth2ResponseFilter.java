package dk.grixie.oauth2.rest.rest;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

public class OAuth2ResponseFilter implements ContainerResponseFilter {
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "Authorization");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }
}
