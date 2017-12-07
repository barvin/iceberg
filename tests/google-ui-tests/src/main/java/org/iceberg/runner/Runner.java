package org.iceberg.runner;

import org.home.config.PropertiesManager;
import org.home.config.TestType;
import org.home.reportportal.IcebergTestNGListener;
import org.home.tests.GoogleTest;
import org.testng.ITestNGListener;
import org.testng.TestNG;

public class Runner {
    public static void main(String[] args) {
        new PropertiesManager().initProperties(TestType.WEB);

        TestNG testNG = new TestNG();
        boolean isRpEnabled = "true".equals(System.getProperty("rp.enable"));
        if (isRpEnabled) {
            testNG.setUseDefaultListeners(false);
            testNG.addListener((ITestNGListener) new IcebergTestNGListener());
        }
        testNG.setTestClasses(new Class[] { GoogleTest.class });
        testNG.run();
    }
}
