package dk.grixie.oauth2.rest.websocket.endpoint;

import dk.grixie.oauth2.rest.oauth2.Validator;
import dk.grixie.oauth2.rest.oauth2.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@PermitAll
//@RolesAllowed({"USER_ID", "FULL_ACCESS"})
@ServerEndpoint(value = "/websocket/echo")
public class Echo {
    private static final Logger log = LoggerFactory.getLogger(Echo.class);

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        log.debug("Opened websocket for session:{}", session.getId());

        Map<String, List<String>> parameters = session.getRequestParameterMap();

        for (String key : parameters.keySet()) {
            log.debug("key: {} values:{}", key, parameters.get(key));
        }

        if (requiresAuthorization()) {
            String accessTokenId = null;

            if (parameters.get("access_token") != null) {
                for (String id : parameters.get("access_token")) {
                    if (id != null) {
                        accessTokenId = id;
                    }
                }
            }

            CloseReason closeReason = checkAuthorization(accessTokenId);

            if (closeReason != null) {
                try {
                    session.close(closeReason);
                } catch (Exception dummy) {
                    //nothing to do here
                }
            }
        } else {
            log.debug("authorization not required");
        }
    }

    @OnMessage
    public void echoMessage(Session session, String msg, boolean last) {
        log.debug("received message:'{}' for session:{}", msg, session.getId());

        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(msg, last);
            }
        } catch (IOException e) {
            try {
                session.close();
            } catch (IOException e1) {
                // Ignore
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.debug("closed websocket for session:{} code: {} reason:{}",
                session.getId(), closeReason.getCloseCode().getCode(), closeReason.getReasonPhrase());
    }

    @OnMessage
    public void echoPongMessage(PongMessage pm) {
        // NO-OP
    }


    private CloseReason checkAuthorization(String accessTokenId) {
        CloseReason closeReason = null;

        if (accessTokenId != null) {
            try {
                Validation validation = Validator.getInstance().validateAccessToken(accessTokenId);

                if (validation.isValid()) {
                    Set<String> temp = new HashSet<>(validation.getScopes());
                    Set<String> roles = getAllowedRoles();
                    temp.removeAll(roles);

                    if (roles.contains("") || temp.size() < validation.getScopes().size()) {
                        log.debug("token {} grants access", validation.getId());
                    } else {
                        log.debug("token {} has insufficient scope, need one of {}",
                                validation.getId(), roles);

                        closeReason = new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY,
                                "insufficient scope");
                    }
                } else {
                    log.debug("invalid token: {}", validation.getId());

                    closeReason = new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY,
                            "invalid token");

                }
            } catch (Exception e) {
                log.error("error while contacting authorization server", e);

                closeReason = new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION,
                        "error while contacting authorization server");
            }

        } else {
            log.debug("no bearer access token found");

            closeReason = new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY,
                    "no bearer access token found");
        }

        return closeReason;
    }

    private boolean requiresAuthorization() {
        return getClass().getAnnotation(PermitAll.class) == null;
    }

    private Set<String> getAllowedRoles() {

        RolesAllowed classRoles = getClass().getAnnotation(RolesAllowed.class);

        Set<String> roles = new HashSet<>();

        if (classRoles != null) {
            roles.addAll(Arrays.asList(classRoles.value()));
        }

        if (classRoles != null) {
            return roles;
        } else {
            return null;
        }
    }
}
