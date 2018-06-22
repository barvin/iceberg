package org.qatools.reportportal;

import org.testng.IExecutionListener;
import org.testng.ITestContext;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.internal.IResultListener2;

public class ReportPortalTestNGListener implements ITestNGListener, IExecutionListener, IResultListener2 {

    private IReportPortalService testNGService;

    public ReportPortalTestNGListener() {
    }

    protected void setTestNGService(IReportPortalService testNGService) {
        this.testNGService = testNGService;
    }

    @Override
    public void onExecutionStart() {
        ReportPortalProperties.copyRpPropertiesToSystemProperties();
        if (testNGService == null) {
            if ("true".equals(System.getProperty("rp.enable"))) {
                testNGService = new ReportPortalService();
            } else {
                testNGService = new ReportPortalServiceEmpty();
            }
        }
        testNGService.startLaunch();
    }

    @Override
    public void onExecutionFinish() {
        testNGService.finishLaunch();
    }

    @Override
    public void onStart(ITestContext testContext) {
    }

    @Override
    public void onFinish(ITestContext testContext) {
    }

    @Override
    public void onTestStart(ITestResult testResult) {
        testNGService.startMethod(testResult);
    }

    @Override
    public void onTestSuccess(ITestResult testResult) {
        testNGService.finishMethod(Statuses.PASSED, testResult);
    }

    @Override
    public void onTestFailure(ITestResult testResult) {
        testNGService.sendReportPortalMsg(testResult);
        testNGService.finishMethod(Statuses.FAILED, testResult);
    }

    @Override
    public void onTestSkipped(ITestResult testResult) {
        testNGService.startMethod(testResult);
        testNGService.sendReportPortalMsg(testResult);
        testNGService.finishMethod(Statuses.SKIPPED, testResult);
    }

    @Override
    public void beforeConfiguration(ITestResult testResult) {
    }

    @Override
    public void onConfigurationFailure(ITestResult testResult) {
        testNGService.saveConfigurationFailure(testResult);
    }

    @Override
    public void onConfigurationSuccess(ITestResult testResult) {
    }

    @Override
    public void onConfigurationSkip(ITestResult testResult) {
        testNGService.saveConfigurationFailure(testResult);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }
}
