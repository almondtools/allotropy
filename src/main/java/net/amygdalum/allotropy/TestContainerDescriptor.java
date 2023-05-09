package net.amygdalum.allotropy;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;

public class TestContainerDescriptor extends AbstractTestDescriptor implements PrototypeTestdescriptor, InterpretableTestDescriptor {

    public static final String CONTAINER = "container";

    public TestContainerDescriptor(UniqueId id, String displayName, TestSource source) {
        super(id, displayName, source);
    }

    public static TestContainerDescriptor fromParent(UniqueId parentId, Class<?> clazz) {
        return new TestContainerDescriptor(parentId.append(CONTAINER, clazz.getName()), resolveDisplayName(clazz), sourceOf(clazz));
    }

    private static String resolveDisplayName(Class<?> clazz) {
        return clazz.getSimpleName();
    }

    private static TestSource sourceOf(Class<?> clazz) {
        return ClassSource.from(clazz);
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    @Override
    public void accept(Interpreter interpreter) {
        interpreter.visitTestContainerDescriptor(this);
    }

    @Override
    public TestDescriptor create(UniqueId id) {
        return new TestContainerDescriptor(id, getDisplayName(), getSource().orElse(null));
    }

    public ClassSource getClassSource() {
        return getSource()
            .filter(ClassSource.class::isInstance)
            .map(ClassSource.class::cast)
            .orElse(null);
    }
}