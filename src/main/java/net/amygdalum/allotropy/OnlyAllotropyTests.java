package net.amygdalum.allotropy;

import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

import java.util.function.Predicate;

public class OnlyAllotropyTests implements Predicate<Class<?>> {

    public static OnlyAllotropyTests onlyAlllotropyTests() {
        return new OnlyAllotropyTests();
    }

    @Override
    public boolean test(Class<?> clazz) {
        while (clazz != null) {
            if (isAnnotated(clazz, RegisterDevices.class)) {
                return true;
            }
            clazz = clazz.getEnclosingClass();
        }
        return false;
    }

}
