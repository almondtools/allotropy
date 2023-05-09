package net.amygdalum.allotropy;

import static java.util.stream.Collectors.toSet;
import static net.amygdalum.allotropy.DeviceDescriptor.DEVICE;
import static net.amygdalum.allotropy.TestContainerDescriptor.CONTAINER;
import static net.amygdalum.allotropy.ViewContainerDescriptor.VIEW;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static org.junit.platform.commons.support.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.support.HierarchyTraversalMode.TOP_DOWN;
import static org.junit.platform.commons.support.ReflectionSupport.findFields;
import static org.junit.platform.commons.support.ReflectionSupport.findMethods;
import static org.junit.platform.commons.support.ReflectionSupport.findNestedClasses;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectNestedClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Match.exact;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.match;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.unresolved;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.NestedClassSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

public class FromClassesResolver implements SelectorResolver {

    @Override
    public Resolution resolve(ClassSelector selector, Context context) {
        Class<?> clazz = selector.getJavaClass();
        if (isAnnotated(clazz, RegisterDevices.class)) {
            return context.addToParent(parent -> Optional.of(testContainer(parent, clazz)))
                .map(td -> match(exact(td, children(clazz))))
                .orElse(unresolved());
        } else {
            List<Class<?>> hierarchy = resolveHierarchy(clazz);
            NestedClassSelector nestedSelector = selectNestedClass(hierarchy, clazz);
            return resolve(nestedSelector, context);
        }
    }

    private List<Class<?>> resolveHierarchy(Class<?> clazz) {
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> enclosing;
        while ((enclosing = clazz.getEnclosingClass()) != null) {
            hierarchy.add(0, enclosing);
            clazz = enclosing;
        }
        return hierarchy;
    }

    @Override
    public Resolution resolve(NestedClassSelector selector, Context context) {
        Class<?> nestedClass = selector.getNestedClass();
        TestDescriptor td = context.addToParent(() -> selectClassFrom(selector.getEnclosingClasses()), parent -> Optional.of(testContainer(parent, nestedClass)))
            .orElse(null);
        return Resolution.match(exact(td, children(nestedClass)));
    }

    private DiscoverySelector selectClassFrom(List<Class<?>> classes) {
        if (classes.size() == 1) {
            return selectClass(classes.get(0));
        }
        int lastIndex = classes.size() - 1;
        return selectNestedClass(classes.subList(0, lastIndex), classes.get(lastIndex));
    }

    @Override
    public Resolution resolve(UniqueIdSelector selector, Context context) {
        UniqueId uniqueId = selector.getUniqueId();
        if (uniqueId.getEngineId().stream().noneMatch(id -> id.equals(AllotropyTestEngine.ENGINE_ID))) {
            return unresolved();
        }
        Segment lastSegment = uniqueId.getLastSegment();

        return switch (lastSegment.getType()) {
        case VIEW -> resolve(selectClass(lastSegment.getValue()), context);
        case CONTAINER -> resolve(selectClass(lastSegment.getValue()), context);
        case DEVICE -> {
            Resolution resolution = resolve(selectUniqueId(uniqueId.removeLastSegment()), context);
            for (var match : resolution.getMatches()) {
                if (match.getTestDescriptor() instanceof ViewContainerDescriptor view) {
                    view.allow(lastSegment.getValue());
                }
            }
            yield resolution;
        }
        default -> unresolved();
        };

    }

    private TestDescriptor testContainer(TestDescriptor parent, Class<?> clazz) {
        if (findMethods(clazz, m -> isAnnotated(m, View.class), TOP_DOWN).isEmpty()
            && findFields(clazz, f -> isAnnotated(f, ViewURL.class), BOTTOM_UP).isEmpty()) {
            return TestContainerDescriptor.fromParent(parent.getUniqueId(), clazz);
        } else {
            return ViewContainerDescriptor.fromParent(parent.getUniqueId(), clazz);
        }
    }

    private Supplier<Set<? extends DiscoverySelector>> children(Class<?> clazz) {
        return () -> Stream.concat(
            findNestedClasses(clazz, c -> true).stream()
                .map(c -> selectClass(c)),
            findMethods(clazz, m -> true, TOP_DOWN).stream()
                .map(m -> selectMethod(clazz, m)))
            .collect(toSet());
    }

}
