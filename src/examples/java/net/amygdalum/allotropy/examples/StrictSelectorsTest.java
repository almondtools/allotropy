package net.amygdalum.allotropy.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import net.amygdalum.allotropy.AllowEmpty;
import net.amygdalum.allotropy.ExtendWith;
import net.amygdalum.allotropy.FromContext;
import net.amygdalum.allotropy.PageObjects;
import net.amygdalum.allotropy.RegisterDevice;
import net.amygdalum.allotropy.View;
import net.amygdalum.allotropy.WithDevice;
import net.amygdalum.allotropy.examples.LocalHttpServer.Server;

@ExtendWith(LocalHttpServer.class)
@RegisterDevice(id = "desktop", device = ExampleDevices.ChromeDesktop.class)
@RegisterDevice(id = "phone", device = ExampleDevices.ChromePhone.class)
public class StrictSelectorsTest {

    @FromContext
    private WebDriver driver;
    @FromContext
    private Server server;
    @FromContext
    private PageObjects pageObjects;

    @FindBy(css = "button.button")
    private List<MyButton> strictButtons;
    @AllowEmpty
    @FindBy(css = "button.button")
    private List<MyButton> soundButtons;

    @View
    void prepare() {
        driver.navigate().to(server.url("/myform.html"));
    }

    @WithDevice("desktop")
    void failsBecauseSelectorsAreStrictAndRequireAtLeastOneMatch() {
        assertEquals(strictButtons.size(), 0);
        System.out.println("will fail: in strict selector mode iterators on WebElement lists are required to do at least one check");
        strictButtons.forEach(b -> b.getLabel().equals("Button"));
    }

    @WithDevice("phone")
    void successBecauseSelectorsAreRelaxedWithAllowEmpty() {
        assertEquals(soundButtons.size(), 0);
        System.out.println("will succeed: @AllowEmpty switches to sound selector mode and in fact: each existing button has the label \"Button\", there just does not exist any");
        soundButtons.forEach(b -> b.getLabel().equals("Button"));
    }
}
