package net.amygdalum.allotropy.util;

import java.util.Optional;

public class Optionals {

    public static <T> Optional<T> some(T value) {
        return Optional.of(value);
    }

    public static <T> Optional<T> any(T value) {
        return Optional.ofNullable(value);
    }

    public static <T> Optional<T> none() {
        return Optional.empty();
    }

}
