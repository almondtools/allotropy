package net.amygdalum.allotropy;

import java.util.function.Predicate;

import org.junit.platform.commons.support.AnnotationSupport;

public class OnlyAllotropyTests implements Predicate<Class<?>> {

    public static OnlyAllotropyTests onlyAlllotropyTests() {
        return new OnlyAllotropyTests();
    }

    @Override
    public boolean test(Class<?> clazz) {
        while (clazz != null) {
            if (AnnotationSupport.isAnnotated(clazz, RegisterDevices.class)) {
                return true;
            }
            clazz = clazz.getEnclosingClass();
        }
        return false;
    }

}
