package net.amygdalum.allotropy.examples;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import net.amygdalum.allotropy.View;

public class MyPage {

    public static class ExternalResolution {
        @FindBy(css = ".headline")
        private WebElement headline;
        @FindBy(css = ".myform")
        private MyForm form;
        @FindBy(css = "button")
        private List<MyButton> buttons;

        @View
        public void init() {
            form.input("some input from page object view");
        }

        public String getHeadlineText() {
            return headline.getText();
        }

        public MyForm getForm() {
            return form;
        }

        public MyButton getButton(int i) {
            return buttons.get(i);
        }

    }

    public static class RelativeResolution implements SearchContext {
        @FindBy(css = ".headline")
        private WebElement headline;
        @FindBy(css = ".myform")
        private MyForm form;
        @FindBy(css = "button")
        private List<MyButton> buttons;

        private WebElement root;

        public RelativeResolution(WebElement root) {
            this.root = root;
        }

        @Override
        public List<WebElement> findElements(By by) {
            return root.findElements(by);
        }

        @Override
        public WebElement findElement(By by) {
            return root.findElement(by);
        }
    }

}
