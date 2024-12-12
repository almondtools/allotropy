package net.amygdalum.allotropy;

import static net.amygdalum.allotropy.ByType.byType;
import static net.amygdalum.allotropy.Scope.DEVICE;
import static net.amygdalum.allotropy.Scope.GLOBAL;
import static net.amygdalum.allotropy.Scope.TEST;
import static net.amygdalum.allotropy.Scope.VIEW;
import static net.amygdalum.allotropy.util.Optionals.any;

import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

public class Registrations implements AutoCloseable {

    private SortedMap<InjectionKey, ScopedValue<?>> resources;

    public Registrations() {
        this.resources = new TreeMap<>();
    }

    @Override
    public void close() {
        for (Scope scope : List.of(TEST, DEVICE, VIEW, GLOBAL)) {
            clear(scope);
        }
    }

    public <T> Optional<T> get(InjectionKey key, Class<T> clazz) {
        return any(resources.get(key))
            .map(r -> r.value())
            .filter(clazz::isInstance)
            .map(clazz::cast);
    }

    protected <T> T resolve(Scope scope, InjectionKey key, Class<T> type, Function<Class<T>, T> creator) {
        ScopedValue<?> scopedValue = resources.computeIfAbsent(key, k -> new ScopedValue<>(scope, creator.apply(type)));
        return type.cast(scopedValue.value());
    }

    protected <T> T register(Scope scope, InjectionKey key, T value) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) value.getClass();
        ScopedValue<?> scopedValue = resources.computeIfAbsent(key, k -> new ScopedValue<>(scope, value));
        return type.cast(scopedValue.value());
    }

    public void clear(Scope scope) {
        resources.entrySet().removeIf(e -> e.getValue().sendClose(scope));
    }

    private static record ScopedValue<T>(Scope scope, T value) {

        public boolean sendClose(Scope scope) {
            if (this.scope == scope) {
                if (value instanceof AutoCloseable closeable) {
                    try {
                        closeable.close();
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
                return true;
            } else {
                return false;
            }
        }

    }

    public ScopedRegistrations forScope(Scope scope) {
        return new ScopedRegistrations(scope);
    }

    public class ScopedRegistrations {
        private Scope scope;

        public ScopedRegistrations(Scope scope) {
            this.scope = scope;
        }

        public <T> T resolve(Class<T> type, Function<Class<T>, T> creator) {
            return Registrations.this.resolve(scope, byType(type), type, creator);
        }

        public <T> T register(Class<T> type, T value) {
            return Registrations.this.register(scope, byType(type), value);
        }

        public <T> T resolve(InjectionKey key, Class<T> type, Function<Class<T>, T> creator) {
            return Registrations.this.resolve(scope, key, type, creator);
        }

        public <T> T register(InjectionKey key, T value) {
            return Registrations.this.register(scope, key, value);
        }

    }

}
