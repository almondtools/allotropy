package net.amygdalum.allotropy.examples;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class MyButton implements SearchContext {

    private WebElement element;

    public MyButton(WebElement element) {
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

    public String getLabel() {
        return element.getText();
    }

}
