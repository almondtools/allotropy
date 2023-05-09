package net.amygdalum.allotropy.extensions;

import java.lang.reflect.Method;

import net.amygdalum.allotropy.Extension;
import net.amygdalum.allotropy.Registrations;

public interface BeforeTestCallback extends Extension {

    void beforeView(Class<?> testClass, Method testMethod, Registrations registrations);

}
