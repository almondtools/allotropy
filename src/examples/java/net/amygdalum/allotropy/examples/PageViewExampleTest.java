package net.amygdalum.allotropy.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.openqa.selenium.WebDriver;

import net.amygdalum.allotropy.ExtendWith;
import net.amygdalum.allotropy.FromContext;
import net.amygdalum.allotropy.Nested;
import net.amygdalum.allotropy.RegisterDevice;
import net.amygdalum.allotropy.ViewURL;
import net.amygdalum.allotropy.WithDevice;
import net.amygdalum.allotropy.examples.LocalHttpServer.Server;

@ExtendWith(LocalHttpServer.class)
@RegisterDevice(id = "desktop", device = ExampleDevices.ChromeDesktop.class)
@RegisterDevice(id = "tablet", device = ExampleDevices.ChromeTablet.class)
@RegisterDevice(id = "phone", device = ExampleDevices.ChromePhone.class)
public class PageViewExampleTest {

    @FromContext
    private WebDriver driver;
    @FromContext
    private Server server;

    @ViewURL(url = "/myform.html")
    private MyPage.ExternalResolution page;

    @Nested
    class Sub {
        @WithDevice("tablet")
        @WithDevice("phone")
        void aspect1() {
            System.out.println("testing Sub.aspect1");
            assertEquals("Headline", page.getHeadlineText(), "here is an assertion on an injected WebElement");
        }

        @WithDevice("desktop")
        void aspect2() {
            System.out.println("testing Sub.aspect2");
            assertEquals("Submit", page.getForm().getSubmitLabel(), "here is an assertion on an injected PageObject");
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
        assertEquals("Button 1", page.getButton(0).getLabel(), "here is an assertion on an injected PageObject list");
        assertEquals("Button 2", page.getButton(1).getLabel(), "here is an assertion on an injected PageObject list");
    }

    @WithDevice("desktop")
    @WithDevice("phone")
    void aspect4() {
        System.out.println("testing aspect4");
        fail("this test should fail");
    }
}
