package net.amygdalum.allotropy.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import net.amygdalum.allotropy.ExtendWith;
import net.amygdalum.allotropy.FromContext;
import net.amygdalum.allotropy.PageObjects;
import net.amygdalum.allotropy.RegisterDevice;
import net.amygdalum.allotropy.RequireExisting;
import net.amygdalum.allotropy.View;
import net.amygdalum.allotropy.WithDevice;
import net.amygdalum.allotropy.examples.LocalHttpServer.Server;

@SystemProperty(name="allotropy.selectors",value="sound")
@ExtendWith(LocalHttpServer.class)
@RegisterDevice(id = "desktop", device = ExampleDevices.ChromeDesktop.class)
@RegisterDevice(id = "phone", device = ExampleDevices.ChromePhone.class)
public class SoundSelectorsTest {

    @FromContext
    private WebDriver driver;
    @FromContext
    private Server server;
    @FromContext
    private PageObjects pageObjects;

    @RequireExisting
    @FindBy(css = "button.button")
    private List<MyButton> strictButtons;
    @FindBy(css = "button.button")
    private List<MyButton> soundButtons;

    @View
    void prepare() {
        driver.navigate().to(server.url("/myform.html"));
    }

    @WithDevice("desktop")
    void failsBecauseSelectorsAreStrictAndRequireAtLeastOneMatch() {
        assertEquals(strictButtons.size(), 0);
        System.out.println("will fail: @RequireExisting switches to strict selector mode: it could be a misleading information that all elements satisfy a condition if there is not any");
        strictButtons.forEach(b -> b.getLabel().equals("Button"));
    }

    @WithDevice("phone")
    void successBecauseSelectorsAreRelaxedWithAllowEmpty() {
        assertEquals(soundButtons.size(), 0);
        System.out.println("will succeed: in sound selector mode iterators on WebElement lists are not restricted");
        soundButtons.forEach(b -> b.getLabel().equals("Button"));
    }
}
