package net.amygdalum.allotropy;

import org.junit.platform.commons.annotation.Testable;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Testable
public @interface WithDevices {

    WithDevice[] value();

}
