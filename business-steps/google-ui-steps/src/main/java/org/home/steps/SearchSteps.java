package org.home.steps;

import org.home.pages.GooglePage;

public class SearchSteps extends BaseSteps {
    private static GooglePage googlePage = new GooglePage();

    public static void performSearchFor(String query) {
        googlePage.searchInput().setValue(query).pressEnter();
    }

    public static void checkNumberOfResults(int expectedNumber) {
        googlePage.results().shouldHaveSize(expectedNumber);
    }
}
