package net.amygdalum.allotropy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.platform.engine.TestDescriptor;

import net.amygdalum.allotropy.extensions.BeforeTestCallback;
import net.amygdalum.allotropy.extensions.BeforeViewCallback;

public class Extensions implements AutoCloseable {

    private Map<Class<? extends Extension>, Installed<? extends Extension>> extensions;

    public Extensions() {
        this.extensions = new LinkedHashMap<>();
    }

    public <T extends Extension> void register(Class<T> extensionClass, TestDescriptor descriptor) {
        for (var ext : List.of(BeforeViewCallback.class, BeforeTestCallback.class)) {
            if (ext.isAssignableFrom(extensionClass)) {
                extensions.compute(extensionClass, (c, s) -> {
                    if (s == null) {
                        try {
                            T extension = extensionClass.getDeclaredConstructor().newInstance();
                            s = Installed.install(extension);
                        } catch (ReflectiveOperationException e) {
                            throw new UnexpectedException(e);
                        }
                    }
                    s.addDescriptor(descriptor);
                    return s;
                });
            }
        }
    }

    public <T extends Extension> List<T> findAll(Class<T> extensionClass, TestDescriptor descriptor) {
        return extensions.entrySet().stream()
            .filter(e -> extensionClass.isAssignableFrom(e.getKey()) && isReachable(descriptor, e.getValue().descriptors()))
            .map(e -> extensionClass.cast(e.getValue().extension()))
            .toList();
    }

    private boolean isReachable(TestDescriptor descriptor, List<TestDescriptor> descriptors) {
        while (descriptor != null && !descriptors.contains(descriptor)) {
            descriptor = descriptor.getParent().orElse(null);
        }
        return descriptor != null;
    }

    @Override
    public void close() {
        extensions.values().stream().forEach(Installed::uninstall);
        extensions.clear();
    }

    private record Installed<T extends Extension>(T extension, List<TestDescriptor> descriptors) {

        public Installed<T> addDescriptor(TestDescriptor descriptor) {
            if (!descriptors.contains(descriptor)) {
                descriptors.add(descriptor);
            }
            return this;
        }

        public static <T extends Extension> Installed<T> install(T extension) {
            return new Installed<>(extension, new ArrayList<>());
        }

        public void uninstall() {
            if (extension instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

}
