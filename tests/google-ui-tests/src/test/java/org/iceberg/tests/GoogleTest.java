package org.iceberg.tests;

import org.iceberg.steps.NavigationSteps;
import org.iceberg.steps.SearchSteps;
import org.iceberg.test_commons.AppComponent;
import org.testng.annotations.Test;

public class GoogleTest extends BaseTest {
    @Test(description = "Very first test", groups = { AppComponent.SEARCH })
    public void veryFirstGoogleTest() {
        NavigationSteps.openWebApp();
        SearchSteps.performSearchFor("aaa");
        SearchSteps.checkNumberOfResults(10);
    }

    @Test(description = "Second test", groups = { AppComponent.SEARCH })
    public void secondGoogleTest() {
        NavigationSteps.openWebApp();
        SearchSteps.performSearchFor("bbb");
        SearchSteps.checkNumberOfResults(5);
    }
}
