package net.amygdalum.allotropy;

public record ByType(Class<?> type) implements InjectionKey {

    public static ByType byType(Class<?> type) {
        return new ByType(type);
    }

    @Override
    public int compareTo(InjectionKey o) {
        if (o instanceof ByType that) {
            return this.type.getName().compareTo(that.type.getName());
        } else {
            return this.toString().compareTo(o.toString());
        }
    }

}
