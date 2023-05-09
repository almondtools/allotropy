package net.amygdalum.allotropy;

import org.openqa.selenium.WebDriver;

public interface Device extends AutoCloseable {
    String id();

    WebDriver open();

}
