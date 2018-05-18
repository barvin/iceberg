package org.iceberg.reportportal;

import org.qatools.reportportal.ReportPortalTestNGListener;

public class IcebergTestNGListener extends ReportPortalTestNGListener {

    public IcebergTestNGListener() {
        super(new IcebergReportPortalService());
    }
}
