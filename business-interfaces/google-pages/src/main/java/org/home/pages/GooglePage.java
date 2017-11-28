package org.home.pages;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

public class GooglePage extends BasePage {
    public SelenideElement searchInput() {
        return Selenide.$(By.name("q"));
    }

    public ElementsCollection results() {
        return Selenide.$$(".srg > .g");
    }
}
