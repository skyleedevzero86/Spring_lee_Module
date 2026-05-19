package com.sleekydz86.monitoring.logstack_s3.config;

import java.io.File;

import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JspWebConfig {

    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> jspWebServerCustomizer() {
        return factory -> {
            if (!(factory instanceof TomcatServletWebServerFactory tomcatFactory)) {
                return;
            }
            File webapp = new File("src/main/webapp");
            if (!webapp.isDirectory()) {
                return;
            }
            tomcatFactory.addContextCustomizers(context -> {
                StandardRoot resources = new StandardRoot(context);
                resources.addPreResources(
                        new DirResourceSet(resources, "/WEB-INF", webapp.getAbsolutePath() + "/WEB-INF", "/"));
                context.setResources(resources);
            });
        };
    }
}
