package net.amygdalum.allotropy;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
@Repeatable(ExtendWiths.class)
public @interface ExtendWith {

    Class<? extends Extension>[] value();

}
