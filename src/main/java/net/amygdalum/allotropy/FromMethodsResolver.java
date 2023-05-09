package net.amygdalum.allotropy;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Match.exact;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.match;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.unresolved;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.discovery.SelectorResolver;

public class FromMethodsResolver implements SelectorResolver {

    @Override
    public Resolution resolve(MethodSelector selector, Context context) {
        Method method = selector.getJavaMethod();
        if (!AnnotationSupport.isAnnotated(method, WithDevice.class)
            && !AnnotationSupport.isAnnotated(method, WithDevices.class)) {
            return Resolution.unresolved();
        }
        return context.addToParent(() -> DiscoverySelectors.selectClass(selector.getJavaClass()), parent -> Optional.of(ExecutableTestDescriptor.fromParent(parent.getUniqueId(), method)))
            .map(s -> match(exact(s)))
            .orElse(unresolved());
    }

    @Override
    public Resolution resolve(UniqueIdSelector selector, Context context) {
        UniqueId uniqueId = selector.getUniqueId();
        if (uniqueId.getEngineId().stream().noneMatch(id -> id.equals(AllotropyTestEngine.ENGINE_ID))) {
            return unresolved();
        }
        Segment lastSegment = uniqueId.getLastSegment();

        Optional<TestDescriptor> container = context.resolve(DiscoverySelectors.selectUniqueId(uniqueId.removeLastSegment()));

        Class<?> clazz = container.flatMap(c -> c.getSource())
            .filter(ClassSource.class::isInstance)
            .map(ClassSource.class::cast)
            .map(c -> c.getJavaClass())
            .orElse(null);
        if (clazz == null) {
            return unresolved();
        }
        return switch (lastSegment.getType()) {
        case ExecutableTestDescriptor.METHOD -> resolve(selectMethod(clazz, lastSegment.getValue()), context);
        default -> unresolved();
        };

    }

}
