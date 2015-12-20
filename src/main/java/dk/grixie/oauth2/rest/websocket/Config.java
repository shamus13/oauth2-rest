package dk.grixie.oauth2.rest.websocket;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.util.HashSet;
import java.util.Set;

public class Config implements ServerApplicationConfig {

    @Override
    public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> scanned) {

        return new HashSet<>();
    }


    @Override
    public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
        Set<Class<?>> results = new HashSet<>();

        for (Class<?> clazz : scanned) {
            if (clazz.getDeclaredAnnotation(ServerEndpoint.class) != null) {
                results.add(clazz);
            }
        }

        return results;
    }
}
