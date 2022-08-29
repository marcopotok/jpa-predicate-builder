package io.github.marcopotok.jpb;

import java.util.function.Function;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

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
