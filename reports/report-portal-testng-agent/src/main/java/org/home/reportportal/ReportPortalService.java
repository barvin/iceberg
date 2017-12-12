package org.home.reportportal;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.epam.reportportal.service.ReportPortal;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import org.home.reportportal.exceptions.RpException;
import org.iceberg.test_commons.Component;
import org.qatools.rp.ReportPortalClient;
import org.qatools.rp.exceptions.ReportPortalClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;

public class ReportPortalService {
    private static final String RP_ID = "rp_id";
    private static final boolean SINGLE_THREAD_LAUNCH = "true".equals(System.getProperty("rp.single.thread.launch"));
    private static final String DEFAULT_COMPONENT = "Undefined component";
    private ReportPortalClient reportPortal;
    private TestContext testContext;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportPortalService.class);
    private static final String RP_BASE_URL = System.getProperty("rp.endpoint");
    private static final String RP_UUID = System.getProperty("rp.uuid");
    private static final String RP_PROJECT = System.getProperty("rp.project");
    private static final String RP_LAUNCH_NAME = System.getProperty("rp.launch");
    private static final Mode RP_MODE = Mode.valueOf(System.getProperty("rp.mode"));
    private static final String RP_LAUNCH_TAGS = System.getProperty("rp.tags");

    public ReportPortalService() {
        this.reportPortal = new ReportPortalClient(RP_BASE_URL, RP_PROJECT, RP_UUID);
        this.testContext = new TestContext();
    }

    public void startLaunch() {
        if (System.getProperty("rp.launch.id") == null) {
            if (!SINGLE_THREAD_LAUNCH) {
                Map<String, Object> filter = new HashMap<>();
                filter.put("filter.eq.name", RP_LAUNCH_NAME);
                if (RP_LAUNCH_TAGS != null) {
                    filter.put("filter.has.tags", RP_LAUNCH_TAGS.replace(";", ","));
                }
                filter.put("page.sort", "start_time,DESC");
                List<LaunchResource> launches = null;
                try {
                    launches = reportPortal.getLaunches(filter);
                } catch (ReportPortalClientException e) {
                    throw new RpException("Unable to get list of launches from ReportPortal", e);
                }
                for (LaunchResource launch : launches) {
                    LOGGER.info("RP Launch found: name: '{}', number: {}, status: {}", launch.getName(), launch.getNumber(),
                            launch.getStatus());
                    if ("IN_PROGRESS".equals(launch.getStatus())) {
                        testContext.setLaunchId(launch.getLaunchId());
                        return;
                    }
                }
            }

            StartLaunchRQ rq = new StartLaunchRQ();
            rq.setName(RP_LAUNCH_NAME);
            rq.setMode(RP_MODE);
            rq.setStartTime(Calendar.getInstance().getTime());
            rq.setTags(RpUtils.parseAsSet(RP_LAUNCH_TAGS));
            try {
                testContext.setLaunchId(reportPortal.startLaunch(rq));
            } catch (ReportPortalClientException e) {
                LOGGER.error("Could not start RP launch", e);
            }
        } else {
            testContext.setLaunchId(System.getProperty("rp.launch.id"));
        }
        com.epam.reportportal.service.ReportPortalClient client = new ReportPortal()
    }

    public void finishLaunch() {
        FinishExecutionRQ rq = new FinishExecutionRQ();
        rq.setEndTime(Calendar.getInstance().getTime());
        try {
            reportPortal.finishLaunch(testContext.getLaunchId(), rq);
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
        if (testResult.getAttribute(RP_ID) != null) {
            return;
        }

        String component = getComponentFromTest(testResult);
        if (!retrieveAndSetCurrentComponentSuiteId(component)) {
            startsNewComponentSuite(component);
        }

        String testPath = testResult.getTestClass().getName() + testResult.getMethod().getMethodName();
        String testName = testResult.getMethod().getDescription();
        if (!testPath.equals(testContext.getCurrentTestPath())) {
            if (testContext.getCurrentTestPath() != null) {
                finishTest();
            }
            startTest(testName, createTestDescription(testResult));
            testContext.setCurrentTestPath(testPath);
            testContext.setCurrentTestDataRowNumber(1);
        } else {
            testContext.nextTestDataRowNumber();
        }

        StartTestItemRQ rq = new StartTestItemRQ();
        rq.setName(testName);
        rq.setLaunchId(testContext.getLaunchId());
        rq.setDescription(createTestRowDescription(testResult));
        rq.setStartTime(Calendar.getInstance().getTime());
        rq.setType("STEP");
        rq.setTags(getTestRowTags(testResult));
        EntryCreatedRS rs = null;
        try {
            rs = reportPortal.startTestItem(testContext.getCurrentTestId(), rq);
        } catch (ReportPortalClientException e) {
            LOGGER.error("Unable start test row: '{}'", testName);
        }
        if (rs != null) {
            testResult.setAttribute(RP_ID, rs.getId());
            ReportPortalListenerContext.setRunningNowItemId(rs.getId());
            if (isTestParametrized(testResult)) {
                RpUtils.printExamplesTableRow(testResult.getParameters()[0].toString());
                RpUtils.printNestedExampleTables(((TestDataMap) testResult.getParameters()[0]).getParameters());
            }
        }
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

    private String getComponentFromTest(ITestResult testResult) {
        return Arrays.stream(testResult.getMethod().getGroups()).filter(Component::isComponent).findFirst()
                .orElse(DEFAULT_COMPONENT);
    }

    private boolean retrieveAndSetCurrentComponentSuiteId(String component) {
        List<TestItemResource> componentSuites = getComponentSuites(testContext.getCurrentComponentSuiteId());
        if (componentSuites != null) {
            for (TestItemResource componentSuite : componentSuites) {
                if (componentSuite.getName().equals(component)) {
                    testContext.setCurrentComponentSuiteId(componentSuite.getItemId());
                    return true;
                }
            }
        }
        return false;
    }

    private List<TestItemResource> getComponentSuites(String teamSuiteId) {
        Map<String, String> filter = new HashMap<>();
        filter.put("filter.eq.parent", teamSuiteId);
        filter.put("page.page", "1");
        filter.put("page.size", "50");
        filter.put("page.sort", "start_time,ASC");
        try {
            return reportPortal.getAllTestItems(filter);
        } catch (ReportPortalClientException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private void startsNewComponentSuite(String component) {
        StartTestItemRQ rq = new StartTestItemRQ();
        rq.setLaunchId(testContext.getLaunchId());
        rq.setName(component);
        rq.setStartTime(Calendar.getInstance().getTime());
        rq.setType("SUITE");
        try {
            EntryCreatedRS rs = reportPortal.startRootTestItem(rq);
            testContext.setCurrentComponentSuiteId(rs.getId());
            testContext.addComponentSuiteToCache(rs.getId(), SINGLE_THREAD_LAUNCH);
        } catch (ReportPortalClientException e) {
            LOGGER.error("Unable to start component suite in ReportPortal", e);
        }
    }

}
