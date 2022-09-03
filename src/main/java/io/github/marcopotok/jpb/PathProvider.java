package io.github.marcopotok.jpb;

import java.util.function.Function;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

@FunctionalInterface
public interface PathProvider {

    Path<?> get(String name, Function<Join<?, ?>, Predicate> restriction);

    default Path<?> get(String name) {
        return get(name, (Function<Join<?, ?>, Predicate>) null);
    }

    default <T> Expression<T> get(String name, Class<T> clazz) {
        return get(name).as(clazz);
    }

    default <T> Expression<T> get(String name, Function<Join<?, ?>, Predicate> restriction, Class<T> clazz) {
        return get(name, restriction).as(clazz);
    }
}
