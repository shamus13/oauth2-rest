package dk.grixie.oauth2.rest.listener;

import dk.grixie.oauth2.rest.oauth2.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

@WebListener
public class InitializationContextListener implements ServletContextListener {
    private static final Logger log = LoggerFactory.getLogger(InitializationContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
        log.info("initializing context {}", event.getServletContext().getContextPath().substring(1));

        final String name = lookup(null, "name");
        final String version = lookup(null, "version");
        final String buildTime = lookup(null, "build-time");

        log.info("application name: {}", name);
        log.info("application version: {}", version);
        log.info("application build: {}", buildTime);

        final String clientId = lookup(name, "client-id");
        final String clientPassword = lookup(name, "client-password");
        final String validateUrl = lookup(name, "validation-endpoint");

        log.info("oauth client id: {}", clientId);
        log.info("oauth client password: {}", clientPassword);
        log.info("oauth validate url: {}", validateUrl);

        new Timer(true).scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            Validator.initialize(clientId, clientPassword, new URL(validateUrl));

                            cancel();

                            log.info("service initialized");

                        } catch (Exception e) {
                            log.error("failed to initialize service", e);
                        }
                    }
                }, 0, 5 * 60 * 1000);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        log.info("shutting down {}", event.getServletContext().getContextPath().substring(1));
    }

    private String lookup(String product, String key) {
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");

            return (String) envContext.lookup(product != null ? product + "/" + key : key);
        } catch (NamingException e) {
            return null;
        }
    }
}

