package org.iceberg.tests;

import com.codeborne.selenide.Selenide;
import org.iceberg.steps.NavigationSteps;
import org.iceberg.steps.SearchSteps;
import org.iceberg.test_commons.AppComponent;
import org.testng.annotations.Test;

import java.util.Date;

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
