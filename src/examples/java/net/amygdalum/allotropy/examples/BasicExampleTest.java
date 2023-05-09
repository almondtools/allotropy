package net.amygdalum.allotropy.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import net.amygdalum.allotropy.DisabledWithDevice;
import net.amygdalum.allotropy.ExtendWith;
import net.amygdalum.allotropy.FromContext;
import net.amygdalum.allotropy.Nested;
import net.amygdalum.allotropy.RegisterDevice;
import net.amygdalum.allotropy.View;
import net.amygdalum.allotropy.WithDevice;
import net.amygdalum.allotropy.examples.LocalHttpServer.Server;

@ExtendWith(LocalHttpServer.class)
@RegisterDevice(id = "desktop", device = ExampleDevices.ChromeDesktop.class)
@RegisterDevice(id = "tablet", device = ExampleDevices.ChromeTablet.class)
@RegisterDevice(id = "phone", device = ExampleDevices.ChromePhone.class)
public class BasicExampleTest {

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

    @View
    void prepare() {
        driver.navigate().to(server.url("/myform.html"));
    }

    @Nested
    class Sub {
        @WithDevice("tablet")
        @WithDevice("phone")
        void aspect1() {
            System.out.println("testing Sub.aspect1");
            assertEquals("Headline", headline.getText(), "here is an assertion on an injected WebElement");
        }

        @WithDevice("desktop")
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
        assertEquals("Button 2", buttons.get(1).getLabel(), "here is an assertion on an injected PageObject list");
    }

    @DisabledWithDevice("desktop")
    @WithDevice("phone")
    void aspect4() {
        System.out.println("testing aspect4");
        fail("this test should fail");
    }
}
