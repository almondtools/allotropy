package net.amygdalum.allotropy;

import java.lang.reflect.Method;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;

public class ExecutableTestDescriptor extends AbstractTestDescriptor implements PrototypeTestdescriptor, InterpretableTestDescriptor {

    public static final String METHOD = "method";

    public ExecutableTestDescriptor(UniqueId uniqueId, String displayName, MethodSource method) {
        super(uniqueId, displayName, method);
    }

    public static ExecutableTestDescriptor fromParent(UniqueId parentId, Method method) {
        return new ExecutableTestDescriptor(parentId.append(METHOD, method.getName()), method.getName(), MethodSource.from(method));
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public void accept(Interpreter interpreter) {
        interpreter.visitExecutableTestDescriptor(this);
    }

    @Override
    public TestDescriptor create(UniqueId id) {
        MethodSource src = getSource()
            .filter(MethodSource.class::isInstance)
            .map(MethodSource.class::cast)
            .orElse(null);
        return new ExecutableTestDescriptor(id, getDisplayName(), src);
    }

    public boolean supportsDevice(String id) {
        return getSource()
            .filter(MethodSource.class::isInstance)
            .map(MethodSource.class::cast)
            .stream()
            .flatMap(m -> AnnotationSupport.findRepeatableAnnotations(m.getJavaMethod(), WithDevice.class).stream())
            .map(w -> w.value())
            .anyMatch(v -> id.equals(v));
    }

    public MethodSource getMethodSource() {
        return getSource()
            .filter(MethodSource.class::isInstance)
            .map(MethodSource.class::cast)
            .orElse(null);
    }

}