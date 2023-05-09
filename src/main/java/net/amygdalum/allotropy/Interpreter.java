package net.amygdalum.allotropy;

import java.util.Optional;

public interface Interpreter {

    default void visitEngineDescriptor(AllotropyEngineDescriptor descriptor) {
        try {
            enterEngineDescriptor(descriptor);
            descriptor.visitChildren(this);
            leaveEngineDescriptor(descriptor, Optional.empty());
        } catch (Throwable e) {
            leaveEngineDescriptor(descriptor, Optional.of(e));
        }
    }

    void enterEngineDescriptor(AllotropyEngineDescriptor descriptor) throws Exception;

    void leaveEngineDescriptor(AllotropyEngineDescriptor descriptor, Optional<Throwable> e);

    default void visitDeviceDescriptor(DeviceDescriptor descriptor) {
        try {
            enterDeviceDescriptor(descriptor);
            descriptor.visitChildren(this);
            leaveDeviceDescriptor(descriptor, Optional.empty());
        } catch (Throwable e) {
            leaveDeviceDescriptor(descriptor, Optional.of(e));
        }
    }

    void enterDeviceDescriptor(DeviceDescriptor descriptor) throws Exception;

    void leaveDeviceDescriptor(DeviceDescriptor descriptor, Optional<Throwable> e);

    default void visitViewContainerDescriptor(ViewContainerDescriptor descriptor) {
        try {
            enterViewContainerDescriptor(descriptor);
            descriptor.visitChildren(this);
            leaveViewContainerDescriptor(descriptor, Optional.empty());
        } catch (Throwable e) {
            leaveViewContainerDescriptor(descriptor, Optional.of(e));
        }

    }

    void enterViewContainerDescriptor(ViewContainerDescriptor descriptor) throws Exception;

    void leaveViewContainerDescriptor(ViewContainerDescriptor descriptor, Optional<Throwable> e);

    default void visitTestContainerDescriptor(TestContainerDescriptor descriptor) {
        try {
            enterTestContainerDescriptor(descriptor);
            descriptor.visitChildren(this);
            leaveTestContainerDescriptor(descriptor, Optional.empty());
        } catch (Throwable e) {
            leaveTestContainerDescriptor(descriptor, Optional.of(e));
        }
    }

    void enterTestContainerDescriptor(TestContainerDescriptor descriptor) throws Exception;

    void leaveTestContainerDescriptor(TestContainerDescriptor descriptor, Optional<Throwable> e);

    default void visitExecutableTestDescriptor(ExecutableTestDescriptor descriptor) {
        try {
            enterExecutableTestDescriptor(descriptor);
            descriptor.visitChildren(this);
            leaveExecutableTestDescriptor(descriptor, Optional.empty());
        } catch (Throwable e) {
            leaveExecutableTestDescriptor(descriptor, Optional.of(e));
        }
    }

    void enterExecutableTestDescriptor(ExecutableTestDescriptor descriptor) throws Exception;

    void leaveExecutableTestDescriptor(ExecutableTestDescriptor descriptor, Optional<Throwable> e);

}
