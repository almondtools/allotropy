package net.amygdalum.allotropy.examples;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.amygdalum.allotropy.ExtendWith;

@Retention(RUNTIME)
@Target({TYPE, ANNOTATION_TYPE})
@ExtendWith(SystemProperties.class)
public @interface SystemProperty {
    String name();

    String value();
}
