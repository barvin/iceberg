package org.home.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesManager.class);

    public void initProperties() {
        try {
            Properties baseProps = new Properties();
            baseProps.load(getInputStream("props/base.properties"));

            Properties groupProps = new Properties(baseProps);
            groupProps.load(getInputStream("props/groups/web.properties"));

            Properties envProps = new Properties(groupProps);
            envProps.load(getInputStream("props/envs/qa.properties"));

            for (String propName : envProps.stringPropertyNames()) {
                if (!System.getProperties().stringPropertyNames().contains(propName)) {
                    System.setProperty(propName, envProps.getProperty(propName));
                }
            }

            LOGGER.debug("SYSTEM PROPERTIES:");
            for (String propName : System.getProperties().stringPropertyNames()) {
                LOGGER.debug("{} = {}", propName, System.getProperty(propName));
            }

        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }

    }

    private InputStream getInputStream(String relativePath) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath);
    }
}
