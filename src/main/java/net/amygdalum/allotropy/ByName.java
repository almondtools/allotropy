package net.amygdalum.allotropy;

public record ByName(String name) implements InjectionKey {

    public static ByName byName(String name) {
        return new ByName(name);
    }

    @Override
    public int compareTo(InjectionKey o) {
        if (o instanceof ByName that) {
            return this.name.compareTo(that.name);
        } else {
            return this.toString().compareTo(o.toString());
        }
    }

}
