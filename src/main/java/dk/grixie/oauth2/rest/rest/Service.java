package dk.grixie.oauth2.rest.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/rest")
public class Service extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> returnValue = new HashSet<>();

        returnValue.add(OptionsRequestFilter.class);
        returnValue.add(OAuth2RequestFilter.class);
        returnValue.add(OAuth2ResponseFilter.class);
        returnValue.add(Resource.class);

        return returnValue;
    }

}
