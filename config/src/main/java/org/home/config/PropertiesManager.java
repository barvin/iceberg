package org.home.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesManager.class);
    private static final String BASE_PROPS_PATH = "props/base.properties";
    private static final String GROUPS_DIR_PATH = "props/groups/";
    private static final String ENVS_DIR_PATH = "props/envs/";

    public void initProperties(TestType testType) {
        try {
            Properties baseProps = new Properties(System.getProperties());
            baseProps.load(getResource(BASE_PROPS_PATH));

            Properties groupProps = new Properties(baseProps);
            InputStream groupProperties = getResource(
                    GROUPS_DIR_PATH + testType.toString().toLowerCase() + ".properties");
            groupProps.load(groupProperties);

            if (System.getProperty("env") != null) {
                Properties envProps = new Properties(groupProps);
                envProps.load(getResource(ENVS_DIR_PATH + System.getProperty("env") + ".properties"));
                System.setProperties(envProps);
            } else {
                System.setProperties(groupProps);
            }

        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }

    }

    private InputStream getResource(String relativePath) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath);
    }
}
