package org.qatools.reportportal;

import org.testng.ITestResult;

public class ReportPortalServiceEmpty implements IReportPortalService {
    @Override
    public void startLaunch() {
    }

    @Override
    public void finishLaunch() {
    }

    @Override
    public void startMethod(ITestResult testResult) {
    }

    @Override
    public void finishMethod(String status, ITestResult testResult) {
    }

    @Override
    public void sendReportPortalMsg(ITestResult result) {
    }

    @Override
    public void saveConfigurationFailure(ITestResult result) {
    }
}
