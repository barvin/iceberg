package org.qatools.reportportal;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.qatools.rp.ReportPortalClient;
import org.qatools.rp.exceptions.ReportPortalClientException;
import org.qatools.rp.message.HashMarkSeparatedMessageParser;
import org.qatools.rp.message.MessageParser;
import org.qatools.rp.message.ReportPortalMessage;
import org.qatools.rp.message.TypeAwareByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;

public class ReportPortalService implements IReportPortalService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportPortalService.class);
    private static final MessageParser MESSAGE_PARSER = new HashMarkSeparatedMessageParser();
    private static final String RP_ID = "rp_id";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private ReportPortalClient reportPortal;
    private TestContext testContext;
    private boolean needToStartAndFinishLaunch;
    private final String rpLaunchName;
    private final Mode rpMode;
    private final String rpLaunchTags;

    public ReportPortalService() {
        this.rpLaunchName = System.getProperty("rp.launch");
        this.rpMode = Mode.valueOf(System.getProperty("rp.mode"));
        this.rpLaunchTags = System.getProperty("rp.tags");

        String rpBaseUrl = System.getProperty("rp.endpoint");
        String rpUuid = System.getProperty("rp.uuid");
        String rpProject = System.getProperty("rp.project");
        this.reportPortal = new ReportPortalClient(rpBaseUrl, rpProject, rpUuid);
        this.testContext = new TestContext();
        this.needToStartAndFinishLaunch = System.getProperty("rp.launch.id") == null;
    }

    @Override
    public void startLaunch() {
        if (needToStartAndFinishLaunch) {
            StartLaunchRQ rq = new StartLaunchRQ();
            rq.setName(rpLaunchName);
            rq.setMode(rpMode);
            rq.setStartTime(Calendar.getInstance().getTime());
            rq.setTags(RpUtils.parseAsSet(rpLaunchTags));
            try {
                testContext.setLaunchId(reportPortal.startLaunch(rq));
            } catch (ReportPortalClientException e) {
                LOGGER.error("Could not start RP launch", e);
            }
        } else {
            testContext.setLaunchId(System.getProperty("rp.launch.id"));
        }
    }

    @Override
    public void finishLaunch() {
        if (testContext.getCurrentTestId() != null) {
            finishTest();
        }
        if (needToStartAndFinishLaunch) {
            finishSuites();

            FinishExecutionRQ rq = new FinishExecutionRQ();
            rq.setEndTime(Calendar.getInstance().getTime());
            try {
                reportPortal.finishLaunch(testContext.getLaunchId(), rq);
            } catch (ReportPortalClientException e) {
                LOGGER.error("Unable finish the launch: '{}'", testContext.getLaunchId());
            }
        }
    }

    @Override
    public void startTestRow(ITestResult testResult) {
        if (testResult.getAttribute(RP_ID) != null) {
            return;
        }
        createPathIfNeeded(testResult);

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
            rs = reportPortal.startTestStepItem(testContext.getCurrentTestId(), rq);
        } catch (ReportPortalClientException e) {
            LOGGER.error("Unable start test row: '{}'", testName);
        }
        if (rs != null) {
            testResult.setAttribute(RP_ID, rs.getId());
            if (isTestParametrized(testResult)) {
                LOGGER.info("Test data:\n{}", testResult.getParameters()[0].toString());
            }
        }
    }

    @Override
    public void finishTestRow(String status, ITestResult testResult) {
        FinishTestItemRQ rq = new FinishTestItemRQ();
        rq.setEndTime(Calendar.getInstance().getTime());
        rq.setStatus(status);
        try {
            reportPortal.finishTestItem(String.valueOf(testResult.getAttribute(RP_ID)), rq);
        } catch (ReportPortalClientException e) {
            LOGGER.error("Unable finish test row: '{}'", testResult.getAttribute(RP_ID));
        }
    }

    @Override
    public void sendReportPortalMsg(final ITestResult result) {
        ReportPortalClient.emitLog(testItemId -> {
            SaveLogRQ rq = new SaveLogRQ();
            rq.setTestItemId(testItemId);
            rq.setLevel("ERROR");
            rq.setLogTime(Calendar.getInstance().getTime());
            if (result.getThrowable() != null) {
                if (MESSAGE_PARSER.supports(result.getThrowable().getMessage())) {
                    try {
                        ReportPortalMessage rpMessage = MESSAGE_PARSER.parse(result.getThrowable().getMessage());
                        TypeAwareByteSource data = rpMessage.getData();
                        SaveLogRQ.File file = new SaveLogRQ.File();
                        file.setContent(data.read());
                        file.setContentType(data.getMediaType());
                        file.setName(UUID.randomUUID().toString());
                        rq.setFile(file);
                        rq.setMessage(rpMessage.getMessage());
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                } else {
                    rq.setMessage(RpUtils.getFullException(result.getThrowable()));
                }
            } else if (testContext.getConfigurationFailure() != null) {
                Throwable throwable = testContext.getConfigurationFailure();
                rq.setMessage(RpUtils.getFullException(throwable));
                testContext.setConfigurationFailure(null);
            } else {
                rq.setMessage("Test has failed without exception");
            }
            rq.setLogTime(Calendar.getInstance().getTime());
            return rq;
        });
    }

    @Override
    public void saveConfigurationFailure(ITestResult result) {
        testContext.setConfigurationFailure(result.getThrowable());
    }

    protected List<Function<ITestResult, String>> getPathFunctions() {
        return Collections.emptyList();
    }

    protected Set<String> getTestRowTags(ITestResult testResult) {
        return new HashSet<>(Arrays.asList(testResult.getMethod().getGroups()));
    }

    private void finishSuites() {
        if (!getPathFunctions().isEmpty()) {
            FinishTestItemRQ rq = new FinishTestItemRQ();
            rq.setEndTime(Calendar.getInstance().getTime());
            finishAllSuitesRecursively(testContext.getLaunchId(), 0, rq);
        }
    }

    private void finishAllSuitesRecursively(String parentId, int depth, FinishTestItemRQ rq) {
        if (depth == getPathFunctions().size() - 1) {
            List<TestItemResource> suites = getSuites(parentId);
            if (suites != null) {
                for (TestItemResource suite : suites) {
                    if (suite.getStatus().equals(IN_PROGRESS)) {
                        try {
                            reportPortal.finishTestItem(suite.getItemId(), rq);
                        } catch (ReportPortalClientException e) {
                            LOGGER.error("Unable to finish test suite in ReportPortal", e);
                        }
                    }
                }
            }
        } else {
            List<TestItemResource> suites = getSuites(parentId);
            if (suites != null) {
                for (TestItemResource suite : suites) {
                    if (suite.getStatus().equals(IN_PROGRESS)) {
                        finishAllSuitesRecursively(suite.getItemId(), depth + 1, rq);
                        try {
                            reportPortal.finishTestItem(suite.getItemId(), rq);
                        } catch (ReportPortalClientException e) {
                            LOGGER.error("Unable to finish test suite in ReportPortal", e);
                        }
                    }
                }
            }
        }
    }

    private void startTest(String testName, String description) {
        StartTestItemRQ rq = new StartTestItemRQ();
        rq.setName(testName);
        rq.setDescription(description);
        rq.setLaunchId(testContext.getLaunchId());
        rq.setStartTime(Calendar.getInstance().getTime());
        rq.setType("TEST");
        EntryCreatedRS rs;
        try {
            rs = reportPortal.startTestItem(testContext.getCurrentSuiteId(), rq);
            testContext.setCurrentTestId(rs.getId());
            testContext.setCurrentTestStatus(Statuses.PASSED);
        } catch (ReportPortalClientException e) {
            LOGGER.error("Unable start test: '{}'", testName);
        }
    }

    private void finishTest() {
        FinishTestItemRQ rq = new FinishTestItemRQ();
        rq.setEndTime(Calendar.getInstance().getTime());
        rq.setStatus(testContext.getCurrentTestStatus());
        try {
            reportPortal.finishTestItem(testContext.getCurrentTestId(), rq);
            testContext.setCurrentTestId(null);
            testContext.setCurrentTestPath(null);
        } catch (ReportPortalClientException e) {
            LOGGER.error("Unable finish test: '{}'", testContext.getCurrentTestId());
        }
        testContext.setConfigurationFailure(null);
    }

    private void createPathIfNeeded(ITestResult testResult) {
        String parentId = testContext.getLaunchId();
        for (Function<ITestResult, String> function : getPathFunctions()) {
            String suiteName = function.apply(testResult);
            if (!retrieveAndSetCurrentSuiteId(parentId, suiteName)) {
                startsNewSuite(parentId, suiteName);
            }
            parentId = testContext.getCurrentSuiteId();
        }
    }

    private boolean retrieveAndSetCurrentSuiteId(String parentId, String suiteName) {
        List<TestItemResource> suites = getSuites(parentId);
        if (suites != null) {
            for (TestItemResource suite : suites) {
                if (suite.getName().equals(suiteName)) {
                    testContext.setCurrentSuiteId(suite.getItemId());
                    return true;
                }
            }
        }
        return false;
    }

    private List<TestItemResource> getSuites(String parentId) {
        Map<String, String> filter = new HashMap<>();
        filter.put("filter.eq.launch", parentId);
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

    private void startsNewSuite(String parentId, String component) {
        StartTestItemRQ rq = new StartTestItemRQ();
        rq.setLaunchId(parentId);
        rq.setName(component);
        rq.setStartTime(Calendar.getInstance().getTime());
        rq.setType("SUITE");
        try {
            EntryCreatedRS rs = reportPortal.startRootTestItem(rq);
            testContext.setCurrentSuiteId(rs.getId());
        } catch (ReportPortalClientException e) {
            LOGGER.error("Unable to start component suite in ReportPortal", e);
        }
    }

    private String createTestDescription(ITestResult testResult) {
        List<String> groupsWithColon = Arrays.stream(testResult.getMethod().getGroups()).filter(g -> g.contains(":"))
                .collect(Collectors.toList());
        if (!groupsWithColon.isEmpty()) {
            return "[ " + StringUtils.join(groupsWithColon, "; ") + " ]";
        }
        return StringUtils.EMPTY;
    }

    private String createTestRowDescription(ITestResult testResult) {
        if (isTestParametrized(testResult)) {
            return "test data row: " + testContext.getCurrentTestDataRowNumber();
        }
        return StringUtils.EMPTY;
    }

    private boolean isTestParametrized(ITestResult testResult) {
        return testResult.getParameters() != null && testResult.getParameters().length > 0;
    }

}
