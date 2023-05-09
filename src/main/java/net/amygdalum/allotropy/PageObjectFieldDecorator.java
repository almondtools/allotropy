package net.amygdalum.allotropy;

import static net.amygdalum.allotropy.SelectorMode.SOUND;
import static net.amygdalum.allotropy.SelectorMode.STRICT;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.FieldDecorator;
import org.openqa.selenium.support.pagefactory.internal.LocatingElementHandler;

public class PageObjectFieldDecorator implements FieldDecorator {

    private SelectorMode selectors;
    private ElementLocatorFactory factory;
    private Consumer<Object> afterResolution;

    public PageObjectFieldDecorator(ElementLocatorFactory factory, SelectorMode selectors) {
        this.selectors = selectors;
        this.factory = factory;
        this.afterResolution = o -> {
        };
    }

    @Override
    public Object decorate(ClassLoader loader, Field field) {
        if (field.getAnnotation(FindBy.class) == null
            && field.getAnnotation(FindBys.class) == null
            && field.getAnnotation(FindAll.class) == null) {
            return null;
        }
        Class<?> type = field.getType();
        if (!WebElement.class.isAssignableFrom(type)
            && !SearchContext.class.isAssignableFrom(type)
            && !isDecoratableList(field)) {
            return null;
        }

        ElementLocator locator = factory.createLocator(field);
        if (locator == null) {
            return null;
        }

        if (WebElement.class.isAssignableFrom(type)) {
            return proxyForLocator(loader, locator);
        } else if (SearchContext.class.isAssignableFrom(type)) {
            return proxyForLocator(type, loader, locator);
        } else if (List.class.isAssignableFrom(type)) {
            SelectorMode selectors = (this.selectors == STRICT)
                ? field.getAnnotation(AllowEmpty.class) == null ? STRICT : SOUND
                : field.getAnnotation(RequireExisting.class) == null ? SOUND : STRICT;
            if (field.getGenericType() instanceof ParameterizedType parameterizedType) {
                if (parameterizedType.getActualTypeArguments()[0] instanceof Class<?> clazz) {
                    return proxyForListLocator(clazz, loader, locator, selectors);
                } else {
                    return proxyForListLocator(WebElement.class, loader, locator, selectors);
                }
            } else {
                return proxyForListLocator(WebElement.class, loader, locator, selectors);
            }
        } else {
            return null;
        }
    }

    public void afterResolution(Consumer<Object> afterResolution) {
        this.afterResolution = afterResolution;
    }

    private Object wrap(Class<?> type, WebElement webElement) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor(WebElement.class);
            constructor.setAccessible(true);
            Object object = constructor.newInstance(webElement);
            afterResolution.accept(object);
            return object;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    protected boolean isDecoratableList(Field field) {
        if (!List.class.isAssignableFrom(field.getType())) {
            return false;
        }

        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType parameterizedType)) {
            return false;
        }

        Type listType = parameterizedType.getActualTypeArguments()[0];

        if (!(listType instanceof Class<?> listClass)) {
            return false;
        }
        if (WebElement.class.isAssignableFrom(listClass)) {
            return true;
        } else if (SearchContext.class.isAssignableFrom(listClass)) {
            try {
                listClass.getDeclaredConstructor(WebElement.class); //just check existence of proper constructor
                return true;
            } catch (ReflectiveOperationException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    protected WebElement proxyForLocator(ClassLoader loader, ElementLocator locator) {
        InvocationHandler handler = new LocatingElementHandler(locator);
        return (WebElement) Proxy.newProxyInstance(loader, new Class[] {WebElement.class, WrapsElement.class, Locatable.class}, handler);
    }

    protected Object proxyForLocator(Class<?> type, ClassLoader loader, ElementLocator locator) {
        InvocationHandler handler = new LocatingElementHandler(locator);
        return wrap(type, (WebElement) Proxy.newProxyInstance(loader, new Class[] {WebElement.class, WrapsElement.class, Locatable.class}, handler));
    }

    @SuppressWarnings("unchecked")
    protected List<WebElement> proxyForListLocator(ClassLoader loader, ElementLocator locator, SelectorMode selectors) {
        InvocationHandler handler = new PageObjectLocatingElementListHandler(locator, selectors);
        return (List<WebElement>) Proxy.newProxyInstance(loader, new Class[] {List.class}, handler);
    }

    @SuppressWarnings("unchecked")
    protected List<Object> proxyForListLocator(Class<?> type, ClassLoader loader, ElementLocator locator, SelectorMode selectors) {
        InvocationHandler handler = new PageObjectListHandler(type, new PageObjectLocatingElementListHandler(locator, selectors));
        return (List<Object>) Proxy.newProxyInstance(loader, new Class[] {List.class}, handler);
    }

    private static class PageObjectLocatingElementListHandler implements InvocationHandler {

        private List<String> ITERABLE_METHODS = List.of("iterator", "listIterator", "spliterator", "toArray", "forEach");

        private ElementLocator locator;
        private SelectorMode selectors;

        public PageObjectLocatingElementListHandler(ElementLocator locator, SelectorMode selectors) {
            this.locator = locator;
            this.selectors = selectors;
        }

        @Override
        public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
            List<WebElement> elements = locator.findElements();
            if (selectors == STRICT && elements.isEmpty() && ITERABLE_METHODS.contains(method.getName())) {
                throw new AssertionError("In strict selector mode List<WebElement>." + method.getName() + " is required to return at least one element.");
            }
            try {
                return method.invoke(elements, objects);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }

    private class PageObjectListHandler implements InvocationHandler {

        private Class<?> type;
        private InvocationHandler handler;

        public PageObjectListHandler(Class<?> type, InvocationHandler handler) {
            this.type = type;
            this.handler = handler;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = handler.invoke(proxy, method, args);
            if (result instanceof WebElement webElement && type != WebElement.class) {
                return wrap(type, webElement);
            } else {
                return result;
            }
        }

    }

}
