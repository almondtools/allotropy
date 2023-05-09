package net.amygdalum.allotropy.examples;

import static net.amygdalum.allotropy.Scope.VIEW;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

import java.util.HashSet;
import java.util.Set;

import net.amygdalum.allotropy.Registrations;
import net.amygdalum.allotropy.extensions.BeforeViewCallback;

public class SystemProperties implements BeforeViewCallback, AutoCloseable {

    private Set<String> properties;
    
    public SystemProperties() {
        this.properties = new HashSet<>();
    }
    
    @Override
    public void beforeView(Class<?> viewClass, Registrations registrations) {
        findAnnotation(viewClass, SystemProperty.class).ifPresent(s ->  {
            setProperty(s.name(), s.value());
        });
        registrations.forScope(VIEW).resolve(SystemProperties.class, c -> this);
    }

    private void setProperty(String name, String value) {
        properties.add(name);
        System.setProperty(name,value);
    }

    @Override
    public void close() throws Exception {
        for (var name : properties) {
            System.setProperty(name, "");
        }
        properties.clear();
    }
}
