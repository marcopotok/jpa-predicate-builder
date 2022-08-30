package io.github.marcopotok.jpb;

import java.util.Collection;

class Operators {
    static final Operator<Object> EQUALS = (value, path, cb) -> cb.equal(path, value);
    static final Operator<String> EQUALS_UPPER_CASE = (value, path, cb) -> cb.equal(cb.upper(path), value);
    static final Operator<Object> NOT_EQUALS = (value, path, cb) -> cb.notEqual(path, value);
    static final Operator<String> NOT_EQUALS_UPPER_CASE = (value, path, cb) -> cb.notEqual(cb.upper(path), value);
    static final Operator<Collection<?>> IN = (values, path, cb) -> path.in(values);
    static final Operator<Collection<?>> NOT_IN = (values, path, cb) -> path.in(values).not();
    static final Operator<?> IS_NULL = (values, path, cb) -> path.isNull();
    static final Operator<?> NOT_NULL = (values, path, cb) -> path.isNotNull();
    static final Operator<String> LIKE_UPPER_CASE = (value, path, cb) -> cb.like(cb.upper(path), value);
}
