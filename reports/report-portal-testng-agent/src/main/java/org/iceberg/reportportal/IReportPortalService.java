package org.iceberg.reportportal;

import org.testng.ITestResult;

public interface IReportPortalService {

    void startLaunch();

    void finishLaunch();

    void startTestRow(ITestResult testResult);

    void finishTestRow(String status, ITestResult testResult);

    void sendReportPortalMsg(final ITestResult result);

    void saveConfigurationFailure(ITestResult result);

}
