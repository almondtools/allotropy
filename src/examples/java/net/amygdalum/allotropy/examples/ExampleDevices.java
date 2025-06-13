package net.amygdalum.allotropy.examples;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.util.Map;
import net.amygdalum.allotropy.Device;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ExampleDevices {

    public static abstract class SimpleDevice implements Device {
        static {
            if (System.getProperty("webdriver.chrome.driver") == null) {
                WebDriverManager.chromedriver()
                .clearResolutionCache()
                .clearDriverCache()
                .setup();
            }
        }

        private String id;
        private WebDriver driver;

        public SimpleDevice(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public WebDriver open() {
            if (driver == null) {
                driver = createDriver();
            }
            return driver;

        }

        public WebDriver createDriver() {
            System.out.println("creating driver " + id());
            ChromeOptions options = options();
            return new ChromeDriver(options);
        }

        protected ChromeOptions options() {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--user-data-dir=" + System.getProperty("java.io.tmpdir") + "/" + id);
            return options;
        }

        @Override
        public void close() {
            if (driver != null) {
                driver.quit();
                driver = null;
            }
        }

    }

    public static class ChromeDesktop extends SimpleDevice {

        public ChromeDesktop(String id) {
            super(id);
        }

        @Override
        protected ChromeOptions options() {
            ChromeOptions options = super.options();
            Map<String, Object> mobileEmulation = Map.of(
                "deviceMetrics", Map.of("width", 1440, "height", 1080, "touch", false, "mobile", false),
                "clientHints", Map.of("platform", "Windows", "mobile", false));
            options.setExperimentalOption("mobileEmulation", mobileEmulation);
            return options;
        }
    }

    public static class ChromeTablet extends SimpleDevice {

        public ChromeTablet(String id) {
            super(id);
        }

        @Override
        protected ChromeOptions options() {
            ChromeOptions options = super.options();
            Map<String, Object> mobileEmulation = Map.of(
                "deviceMetrics", Map.of("width", 768, "height", 1080, "touch", true, "mobile", true),
                "clientHints", Map.of("platform", "macOS", "mobile", false));
            options.setExperimentalOption("mobileEmulation", mobileEmulation);
            return options;
        }
    }

    public static class ChromePhone extends SimpleDevice {

        public ChromePhone(String id) {
            super(id);
        }

        @Override
        protected ChromeOptions options() {
            ChromeOptions options = super.options();
            Map<String, Object> mobileEmulation = Map.of(
                "deviceMetrics", Map.of("width", 320, "height", 540, "touch", true, "mobile", true),
                "clientHints", Map.of("platform", "Android", "mobile", true));
            options.setExperimentalOption("mobileEmulation", mobileEmulation);
            return options;
        }
    }

}
