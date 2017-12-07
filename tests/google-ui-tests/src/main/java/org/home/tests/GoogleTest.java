package org.home.tests;

import org.home.steps.NavigationSteps;
import org.home.steps.SearchSteps;
import org.testng.annotations.Test;

public class GoogleTest extends BaseTest {
    @Test(description = "Very first test")
    public void veryFirstGoogleTest() {
        NavigationSteps.openWebApp();
        SearchSteps.performSearchFor("aaa");
        SearchSteps.checkNumberOfResults(10);
    }

    @Test(description = "Second test")
    public void secondGoogleTest() {
        NavigationSteps.openWebApp();
        SearchSteps.performSearchFor("bbb");
        SearchSteps.checkNumberOfResults(10);
    }
}
