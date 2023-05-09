package net.amygdalum.allotropy;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestDescriptor.Visitor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver.InitializationContext;

public class GroupByView implements Function<InitializationContext<AllotropyEngineDescriptor>, Visitor> {

    @Override
    public Visitor apply(InitializationContext<AllotropyEngineDescriptor> t) {
        return new Monomorphizer();
    }

    private static class Monomorphizer implements Visitor {

        @Override
        public void visit(TestDescriptor descriptor) {
            if (descriptor instanceof ViewContainerDescriptor view) {
                List<String> deviceIds = view.deviceIds();
                if (deviceIds.isEmpty()) {
                    view.removeFromHierarchy();
                    return;
                }
                new ArrayList<>(view.getChildren())
                    .stream()
                    .filter(PrototypeTestdescriptor.class::isInstance)
                    .map(PrototypeTestdescriptor.class::cast)
                    .forEach(c -> monomomorphize(view, c, deviceIds));

            } else {
                new ArrayList<>(descriptor.getChildren()).forEach(c -> c.accept(this));
            }
        }

        private void monomomorphize(ViewContainerDescriptor view, PrototypeTestdescriptor child, List<String> deviceIds) {
            view.removeChild(child);
            for (var deviceId : deviceIds) {
                UniqueId rootId = view.getUniqueId();
                Class<? extends Device> deviceClass = view.deviceClassFor(deviceId);
                DeviceDescriptor parent = view.getChildren().stream()
                    .filter(DeviceDescriptor.class::isInstance)
                    .map(DeviceDescriptor.class::cast)
                    .filter(c -> c.deviceId().equals(deviceId))
                    .findFirst()
                    .orElseGet(() -> DeviceDescriptor.fromParent(rootId, deviceId, deviceClass));
                view.addChild(parent);
                UniqueId testId = parent.getUniqueId();
                monomorphize(child, rootId, testId, deviceId).ifPresent(parent::addChild);
            }
        }

        private Optional<TestDescriptor> monomorphize(PrototypeTestdescriptor prototype, UniqueId from, UniqueId to, String deviceId) {
            UniqueId protoId = prototype.getUniqueId();
            Deque<Segment> segments = new LinkedList<>();
            while (!protoId.equals(from)) {
                segments.addFirst(protoId.getLastSegment());
                protoId = protoId.removeLastSegment();
            }
            UniqueId id = to;
            for (var segment : segments) {
                id = id.append(segment);
            }

            TestDescriptor descriptor = prototype.create(id);
            for (var protoChild : prototype.getChildren().stream().filter(PrototypeTestdescriptor.class::isInstance).map(PrototypeTestdescriptor.class::cast)
                .toList()) {
                monomorphize(protoChild, from, to, deviceId).ifPresent(descriptor::addChild);
            }
            if (!descriptor.getChildren().isEmpty()) {
                return Optional.of(descriptor);
            } else if (descriptor instanceof ExecutableTestDescriptor exec && exec.supportsDevice(deviceId)) {
                return Optional.of(descriptor);
            } else {
                return Optional.empty();
            }
        }

    }

}
