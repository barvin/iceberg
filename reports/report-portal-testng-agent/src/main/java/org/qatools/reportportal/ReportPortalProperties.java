package org.qatools.reportportal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ReportPortalProperties {
    private static final String RP_PROPS_FILE_PATH = "reportportal.properties";

    private ReportPortalProperties() {
    }

    static void copyRpPropertiesToSystemProperties() {
        try {
            Properties rpProperties = new Properties();
            InputStream iStream = getRpPropertiesFileResource();
            if (iStream != null) {
                rpProperties.load(iStream);
                addToSystemPropertiesWithoutOverriding(rpProperties);
            }
        } catch (IOException e) {
            // do nothing
        }
    }

    private static void addToSystemPropertiesWithoutOverriding(Properties propsToAdd) {
        propsToAdd.forEach((propName, propValue) -> {
            if (System.getProperty(String.valueOf(propName)) == null) {
                System.setProperty(String.valueOf(propName), String.valueOf(propValue));
            }
        });
    }

    private static InputStream getRpPropertiesFileResource() {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(RP_PROPS_FILE_PATH);
    }
}
