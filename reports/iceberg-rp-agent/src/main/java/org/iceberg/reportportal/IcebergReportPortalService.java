package org.iceberg.reportportal;

import org.iceberg.test_commons.AppComponent;
import org.qatools.reportportal.ReportPortalService;
import org.testng.ITestResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class IcebergReportPortalService extends ReportPortalService {
    private static final String DEFAULT_COMPONENT = "Undefined component";

    @Override
    protected List<Function<ITestResult, String>> getPathFunctions() {
        return Collections.singletonList(testResult ->
                Arrays.stream(testResult.getMethod().getGroups()).filter(AppComponent::isComponent).findFirst()
                        .orElse(DEFAULT_COMPONENT)
        );
    }
}
