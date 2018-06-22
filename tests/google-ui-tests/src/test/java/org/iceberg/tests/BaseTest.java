package org.iceberg.tests;

import com.codeborne.selenide.Selenide;
import org.iceberg.config.PropertiesManager;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public abstract class BaseTest {
    @BeforeSuite
    public void beforeSuite() {
        PropertiesManager.loadProperties();
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        Selenide.close();
    }
}
