package net.amygdalum.allotropy;

import java.util.Optional;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

public class DeviceDescriptor extends AbstractTestDescriptor implements InterpretableTestDescriptor {

    public static final String DEVICE = "device";

    private String deviceId;
    private Class<? extends Device> deviceClass;

    public DeviceDescriptor(UniqueId uniqueId, String deviceId, Class<? extends Device> deviceClass, String displayName) {
        super(uniqueId, displayName);
        this.deviceId = deviceId;
        this.deviceClass = deviceClass;
    }

    public static DeviceDescriptor fromParent(UniqueId parentId, String deviceId, Class<? extends Device> deviceClass) {
        return new DeviceDescriptor(parentId.append(DEVICE, deviceId), deviceId, deviceClass, deviceId);
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    @Override
    public Optional<TestSource> getSource() {
        return getParent().flatMap(p -> p.getSource());
    }

    @Override
    public void accept(Interpreter interpreter) {
        interpreter.visitDeviceDescriptor(this);
    }

    public String deviceId() {
        return deviceId;
    }

    public Class<? extends Device> getDeviceClass() {
        return deviceClass;
    }

    public <T extends Device> T newDevice(Class<T> deviceClass) {
        try {
            return deviceClass.getConstructor(String.class).newInstance(deviceId);
        } catch (ReflectiveOperationException e) {
            throw new DeviceResolutionException("cannot instantiate device of class '" + deviceClass.getName() + "'", e);
        }
    }
}
