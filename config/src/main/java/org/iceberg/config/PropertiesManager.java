package org.iceberg.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesManager.class);
    private static final String BASE_PROPS_PATH = "props/base.properties";
    private static final String ENVS_DIR_PATH = "props/envs/";

    public static void loadProperties() {
        try {
            Properties baseProps = new Properties();
            baseProps.load(getResource(BASE_PROPS_PATH));

            Properties envProps = new Properties();
            if (System.getProperty("env") != null) {
                envProps.load(getResource(ENVS_DIR_PATH + System.getProperty("env") + ".properties"));
            }
            Properties propsFromPropFiles = mergePropertiesOverridingPrevious(baseProps, envProps);
            addToSystemPropertiesWithoutOverriding(propsFromPropFiles);

        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    private static void addToSystemPropertiesWithoutOverriding(Properties propsToAdd) {
        propsToAdd.forEach((propName, propValue) -> {
            if (System.getProperty(String.valueOf(propName)) == null) {
                System.setProperty(String.valueOf(propName), String.valueOf(propValue));
            }
        });
    }

    private static InputStream getResource(String relativePath) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath);
    }

    private static Properties mergePropertiesOverridingPrevious(Properties... propList) {
        Properties result = new Properties();
        Arrays.stream(propList).forEach(properties -> properties
                .forEach((key, value) -> result.setProperty(String.valueOf(key), String.valueOf(value))));
        return result;
    }
}
