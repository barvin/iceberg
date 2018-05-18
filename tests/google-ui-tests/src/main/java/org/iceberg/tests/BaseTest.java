package org.iceberg.tests;

import org.iceberg.config.PropertiesManager;
import org.testng.annotations.BeforeSuite;

public abstract class BaseTest {
    @BeforeSuite
    public void beforeSuite() {
        PropertiesManager.loadProperties();
    }
}
