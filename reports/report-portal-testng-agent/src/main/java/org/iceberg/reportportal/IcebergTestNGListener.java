package org.iceberg.reportportal;

import org.iceberg.config.PropertiesManager;
import org.testng.IExecutionListener;
import org.testng.ITestContext;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.internal.IResultListener2;

public class IcebergTestNGListener implements ITestNGListener, IExecutionListener, IResultListener2 {

    private IReportPortalService testNGService;

    public IcebergTestNGListener() {
        PropertiesManager.loadReportPortalProperties();
        if ("true".equals(System.getProperty("rp.enable"))) {
            this.testNGService = new ReportPortalServiceImpl();
        } else {
            this.testNGService = new ReportPortalServiceEmpty();
        }
    }

    @Override
    public void onExecutionStart() {
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
        testNGService.startTestRow(testResult);
    }

    @Override
    public void onTestSuccess(ITestResult testResult) {
        testNGService.finishTestRow(Statuses.PASSED, testResult);
    }

    @Override
    public void onTestFailure(ITestResult testResult) {
        testNGService.sendReportPortalMsg(testResult);
        testNGService.finishTestRow(Statuses.FAILED, testResult);
    }

    @Override
    public void onTestSkipped(ITestResult testResult) {
        testNGService.startTestRow(testResult);
        testNGService.sendReportPortalMsg(testResult);
        testNGService.finishTestRow(Statuses.SKIPPED, testResult);
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
