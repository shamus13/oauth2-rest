package dk.grixie.oauth2.rest.rest;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class OAuthSecurityContext implements SecurityContext {

    private SecurityContext parent;
    private Principal principal;
    private Set<String> scopes;

    public OAuthSecurityContext(final SecurityContext parent, final String uid, final Collection<String> scopes) {
        this.parent = parent;
        this.principal = new Principal() {
            @Override
            public String getName() {
                return uid;
            }
        };
        this.scopes = new HashSet<>(scopes);
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return scopes.contains(role);
    }

    @Override
    public boolean isSecure() {
        return parent.isSecure();
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}
