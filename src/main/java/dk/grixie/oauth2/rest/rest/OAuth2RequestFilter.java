package dk.grixie.oauth2.rest.rest;

import dk.grixie.oauth2.rest.oauth2.Validator;
import dk.grixie.oauth2.rest.oauth2.Validation;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Provider
public class OAuth2RequestFilter implements ContainerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(OAuth2RequestFilter.class);

    @Context
    private ResourceInfo info;

    public OAuth2RequestFilter() {
    }

    @Override
    public void filter(ContainerRequestContext request) {
        if (requiresAuthorization()) {
            Collection<String> authFields = request.getHeaders().get("Authorization");

            if (authFields != null) {
                String token = null;

                for (String auth : authFields) {
                    if (auth.startsWith("Bearer")) {
                        token = auth.substring("Bearer".length() + 1).trim();

                        break;
                    }
                }

                if (token != null) {
                    try {
                        Validation validation = Validator.getInstance().
                                validateAccessToken(new String(Base64.decodeBase64(token.getBytes("UTF-8")),
                                "UTF-8"));

                        if (validation.isValid()) {
                            Set<String> methodRoles = getAllowedRoles();

                            if (methodRoles != null) {
                                Set<String> temp = new HashSet<>(validation.getScopes());
                                temp.removeAll(methodRoles);

                                if (methodRoles.contains("") || temp.size() < validation.getScopes().size()) {
                                    request.setSecurityContext(new OAuthSecurityContext(request.getSecurityContext(),
                                            validation.getUserId(), validation.getScopes()));

                                    log.debug("token {} grants access to method: {} of class: {}",
                                            validation.getId(), info.getResourceMethod().getName(),
                                            info.getResourceClass().getName());
                                } else {
                                    log.debug("token {} has insufficient scope, need one of {} to access method: {} of class: {}",
                                            validation.getId(), methodRoles, info.getResourceMethod().getName(),
                                            info.getResourceClass().getName());

                                    StringBuilder scopeString = new StringBuilder();

                                    for (String scope : methodRoles) {
                                        scopeString.append(scope);
                                        scopeString.append(' ');
                                    }

                                    Response response = Response.status(Response.Status.FORBIDDEN).header("WWW-Authenticate",
                                            "Bearer error=\"insufficient_scope\", error_description=\"The token does not " +
                                                    "have the scopes required for access.\", scope=\""
                                                    + scopeString.toString().trim() + "\"").
                                            entity("").type(MediaType.APPLICATION_JSON).build();

                                    request.abortWith(response);
                                }
                            } else {
                                log.error("no roles defined at method or class level for method: {} in class: {}",
                                        info.getResourceMethod().getName(), info.getResourceClass().getName());

                                Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                                        entity("").type(MediaType.APPLICATION_JSON).build();

                                request.abortWith(response);
                            }
                        } else {
                            log.debug("invalid token: {}", validation.getId());

                            Response response = Response.status(Response.Status.UNAUTHORIZED).header("WWW-Authenticate",
                                    "Bearer error=\"invalid_token\", error_description=\"The token does not exist. " +
                                            "It may have been revoked or expired.\"").
                                    entity("").type(MediaType.APPLICATION_JSON).build();

                            request.abortWith(response);
                        }
                    } catch (Exception e) {

                        log.error("error while contacting authorization server", e);

                        Response response = Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).
                                entity("").type(MediaType.APPLICATION_JSON).build();

                        request.abortWith(response);
                    }
                } else {

                    log.debug("no bearer access token found");

                    Response response = Response.status(Response.Status.UNAUTHORIZED).header("WWW-Authenticate",
                            "Bearer error=\"invalid_token\", error_description=\"Only Bearer tokens are supported\"").
                            entity("").type(MediaType.APPLICATION_JSON).build();

                    request.abortWith(response);
                }
            } else {
                log.debug("no authentication header found");

                Response response = Response.serverError().status(Response.Status.UNAUTHORIZED).header("WWW-Authenticate", "Bearer").
                        entity("").type(MediaType.APPLICATION_JSON).build();

                request.abortWith(response);
            }
        } else {
            log.debug("authorization not required");
        }
    }

    private boolean requiresAuthorization() {
        return info.getResourceMethod().getAnnotation(PermitAll.class) == null;
    }

    private Set<String> getAllowedRoles() {

        RolesAllowed methodRoles = info.getResourceMethod().getAnnotation(RolesAllowed.class);
        RolesAllowed classRoles = info.getResourceClass().getAnnotation(RolesAllowed.class);

        Set<String> roles = new HashSet<>();

        if (methodRoles != null) {
            roles.addAll(Arrays.asList(methodRoles.value()));
        }

        if (classRoles != null) {
            roles.addAll(Arrays.asList(classRoles.value()));
        }

        if (methodRoles != null || classRoles != null) {
            return roles;
        } else {
            return null;
        }
    }
}
