package io.github.marcopotok.jpb;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

class PredicateRepository<T> {

    private final List<PredicateContext<T>> predicates = new LinkedList<>();
    private boolean isFrozen;

    public void add(PredicateContext<T> context) {
        if (isFrozen) {
            return;
        }
        this.predicates.add(context);
    }

    public Stream<PredicateContext<T>> stream() {
        return this.predicates.stream();
    }

    public void set(PredicateContext<T> context) {
        if (isFrozen) {
            return;
        }
        this.predicates.clear();
        this.predicates.add(context);
    }

    public void freeze() {
        this.isFrozen = true;
    }
}
