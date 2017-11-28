package org.home.tests;

import org.home.config.PropertiesManager;
import org.home.config.TestType;
import org.testng.annotations.BeforeSuite;

public abstract class BaseTest {

    @BeforeSuite()
    public void beforeSuite() {
        new PropertiesManager().initProperties(TestType.WEB);
    }

}
