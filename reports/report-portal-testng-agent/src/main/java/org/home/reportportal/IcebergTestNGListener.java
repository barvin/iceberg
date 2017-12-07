package org.home.reportportal;

import org.testng.IExecutionListener;
import org.testng.ITestContext;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.internal.IResultListener2;

public class IcebergTestNGListener implements ITestNGListener, IExecutionListener, IResultListener2 {

    private ReportPortalService testNGService;

    public IcebergTestNGListener() {
        this.testNGService = new ReportPortalService();
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
        testNGService.startTest(testContext);
    }

    @Override
    public void onFinish(ITestContext testContext) {
        testNGService.finishTest(testContext);
    }

    @Override
    public void onTestStart(ITestResult testResult) {
        testNGService.startTestMethod(testResult);
    }

    @Override
    public void onTestSuccess(ITestResult testResult) {
        testNGService.finishTestMethod(Statuses.PASSED, testResult);
    }

    @Override
    public void onTestFailure(ITestResult testResult) {
        testNGService.sendReportPortalMsg(testResult);
        testNGService.finishTestMethod(Statuses.FAILED, testResult);
    }

    @Override
    public void onTestSkipped(ITestResult testResult) {
        testNGService.startTestMethod(testResult);
        testNGService.finishTestMethod(Statuses.SKIPPED, testResult);
    }

    @Override
    public void beforeConfiguration(ITestResult testResult) {
//        testNGService.startConfiguration(testResult);
    }

    @Override
    public void onConfigurationFailure(ITestResult testResult) {
        testNGService.sendReportPortalMsg(testResult);
        testNGService.finishTestMethod(Statuses.FAILED, testResult);
    }

    @Override
    public void onConfigurationSuccess(ITestResult testResult) {
        testNGService.finishTestMethod(Statuses.PASSED, testResult);
    }

    @Override
    public void onConfigurationSkip(ITestResult testResult) {
//        testNGService.startConfiguration(testResult);
//        testNGService.finishTestMethod(Statuses.SKIPPED, testResult);
    }

    // this action temporary doesn't supported by report portal
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        testNGService.finishTestMethod(Statuses.FAILED, result);
    }
}
