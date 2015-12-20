package dk.grixie.oauth2.rest.oauth2;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Validation {
    private final String id;
    private final boolean valid;
    private final String userId;
    private final long expiresAt;
    private final Set<String> scopes;

    public Validation(final String id, final boolean valid, final String userId, final long expiresAt,
                       final Collection<String> scopes) {
        this.id = id;
        this.valid = valid;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.scopes = Collections.unmodifiableSet(new HashSet<>(scopes));
    }

    public String getId() {
        return id;
    }

    public boolean isValid() {
        return valid;
    }

    public String getUserId() {
        return userId;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public Set<String> getScopes() {
        return scopes;
    }
}
