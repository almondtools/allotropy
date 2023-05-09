package net.amygdalum.allotropy;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.platform.commons.annotation.Testable;

@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Repeatable(WithDevices.class)
@Testable
public @interface WithDevice {
    String value();
}
