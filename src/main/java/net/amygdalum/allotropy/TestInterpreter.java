package net.amygdalum.allotropy;

import static net.amygdalum.allotropy.Scope.DEVICE;
import static net.amygdalum.allotropy.Scope.GLOBAL;
import static net.amygdalum.allotropy.Scope.VIEW;
import static org.junit.platform.commons.support.AnnotationSupport.findRepeatableAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.openqa.selenium.WebDriver;
import org.opentest4j.IncompleteExecutionException;

import net.amygdalum.allotropy.extensions.BeforeViewCallback;

public class TestInterpreter implements Interpreter, AutoCloseable {
    static {
        try (InputStream is = TestInterpreter.class.getClassLoader().getResourceAsStream("logging.properties")) {
            if (is != null) {
                LogManager.getLogManager().readConfiguration(is);
            }
        } catch (IOException e) {
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.severe("cannot loag logging.properties: " + e.getMessage());
        }
    }

    private EngineExecutionListener engineExecutionListener;
    private Extensions extensions;
    private Registrations registrations;
    private PageObjects pageObjects;
    private Deque<TestContext<?>> contexts;

    private AllotropyConfiguration config;
    private WebDriver driver;

    public TestInterpreter(EngineExecutionListener engineExecutionListener) {
        this.engineExecutionListener = engineExecutionListener;
        this.extensions = new Extensions();
        this.registrations = new Registrations();
        this.contexts = new LinkedList<>();
    }

    protected <T extends InterpretableTestDescriptor> TestContext<T> pushContext(T descriptor) {
        System.out.println(descriptor.getClass().getName() + ":" + descriptor.getSource());
        TestContext<T> context = new TestContext<>(descriptor);
        contexts.push(context);
        return context;
    }

    protected void dropContext(InterpretableTestDescriptor descriptor) {
        while (contexts.peek().descriptor() != descriptor) {
            contexts.remove();
        }
        contexts.remove();
    }

    protected Optional<Object> contextObjectFor(Class<?> clazz) {
        return contexts.stream()
            .map(c -> c.instance())
            .filter(clazz::isInstance)
            .findFirst();
    }

    protected List<Object> contextObjects() {
        return contexts.stream()
            .map(c -> c.instance())
            .filter(Objects::nonNull)
            .toList();
    }

    protected <T extends TestDescriptor> Optional<TestContext<T>> contextForDescriptor(Class<T> clazz) {
        return contexts.stream()
            .filter(c -> clazz.isInstance(c.descriptor()))
            .findFirst()
            .map(c -> c.cast(clazz));
    }

    @Override
    public void close() {
        extensions.close();
        registrations.close();
    }

    @Override
    public void enterEngineDescriptor(AllotropyEngineDescriptor descriptor) throws Exception {
        this.config = descriptor.getConfiguration();
        pushContext(descriptor).assign(new RootContext());
        engineExecutionListener.executionStarted(descriptor);
    }

    @Override
    public void leaveEngineDescriptor(AllotropyEngineDescriptor descriptor, Optional<Throwable> e) {
        engineExecutionListener.executionFinished(descriptor, resultFrom(e));
        dropContext(descriptor);
    }

    @Override
    public void enterViewContainerDescriptor(ViewContainerDescriptor descriptor) throws Exception {
        pushContext(descriptor).assign(descriptor.getClassSource().getJavaClass());
        engineExecutionListener.executionStarted(descriptor);
        registrations.clear(VIEW);
        registerExtensions(descriptor.getClassSource().getJavaClass(), descriptor);
        for (var extension : extensions.findAll(BeforeViewCallback.class, descriptor)) {
            extension.beforeView(descriptor.getClassSource().getJavaClass(), registrations);
        }
    }

    @Override
    public void leaveViewContainerDescriptor(ViewContainerDescriptor descriptor, Optional<Throwable> e) {
        engineExecutionListener.executionFinished(descriptor, resultFrom(e));
        dropContext(descriptor);
    }

    @Override
    public void enterDeviceDescriptor(DeviceDescriptor descriptor) throws Exception {
        TestContext<DeviceDescriptor> context = pushContext(descriptor);
        registrations.clear(DEVICE);

        Device device = registrations.forScope(GLOBAL).resolve(descriptor.getDeviceClass(), descriptor::newDevice);

        driver = registrations.forScope(DEVICE).resolve(WebDriver.class, d -> device.open());
        pageObjects = registrations.forScope(DEVICE).resolve(PageObjects.class, d -> new PageObjects(driver, registrations, config.selectors()));

        populateContext(driver);
        engineExecutionListener.executionStarted(descriptor);
        context.assign(device);
        callViewMethod(descriptor);
    }

    @Override
    public void leaveDeviceDescriptor(DeviceDescriptor descriptor, Optional<Throwable> e) {
        driver = null;
        engineExecutionListener.executionFinished(descriptor, resultFrom(e));
        dropContext(descriptor);
    }

    @Override
    public void enterTestContainerDescriptor(TestContainerDescriptor descriptor) throws Exception {
        pushContext(descriptor).assign(descriptor.getClassSource().getJavaClass());
        engineExecutionListener.executionStarted(descriptor);
        registerExtensions(descriptor.getClassSource().getJavaClass(), descriptor);
    }

    @Override
    public void leaveTestContainerDescriptor(TestContainerDescriptor descriptor, Optional<Throwable> e) {
        engineExecutionListener.executionFinished(descriptor, resultFrom(e));
        dropContext(descriptor);
    }

    @Override
    public void enterExecutableTestDescriptor(ExecutableTestDescriptor descriptor) throws Exception {
        pushContext(descriptor);
        registrations.clear(Scope.TEST);
        registerExtensions(descriptor.getMethodSource().getJavaMethod(), descriptor);

        populateContext(driver);
        engineExecutionListener.executionStarted(descriptor);
        callTestMethod(descriptor);
    }

    @Override
    public void leaveExecutableTestDescriptor(ExecutableTestDescriptor descriptor, Optional<Throwable> e) {
        TestExecutionResult result = resultFrom(e);
        engineExecutionListener.executionFinished(descriptor, result);
        dropContext(descriptor);
    }

    private void registerExtensions(AnnotatedElement element, TestDescriptor descriptor) {
        for (var extension : findRepeatableAnnotations(element, ExtendWith.class)
            .stream()
            .flatMap(e -> Arrays.stream(e.value()))
            .toList()) {
            extensions.register(extension, descriptor);
        }

    }

    private void populateContext(WebDriver driver) {
        for (var contextObject : contextObjects()) {
            pageObjects.process(contextObject);
        }
    }

    private void callViewMethod(DeviceDescriptor descriptor) throws Exception {
        TestContext<ViewContainerDescriptor> view = contextForDescriptor(ViewContainerDescriptor.class).orElseThrow(() -> new TestLayoutException("cannot resolve view container"));
        try {
            try {
                Method viewMethod = view.descriptor().getViewMethod();
                viewMethod.setAccessible(true);
                viewMethod.invoke(view.instance());
            } catch (NoSuchElementException e) {
                ViewObject viewObject = view.descriptor().getViewObject();
                viewObject.init(driver, pageObjects, contextObjects());
            }
        } catch (InvocationTargetException e) {
            throwCauseOf(e);
        } catch (ReflectiveOperationException e) {
            throw new UnexpectedException(e);
        }
    }

    private void callTestMethod(ExecutableTestDescriptor descriptor) throws Exception {
        try {
            Method testMethod = descriptor.getMethodSource().getJavaMethod();
            Object object = contextObjectFor(descriptor.getMethodSource().getJavaClass()).orElseThrow(() -> new TestLayoutException("cannot resolve test container"));
            testMethod.setAccessible(true);
            testMethod.invoke(object);
        } catch (InvocationTargetException e) {
            throwCauseOf(e);
        } catch (ReflectiveOperationException e) {
            throw new UnexpectedException(e);
        }
    }

    private void throwCauseOf(InvocationTargetException e) throws Exception {
        Throwable cause = e.getCause();
        if (cause instanceof Error err) {
            throw err;
        } else if (cause instanceof Exception ex) {
            throw ex;
        }
    }

    private TestExecutionResult resultFrom(Optional<Throwable> e) {
        Throwable exception = e.orElse(null);
        if (exception instanceof AssertionError ae) {
            return TestExecutionResult.failed(ae);
        } else if (exception instanceof IncompleteExecutionException ie) {
            return TestExecutionResult.aborted(ie);
        } else if (exception == null) {
            return TestExecutionResult.successful();
        } else {
            return TestExecutionResult.failed(exception);
        }
    }

    public class TestContext<T extends TestDescriptor> {

        private T descriptor;
        private Object instance;

        public TestContext(T descriptor) {
            this.descriptor = descriptor;
        }

        public T descriptor() {
            return descriptor;
        }

        public Object instance() {
            return instance;
        }

        public void assign(Object instance) {
            this.instance = instance;
        }

        public void assign(Class<?> clazz) {
            try {
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                assign(constructor.newInstance());
            } catch (ReflectiveOperationException e1) {
                Constructor<?> constructor = Arrays.stream(clazz.getDeclaredConstructors())
                    .filter(c -> c.getParameterCount() == 1)
                    .findFirst()
                    .orElseThrow(() -> new TestLayoutException("cannot find appropriate constructor to establish test hierarchy for class " + clazz.getName()));

                Class<?> parameterClazz = constructor.getParameterTypes()[0];

                Object parent = contextObjectFor(parameterClazz).orElseThrow(() -> new TestLayoutException("cannot find appropriate constructor to establish test hierarchy for class " + clazz.getName()));
                try {
                    constructor.setAccessible(true);
                    assign(constructor.newInstance(parent));
                } catch (ReflectiveOperationException e2) {
                    TestLayoutException e = new TestLayoutException("cannot find appropriate constructor to establish test hierarchy for class " + clazz.getName());
                    e.addSuppressed(e1);
                    e.addSuppressed(e2);
                    throw e;
                }

            }
        }

        @SuppressWarnings("unchecked")
        public <S extends TestDescriptor> TestContext<S> cast(Class<S> clazz) {
            return (TestContext<S>) this;
        }
    }

    public static class RootContext {

    }
}
