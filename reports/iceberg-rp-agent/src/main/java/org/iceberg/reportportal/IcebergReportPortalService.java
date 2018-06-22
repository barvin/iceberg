package org.iceberg.reportportal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.iceberg.test_commons.AppComponent;
import org.qatools.reportportal.ReportPortalService;
import org.testng.ITestResult;

import com.google.common.collect.Sets;

public class IcebergReportPortalService extends ReportPortalService {
    private static final String DEFAULT_COMPONENT = "Undefined component";

    @Override
    protected List<Function<ITestResult, String>> getPathFunctions() {
        return Collections.singletonList(testResult ->
                Arrays.stream(testResult.getMethod().getGroups()).filter(AppComponent::isComponent).findFirst()
                        .orElse(DEFAULT_COMPONENT)
        );
    }

    @Override
    protected Set<String> createTestStepTags(ITestResult testResult) {
        Set<String> tags = Sets.newHashSet();
        List<String> groupsList = Arrays.asList(testResult.getMethod().getGroups());
        groupsList.stream().filter(AppComponent::isComponent).forEach(c -> tags.add("component:" + c));
        return tags;
    }

}
