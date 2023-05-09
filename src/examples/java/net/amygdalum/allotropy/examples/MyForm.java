package net.amygdalum.allotropy.examples;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MyForm implements SearchContext {

    private WebElement element;

    @FindBy(css = "input[type='text']")
    private WebElement input;
    @FindBy(css = "input[type='submit']")
    private WebElement submit;

    public MyForm(WebElement element) {
        this.element = element;
    }

    @Override
    public List<WebElement> findElements(By by) {
        return element.findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        return element.findElement(by);
    }

    public String getSubmitLabel() {
        return submit.getAttribute("value");
    }

    public void input(String text) {
        input.sendKeys(text);
    }

}
