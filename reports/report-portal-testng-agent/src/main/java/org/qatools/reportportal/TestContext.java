package org.qatools.reportportal;

/**
 * Context for TestNG Listener
 * 
 */
public class TestContext {

    private String launchName;
    private String launchId;
    private String currentSuiteId;
    private String currentTestPath;
    private String currentTestId;
    private String currentTestStatus;
    private int currentTestDataRowNumber;
    private Throwable configurationFailure;

    public String getLaunchName() {
        return launchName;
    }

    public void setLaunchName(String launchName) {
        this.launchName = launchName;
    }

    public String getLaunchId() {
        return launchId;
    }

    public void setLaunchId(String launchId) {
        this.launchId = launchId;
    }

    public String getCurrentSuiteId() {
        return currentSuiteId;
    }

    public void setCurrentSuiteId(String currentSuiteId) {
        this.currentSuiteId = currentSuiteId;
    }

    public String getCurrentTestPath() {
        return currentTestPath;
    }

    public void setCurrentTestPath(String currentTestPath) {
        this.currentTestPath = currentTestPath;
    }

    public void setCurrentTestId(String currentTestId) {
        this.currentTestId = currentTestId;
    }

    public String getCurrentTestId() {
        return currentTestId;
    }

    public String getCurrentTestStatus() {
        return currentTestStatus;
    }

    public void setCurrentTestStatus(String currentTestStatus) {
        this.currentTestStatus = currentTestStatus;
    }

    public Throwable getConfigurationFailure() {
        return configurationFailure;
    }

    public void setConfigurationFailure(Throwable configurationFailure) {
        this.configurationFailure = configurationFailure;
    }

    public int getCurrentTestDataRowNumber() {
        return currentTestDataRowNumber;
    }

    public void setCurrentTestDataRowNumber(int currentTestDataRowNumber) {
        this.currentTestDataRowNumber = currentTestDataRowNumber;
    }

    public void nextTestDataRowNumber() {
        currentTestDataRowNumber++;
    }
}
