package org.iceberg.steps;

import com.codeborne.selenide.Selenide;

public class NavigationSteps extends BaseSteps {

    public static void openWebApp() {
        Selenide.open(System.getProperty("base.url"));
    }
}
