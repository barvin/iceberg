package org.home.reportportal;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.qatools.rp.ReportPortalClient;
import org.qatools.rp.exceptions.ReportPortalClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IAttributes;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;

public class ReportPortalService {
    private static final String RP_ID = "rp_id";
    private ReportPortalClient reportPortal;
    private String launchId;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportPortalService.class);
    private static final String RP_BASE_URL = System.getProperty("rp.endpoint");
    private static final String RP_UUID = System.getProperty("rp.uuid");
    private static final String RP_PROJECT = System.getProperty("rp.project");
    private static final String RP_LAUNCH_NAME = System.getProperty("rp.launch");
    private static final Mode RP_MODE = Mode.valueOf(System.getProperty("rp.mode"));
    private static final String RP_LAUNCH_TAGS = System.getProperty("rp.tags");

    public ReportPortalService() {
        this.reportPortal = new ReportPortalClient(RP_BASE_URL, RP_PROJECT, RP_UUID);
    }

    public void startLaunch() {
        StartLaunchRQ rq = new StartLaunchRQ();
        rq.setName(RP_LAUNCH_NAME);
        rq.setMode(RP_MODE);
        rq.setStartTime(Calendar.getInstance().getTime());
        rq.setTags(RpUtils.parseAsSet(RP_LAUNCH_TAGS));
        try {
            this.launchId = reportPortal.startLaunch(rq);
        } catch (ReportPortalClientException e) {
            LOGGER.error("Could not start RP launch", e);
        }
    }

    public void finishLaunch() {
        FinishExecutionRQ rq = new FinishExecutionRQ();
        rq.setEndTime(Calendar.getInstance().getTime());
        try {
            reportPortal.finishLaunch(launchId, rq);
        } catch (ReportPortalClientException e) {
            LOGGER.error("Could not finish RP launch", e);
        }
    }

    public void startTest(ITestContext testContext) {
//        StartTestItemRQ rq = buildStartTestItemRq(testContext);
//
//        final Maybe<String> testID = reportPortal
//                .startTestItem(this.<Maybe<String>>getAttribute(testContext.getSuite(), RP_ID), rq);
//
//        testContext.setAttribute(RP_ID, testID);
        LOGGER.info("startTest " + testContext.getName());
    }

    public void finishTest(ITestContext testContext) {
//        FinishTestItemRQ rq = new FinishTestItemRQ();
//        rq.setEndTime(testContext.getEndDate());
//        String status = isTestPassed(testContext) ? Statuses.PASSED : Statuses.FAILED;
//
//        rq.setStatus(status);
//        reportPortal.finishTestItem(this.<Maybe<String>>getAttribute(testContext, RP_ID), rq);

        LOGGER.info("finishTest " + testContext.getName());
    }

    public void startTestMethod(ITestResult testResult) {
//        StartTestItemRQ rq = buildStartStepRq(testResult);
//        if (rq == null)
//            return;
//        Maybe<String> stepMaybe = reportPortal
//                .startTestItem(this.<Maybe<String>>getAttribute(testResult.getTestContext(), RP_ID), rq);
//
//        testResult.setAttribute(RP_ID, stepMaybe);
        LOGGER.info("startTestMethod " + testResult.getName());
    }

    public void finishTestMethod(String status, ITestResult testResult) {
//        final Date now = Calendar.getInstance().getTime();
//        FinishTestItemRQ rq = new FinishTestItemRQ();
//        rq.setEndTime(now);
//        rq.setStatus(status);
//        // Allows indicate that SKIPPED is not to investigate items for WS
//        if (status.equals(Statuses.SKIPPED) && !parameters.getSkippedAnIssue()) {
//            Issue issue = new Issue();
//            issue.setIssueType(NOT_ISSUE);
//            rq.setIssue(issue);
//        }
//
//        reportPortal.finishTestItem(this.<Maybe<String>>getAttribute(testResult, RP_ID), rq);
        LOGGER.info("finishTestMethod " + testResult.getName());
        LOGGER.info("    with status " + status);
    }

    public void sendReportPortalMsg(final ITestResult result) {
        SaveLogRQ rq = new SaveLogRQ();
        rq.setTestItemId(String.valueOf(result.getAttribute(RP_ID)));
        rq.setLevel("ERROR");
        rq.setLogTime(Calendar.getInstance().getTime());
        if (result.getThrowable() != null) {
            rq.setMessage(RpUtils.getStackTraceString(result.getThrowable()));
        } else
            rq.setMessage("Test has failed without exception");
        rq.setLogTime(Calendar.getInstance().getTime());
        try {
            reportPortal.log(rq);
        } catch (ReportPortalClientException e) {
            LOGGER.error("Could not sent log to RP", e);
        }
    }

    /**
     * Extension point to customize test creation event/request
     *
     * @param testContext TestNG test context
     * @return Request to ReportPortal
     */
    protected StartTestItemRQ buildStartTestItemRq(ITestContext testContext) {
        StartTestItemRQ rq = new StartTestItemRQ();
        rq.setName(testContext.getName());
        rq.setStartTime(Calendar.getInstance().getTime());
        rq.setType("TEST");
        return rq;
    }

    /**
     * Extension point to customize test step creation event/request
     *
     * @param testResult TestNG's testResult context
     * @return Request to ReportPortal
     */
    protected StartTestItemRQ buildStartStepRq(ITestResult testResult) {
        if (testResult.getAttribute(RP_ID) != null) {
            return null;
        }
        StartTestItemRQ rq = new StartTestItemRQ();
        String testStepName = testResult.getMethod().getMethodName();
        rq.setName(testStepName);

        rq.setDescription(createStepDescription(testResult));
        rq.setStartTime(Calendar.getInstance().getTime());
        rq.setType("STEP");
        return rq;
    }

    private String createStepDescription(ITestResult testResult) {
        StringBuilder stringBuffer = new StringBuilder();
        if (testResult.getMethod().getDescription() != null) {
            stringBuffer.append(testResult.getMethod().getDescription());
        }
        if (testResult.getParameters() != null && testResult.getParameters().length != 0) {
            stringBuffer.append(" [ ");
            for (Object parameter : testResult.getParameters()) {
                stringBuffer.append(" ");
                stringBuffer.append(parameter);
                stringBuffer.append(" |");
            }
            stringBuffer.deleteCharAt(stringBuffer.lastIndexOf("|"));
            stringBuffer.append(" ] ");
        }
        return stringBuffer.toString();
    }

}
