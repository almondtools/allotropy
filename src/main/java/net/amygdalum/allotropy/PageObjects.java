package net.amygdalum.allotropy;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotatedFields;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.DefaultElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;

public class PageObjects {

    private Registrations registrations;
    private ElementLocatorFactory elementLocatorFactory;
    private PageObjectFieldDecorator fieldDecorator;

    public PageObjects(WebDriver driver, Registrations registrations, SelectorMode selectors) {
        this.registrations = registrations;
        this.elementLocatorFactory = new DefaultElementLocatorFactory(driver);
        this.fieldDecorator = new PageObjectFieldDecorator(elementLocatorFactory, selectors);

    }

    public void process(Object object) {
        fieldDecorator.afterResolution(o -> injectFields(o));

        injectFields(object);
    }

    private void injectFields(Object object) {
        PageFactory.initElements(fieldDecorator, object);

        for (var field : findAnnotatedFields(object.getClass(), FromContext.class)) {
            ByName byName = ByName.byName(field.getName());
            ByType byType = ByType.byType(field.getType());
            registrations.get(byName, field.getType()).map(o -> (Object) o)
                .or(() -> registrations.get(byType, field.getType()).map(o -> (Object) o))
                .ifPresent(o -> {
                    try {
                        field.setAccessible(true);
                        field.set(object, o);
                    } catch (ReflectiveOperationException e) {
                        throw new TestLayoutException("field " + field.getName() + " of " + object.getClass().getName() + " is not writeable");
                    }
                });
            ;
        }
    }

}
