package net.amygdalum.allotropy.util;

import static net.amygdalum.allotropy.util.Optionals.some;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class Collections {

    public static <S, T extends Collection<S>> Optional<T> nonEmpty(T collection) {
        return some(collection).filter(l -> !l.isEmpty());
    }

    public static <T> List<T> trunc(List<T> classes) {
        if (classes.size() < 1) {
            throw new NoSuchElementException();
        }
        return classes.subList(0, classes.size() - 1);
    }

    public static <T> T last(List<T> classes) {
        if (classes.size() < 1) {
            throw new NoSuchElementException();
        }
        return classes.get(classes.size() - 1);
    }

    public static <T> Optional<T> single(List<T> list) {
        if (list.size() == 1) {
            return Optionals.some(list.get(0));
        } else {
            return Optionals.none();
        }
    }

}
