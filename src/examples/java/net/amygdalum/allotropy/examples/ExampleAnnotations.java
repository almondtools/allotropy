package net.amygdalum.allotropy.examples;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.amygdalum.allotropy.ExtendWith;
import net.amygdalum.allotropy.Nested;
import net.amygdalum.allotropy.RegisterDevice;
import net.amygdalum.allotropy.View;
import net.amygdalum.allotropy.WithDevice;

public class ExampleAnnotations {

    @Retention(RUNTIME)
    @Target(TYPE)
    @ExtendWith(LocalHttpServer.class)
    @RegisterDevice(id = "desktop", device = ExampleDevices.ChromeDesktop.class)
    @RegisterDevice(id = "tablet", device = ExampleDevices.ChromeTablet.class)
    @RegisterDevice(id = "phone", device = ExampleDevices.ChromePhone.class)
    public @interface ExampleDevicesDemo {
    }

    @Retention(RUNTIME)
    @Target(METHOD)
    @View
    public @interface ExampleView {
    }

    @Retention(RUNTIME)
    @Target(METHOD)
    @WithDevice("tablet")
    @WithDevice("phone")
    public @interface WithTabletAndPhone {
    }

    @Retention(RUNTIME)
    @Target(METHOD)
    @WithDevice("desktop")
    public @interface WithDesktop {
    }

    @Retention(RUNTIME)
    @Target(TYPE)
    @Nested
    public @interface ExtendedNested {
    }

}
