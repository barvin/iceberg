package org.iceberg.reportportal;

import org.iceberg.config.PropertiesManager;
import org.qatools.reportportal.ReportPortalTestNGListener;

public class IcebergTestNGListener extends ReportPortalTestNGListener {

    public IcebergTestNGListener() {
        PropertiesManager.loadProperties();
        setTestNGService(new IcebergReportPortalService());
    }
}
