package net.amygdalum.allotropy;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotatedMethods;
import static org.junit.platform.commons.support.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.support.ReflectionSupport.findFields;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.openqa.selenium.WebDriver;

public record ViewObject(Field viewField, ViewURL viewSpec) {

    public void init(WebDriver driver, PageObjects pageObjects, List<Object> objects) {
        String url = baseURL();
        URLProvider urlProvider = resolveURLProvider(objects);
        driver.navigate().to(urlProvider.url(url));
        for (Object object : objects) {
            if (viewField.getDeclaringClass().isInstance(object)) {
                try {
                    Class<?> pageObjectClazz = viewField.getType();
                    Object pageObject = pageObjectClazz.getDeclaredConstructor().newInstance();
                    pageObjects.process(pageObject);
                    viewField.setAccessible(true);
                    viewField.set(object, pageObject);
                    Method viewMethod = findAnnotatedMethods(pageObject.getClass(), View.class, BOTTOM_UP)
                        .stream()
                        .findFirst()
                        .orElse(null);
                    if (viewMethod != null) {
                        viewMethod.invoke(pageObject);
                    }
                    break;
                } catch (ReflectiveOperationException e) {
                    continue;
                }
            }
        }
    }

    private String baseURL() {
        return viewSpec.url();
    }

    private URLProvider resolveURLProvider(List<Object> objects) {
        for (Object object : objects) {
            Field field = findFields(object.getClass(), f -> viewSpec.provider().isAssignableFrom(f.getType()), BOTTOM_UP)
                .stream()
                .findFirst()
                .orElse(null);
            if (field != null) {
                try {
                    field.setAccessible(true);
                    URLProvider urlProvider = (URLProvider) field.get(object);
                    return urlProvider;
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
        return s -> s;
    }

}
