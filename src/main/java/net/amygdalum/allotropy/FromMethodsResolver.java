package net.amygdalum.allotropy;

import static net.amygdalum.allotropy.AllotropyTestEngine.ENGINE_ID;
import static net.amygdalum.allotropy.ExecutableTestDescriptor.METHOD;
import static net.amygdalum.allotropy.util.Optionals.some;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Match.exact;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.match;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.unresolved;

import java.lang.reflect.Method;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.discovery.SelectorResolver;

public class FromMethodsResolver implements SelectorResolver {

    @Override
    public Resolution resolve(MethodSelector selector, Context context) {
        Method method = selector.getJavaMethod();
        if (!isAnnotated(method, WithDevice.class)
            && !isAnnotated(method, WithDevices.class)) {
            return unresolved();
        }
        return context.addToParent(() -> selectClass(selector.getJavaClass()), parent -> some(ExecutableTestDescriptor.fromParent(parent.getUniqueId(), method)))
            .map(s -> match(exact(s)))
            .orElse(unresolved());
    }

    @Override
    public Resolution resolve(UniqueIdSelector selector, Context context) {
        UniqueId uniqueId = selector.getUniqueId();
        if (uniqueId.getEngineId().stream().noneMatch(id -> id.equals(ENGINE_ID))) {
            return unresolved();
        }
        Segment lastSegment = uniqueId.getLastSegment();

        var otd = context.resolve(selectUniqueId(uniqueId.removeLastSegment()));

        Class<?> clazz = otd.flatMap(td -> td.getSource())
            .filter(ClassSource.class::isInstance)
            .map(ClassSource.class::cast)
            .map(c -> c.getJavaClass())
            .orElse(null);
        if (clazz == null) {
            return unresolved();
        }
        return switch (lastSegment.getType()) {
        case METHOD -> resolve(selectMethod(clazz, lastSegment.getValue()), context);
        default -> unresolved();
        };
    }

}
