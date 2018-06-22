package org.qatools.reportportal;

import org.testng.ITestResult;

public interface IReportPortalService {

    void startLaunch();

    void finishLaunch();

    void startMethod(ITestResult testResult);

    void finishMethod(String status, ITestResult testResult);

    void sendReportPortalMsg(final ITestResult result);

    void saveConfigurationFailure(ITestResult result);

}
