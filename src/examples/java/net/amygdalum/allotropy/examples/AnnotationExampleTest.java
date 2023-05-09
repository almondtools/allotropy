package net.amygdalum.allotropy.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import net.amygdalum.allotropy.FromContext;
import net.amygdalum.allotropy.Nested;
import net.amygdalum.allotropy.WithDevice;
import net.amygdalum.allotropy.examples.ExampleAnnotations.ExampleDevicesDemo;
import net.amygdalum.allotropy.examples.ExampleAnnotations.ExampleView;
import net.amygdalum.allotropy.examples.ExampleAnnotations.ExtendedNested;
import net.amygdalum.allotropy.examples.ExampleAnnotations.WithDesktop;
import net.amygdalum.allotropy.examples.ExampleAnnotations.WithTabletAndPhone;
import net.amygdalum.allotropy.examples.LocalHttpServer.Server;

@ExampleDevicesDemo
public class AnnotationExampleTest {

    @FindBy(css = ".headline")
    private WebElement headline;
    @FindBy(css = ".myform")
    private MyForm form;
    @FindBy(css = "button")
    private List<MyButton> buttons;

    @FromContext
    private WebDriver driver;
    @FromContext
    private Server server;

    @ExampleView
    void prepare() {
        driver.navigate().to(server.url("/myform.html"));
    }

    @ExtendedNested
    class Sub {
        @WithTabletAndPhone
        void aspect1() {
            System.out.println("testing Sub.aspect1");
            assertEquals("Headline", headline.getText(), "here is an assertion on an injected WebElement");
        }

        @WithDesktop
        void aspect2() {
            System.out.println("testing Sub.aspect2");
            assertEquals("Submit", form.getSubmitLabel(), "here is an assertion on an injected PageObject");
        }

        @Nested
        class SubSub {
            @WithDevice("desktop")
            void aspect5() {
                System.out.println("testing Sub.SubSub.aspect5");
                fail("this test should fail");
            }
        }

    }

    @WithDevice("tablet")
    void aspect3() {
        System.out.println("testing aspect3");
        assertEquals("Button 1", buttons.get(0).getLabel(), "here is an assertion on an injected PageObject list");
        assertEquals("Button 1", buttons.get(1).getLabel(), "here is an assertion on an injected PageObject list");
    }

    @WithDevice("desktop")
    @WithDevice("phone")
    void aspect4() {
        System.out.println("testing aspect4");
        fail("this test should fail");
    }
}
