package org.iceberg.runner;

import java.io.File;
import java.io.IOException;

import org.qatools.reportportal.ReportPortalTestNGListener;
import org.iceberg.tests.GoogleTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestNGListener;
import org.testng.TestNG;

public class Runner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Runner.class);

    public static void main(String[] args) {
        String role = "leader";
        for (String arg : args) {
            if (arg.contains("=") && arg.split("=")[0].equals("role")) {
                role = arg.split("=")[1];
            }
        }
        LOGGER.info("ROLE: " + role);
        if ("leader".equals(role)) {
            try {
                int exitValue = exec(Runner.class, "role=worker");
                LOGGER.info("exitValue = {}", exitValue);
            } catch (IOException | InterruptedException e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        } else if ("worker".equals(role)) {
            TestNG testNG = new TestNG();
            boolean isRpEnabled = "true".equals(System.getProperty("rp.enable"));
            if (isRpEnabled) {
                testNG.setUseDefaultListeners(false);
                testNG.addListener((ITestNGListener) new ReportPortalTestNGListener());
            }
            testNG.setTestClasses(new Class[] { GoogleTest.class });
            testNG.run();
        }
    }

    public static int exec(Class clazz, String args) throws IOException, InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = clazz.getCanonicalName();

        ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className, args);

        Process process = builder.start();
        process.waitFor();
        return process.exitValue();
    }
}
