package org.home.reportportal;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Context for TestNG Listener
 * 
 */
public class TestContext {

    private String launchName;
    private String launchId;
    private String currentComponentSuiteId;
    private String currentTestPath;
    private String currentTestId;
    private String currentTestStatus;
    private int currentTestDataRowNumber;
    private Throwable configurationFailure;
    private Deque<String> teamSuitesCache = new LinkedList<>();
    private Deque<String> componentSuitesCache = new LinkedList<>();

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

    public String getCurrentComponentSuiteId() {
        return currentComponentSuiteId;
    }

    public void setCurrentComponentSuiteId(String currentComponentSuiteId) {
        this.currentComponentSuiteId = currentComponentSuiteId;
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

    public Deque<String> getTeamSuitesCache() {
        return teamSuitesCache;
    }

    public void addTeamSuiteToCache(String suiteId, boolean isSingleThredLanch) {
        if (isSingleThredLanch) {
            teamSuitesCache.push(suiteId);
        }
    }

    public Deque<String> getComponentSuitesCache() {
        return componentSuitesCache;
    }

    public void addComponentSuiteToCache(String suiteId, boolean isSingleThredLanch) {
        if (isSingleThredLanch) {
            componentSuitesCache.push(suiteId);
        }
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
