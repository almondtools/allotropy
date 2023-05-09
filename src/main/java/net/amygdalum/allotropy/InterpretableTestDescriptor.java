package net.amygdalum.allotropy;

import org.junit.platform.engine.TestDescriptor;

public interface InterpretableTestDescriptor extends TestDescriptor {

    void accept(Interpreter interpreter);

    default void visitChildren(Interpreter interpreter) {
        this.getChildren()
            .stream()
            .distinct()
            .filter(InterpretableTestDescriptor.class::isInstance)
            .map(InterpretableTestDescriptor.class::cast)
            .forEach(child -> child.accept(interpreter));
    }
}
