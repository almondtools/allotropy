package net.amygdalum.allotropy.examples;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import net.amygdalum.allotropy.ExtendWith;
import net.amygdalum.allotropy.FromContext;
import net.amygdalum.allotropy.RegisterDevice;
import net.amygdalum.allotropy.View;
import net.amygdalum.allotropy.WithDevice;
import net.amygdalum.allotropy.examples.LocalHttpServer.Server;
import net.amygdalum.allotropy.fluent.Expectations;

@ExtendWith(LocalHttpServer.class)
@RegisterDevice(id = "desktop", device = ExampleDevices.ChromeDesktop.class)
@RegisterDevice(id = "mobile", device = ExampleDevices.ChromePhone.class)
public class FluentExampleTest {

    @FindBy(css = ".text")
    private WebElement text;
    @FindBy(css = ".image")
    private WebElement image;

    @FromContext
    private WebDriver driver;
    @FromContext
    private Server server;

    @View
    void prepare() {
        driver.navigate().to(server.url("/textImage.html"));
    }

    @WithDevice("mobile")
    void aboveEachOther() {
        Expectations.expect(image).below().of(text);
    }

    @WithDevice("desktop")
    void nextToEachOther() {
        Expectations.expect(image).right().of(text);
    }
}
