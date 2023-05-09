package net.amygdalum.allotropy;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.HierarchyTraversalMode.BOTTOM_UP;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;

public class ViewContainerDescriptor extends AbstractTestDescriptor implements InterpretableTestDescriptor {

    public static final String VIEW = "view";

    private List<String> deviceIdRestrictions;

    public ViewContainerDescriptor(UniqueId uniqueId, String displayName, TestSource source) {
        super(uniqueId, displayName, source);
        this.deviceIdRestrictions = new ArrayList<>();
    }

    public static ViewContainerDescriptor fromParent(UniqueId parentId, Class<?> clazz) {
        return new ViewContainerDescriptor(parentId.append(VIEW, clazz.getName()), resolveDisplayName(clazz), sourceOf(clazz));
    }

    private static String resolveDisplayName(Class<?> clazz) {
        return clazz.getName();
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
        interpreter.visitViewContainerDescriptor(this);
    }

    public ClassSource getClassSource() {
        return getSource()
            .filter(ClassSource.class::isInstance)
            .map(ClassSource.class::cast)
            .orElse(null);
    }

    private Stream<Class<?>> streamHierarchy(Class<?> clazz) {
        Builder<Class<?>> builder = Stream.builder();
        builder.add(clazz);
        Class<?> enclosing;
        while ((enclosing = clazz.getEnclosingClass()) != null) {
            builder.add(enclosing);
            clazz = enclosing;
        }
        return builder.build();
    }

    public void allow(String deviceId) {
        deviceIdRestrictions.add(deviceId);
    }

    public List<String> deviceIds() {
        return streamHierarchy(getClassSource().getJavaClass())
            .flatMap(c -> AnnotationSupport.findRepeatableAnnotations(c, RegisterDevice.class).stream())
            .map(a -> a.id())
            .filter(id -> deviceIdRestrictions.isEmpty() || deviceIdRestrictions.contains(id))
            .collect(Collectors.toList());
    }

    public Class<? extends Device> deviceClassFor(String deviceId) {
        return streamHierarchy(getClassSource().getJavaClass())
            .flatMap(c -> AnnotationSupport.findRepeatableAnnotations(c, RegisterDevice.class).stream())
            .filter(a -> a.id().equals(deviceId))
            .map(a -> a.device())
            .findFirst()
            .orElseThrow(() -> new DeviceResolutionException("cannot resolve device with id '" + deviceId + "'"));
    }

    public Method getViewMethod() {
        return AnnotationSupport.findAnnotatedMethods(getClassSource().getJavaClass(), View.class, BOTTOM_UP)
            .stream()
            .findFirst()
            .orElseThrow();
    }

    public ViewObject getViewObject() {
        Field viewField = AnnotationSupport.findAnnotatedFields(getClassSource().getJavaClass(), ViewURL.class)
            .stream()
            .findFirst()
            .orElseThrow();
        ViewURL viewURL = findAnnotation(viewField, ViewURL.class).orElseThrow();

        return new ViewObject(viewField, viewURL);
    }

}