package com.user_service.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures HTTP to HTTPS redirection using Tomcat.
 */
@Configuration
public class WebServerConfig {

    @Bean
    public ServletWebServerFactory webServerFactoryWithRedirect() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addAdditionalTomcatConnectors(httpToHttpsRedirectConnector());
        return factory;
    }

    private Connector httpToHttpsRedirectConnector() {
        Connector httpConnector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        httpConnector.setScheme("http");
        httpConnector.setPort(8081);              // Accept HTTP on port 8080
        httpConnector.setSecure(false);
        httpConnector.setRedirectPort(8443);      // Redirect to HTTPS on port 8443
        return httpConnector;
    }
}
