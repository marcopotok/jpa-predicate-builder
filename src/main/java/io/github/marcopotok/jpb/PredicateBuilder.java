package io.github.marcopotok.jpb;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public class PredicateBuilder<T> {

    private static final String WILDCARD_REQUEST = "\\*";
    private static final String WILDCARD_DB = "%";

    private static final Operator<Object> EQUALS = (value, path, cb) -> cb.equal(path, value);
    private static final Operator<String> EQUALS_UPPER_CASE = (value, path, cb) -> cb.equal(cb.upper(path), value);
    private static final Operator<Object> NOT_EQUALS = (value, path, cb) -> cb.notEqual(path, value);
    private static final Operator<Collection<?>> IN = (values, path, cb) -> path.in(values);
    private static final Operator<Collection<?>> NOT_IN = (values, path, cb) -> path.in(values).not();
    private static final Operator<?> IS_NULL = (values, path, cb) -> path.isNull();
    private static final Operator<?> NOT_NULL = (values, path, cb) -> path.isNotNull();
    private static final Operator<String> LIKE_UPPER_CASE = (value, path, cb) -> cb.like(cb.upper(path), value);
    private static final PredicateContext<?> DISJUNCTION = (root, query, cb) -> cb.disjunction();
    private static final PredicateContext<?> CONJUNCTION = (root, query, cb) -> cb.conjunction();

    private final PredicateRepository<T> predicates = new PredicateRepository<>();
    private final Map<String, Join<?, T>> joinCache = new HashMap<>();
    private final Collection<String> prefetches = new LinkedList<>();
    private final PrefetchEngine prefetchEngine;

    public PredicateBuilder() {
        this(new DefaultPrefetchEngine());
    }

    public PredicateBuilder(PrefetchEngine prefetchEngine) {
        this.prefetchEngine = prefetchEngine;
    }

    public static <T> PredicateBuilder<T> builder() {
        return new PredicateBuilder<>();
    }

    public static <T> PredicateBuilder<T> of(Class<T> clazz) {
        return builder();
    }

    public Predicate build(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        prefetches.forEach(prefetch -> prefetchEngine.prefetch(prefetch, root, query));
        return predicates.stream()
                .map(predicateContext -> predicateContext.toPredicate(root, query, criteriaBuilder))
                .reduce(criteriaBuilder::and)
                .orElse(criteriaBuilder.conjunction());
    }

    public PredicateBuilder<T> and(PredicateBuilder<T> other) {
        other.predicates.stream().forEach(this.predicates::add);
        this.prefetches.addAll(other.prefetches);
        return this;
    }

    public PredicateBuilder<T> and() {
        return this;
    }

    public PredicateBuilder<T> distinct() {
        predicates.add((root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);
            return criteriaBuilder.conjunction();
        });
        return this;
    }

    public PredicateBuilder<T> prefetch(String attributes) {
        this.prefetches.add(attributes);
        return this;
    }

    public <U> PredicateBuilder<T> withId(U id) {
        return withProperty("id", id);
    }

    public <U> PredicateBuilder<T> withProperty(String name, U value) {
        addPredicateContextIfHasValue(name, EQUALS, value);
        return this;
    }

    public <U> PredicateBuilder<T> withRequiredProperty(String name, U value) {
        if (value == null) {
            disjunct();
            return this;
        }
        return withProperty(name, value);
    }

    public <U extends String> PredicateBuilder<T> withPropertyIgnoreCase(String name, U value) {
        addPredicateContextIfHasValue(name, EQUALS_UPPER_CASE, value != null ? value.toUpperCase(Locale.ROOT) : null);
        return this;
    }

    public <U extends String> PredicateBuilder<T> withRequiredProperty(String name, U value) {
        return withRequiredProperty(name, value == null || value.isEmpty() ? null : (Object) value);
    }

    public <U> PredicateBuilder<T> withPropertyNot(String name, U value) {
        addPredicateContextIfHasValue(name, NOT_EQUALS, value);
        return this;
    }

    public PredicateBuilder<T> withPropertyIn(String name, Collection<?> values) {
        addPredicateContextIfHasValue(name, IN, values);
        return this;
    }

    public PredicateBuilder<T> withRequiredPropertyIn(String name, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            disjunct();
            return this;
        }
        return withPropertyIn(name, values);
    }

    public PredicateBuilder<T> withPropertyNotIn(String name, Collection<?> values) {
        addPredicateContextIfHasValue(name, NOT_IN, values);
        return this;
    }

    public PredicateBuilder<T> withNullProperty(String name) {
        addPredicateContext(name, IS_NULL, null);
        return this;
    }

    public PredicateBuilder<T> withNotNullProperty(String name) {
        addPredicateContext(name, NOT_NULL, null);
        return this;
    }

    public PredicateBuilder<T> withPropertyLikeIgnoreCase(String propertyName, String propertyValue) {
        if (propertyValue != null) {
            addPredicateContextIfHasValue(propertyName, LIKE_UPPER_CASE,
                    propertyValue.replaceAll(WILDCARD_REQUEST, WILDCARD_DB).toUpperCase(Locale.ROOT));
        }
        return this;
    }

    public PredicateBuilder<T> withPropertyStartingWith(String propertyName, String propertyValue) {
        String likeValue = propertyValue == null ? WILDCARD_DB : propertyValue.toUpperCase(Locale.ROOT) + WILDCARD_DB;
        addPredicateContextIfHasValue(propertyName, LIKE_UPPER_CASE, likeValue);
        return this;
    }

    public <U extends Comparable<? super U>> PredicateBuilder<T> withPropertyAfter(String propertyName, U from) {
        addPredicateContextIfHasValue(propertyName, (value, path, cb) -> cb.greaterThan(path, value), from);
        return this;
    }

    public <X extends Comparable<? super X>> PredicateBuilder<T> withPropertyAfterInclusive(String propertyName,
            X from) {
        addPredicateContextIfHasValue(propertyName, (value, path, cb) -> cb.greaterThanOrEqualTo(path, value), from);
        return this;
    }

    public <X extends Comparable<? super X>> PredicateBuilder<T> withPropertyBefore(String propertyName, X to) {
        addPredicateContextIfHasValue(propertyName, (value, path, cb) -> cb.lessThan(path, value), to);
        return this;
    }

    public <X extends Comparable<? super X>> PredicateBuilder<T> withPropertyBeforeInclusive(String propertyName,
            X to) {
        addPredicateContextIfHasValue(propertyName, (value, path, cb) -> cb.lessThanOrEqualTo(path, value), to);
        return this;
    }

    public <X extends Comparable<X>> PredicateBuilder<T> withPropertyMaxValue(Class<T> entityClass,
            Class<X> propertyClass, String propertyName) {
        predicates.add((root, query, cb) -> {
            Subquery<X> subQuery = query.subquery(propertyClass);
            Root<T> subRoot = subQuery.from(entityClass);
            Path<X> x = subRoot.get(propertyName);
            subQuery.select(cb.greatest(x));
            return cb.equal(root.get(propertyName), subQuery);
        });
        return this;
    }

    public <U> PredicateBuilder<T> with(U value, Function<U, Clause> operator) {
        Objects.requireNonNull(operator, "Operator cannot be null");
        if (value != null) {
            with(operator.apply(value));
        }
        return this;
    }

    public PredicateBuilder<T> with(Clause clause) {
        if (clause != null) {
            predicates.add((root, criteriaQuery, criteriaBuilder) -> clause.toPredicate(criteriaBuilder,
                    (path, joinOn) -> getPropertyPath(root, path, joinOn)));
        }
        return this;
    }

    public PredicateBuilder<T> groupBy(String... propertyName) {
        predicates.add((root, query, criteriaBuilder) -> {
            query.groupBy(Arrays.stream(propertyName).map(root::get).collect(Collectors.toList()));
            return criteriaBuilder.conjunction();
        });
        return this;
    }

    public PredicateBuilder<T> project(String... propertyName) {
        predicates.add((root, query, criteriaBuilder) -> {
            query.multiselect(Arrays.stream(propertyName).map(root::get).collect(Collectors.toList()));
            return criteriaBuilder.conjunction();
        });
        return this;
    }

    private <U> void addPredicateContextIfHasValue(String name, Operator<U> operator, U value) {
        if (value != null) {
            addPredicateContext(name, operator, value);
        }
    }

    private <U> void addPredicateContext(String name, Operator<U> operator, U value) {
        Objects.requireNonNull(name, "Property name cannot be null");
        predicates.add((root, criteriaQuery, criteriaBuilder) -> {
            Path<U> propertyPath = getPropertyPath(root, name, null);
            return operator.toPredicate(value, propertyPath, criteriaBuilder);
        });
    }

    private <U> Path<U> getPropertyPath(Root<T> root, String key, Function<Join<?, ?>, Predicate> joinOn) {
        String[] split = key.split("\\.");
        return getRelationPath(root, split, joinOn).get(split[split.length - 1]);
    }

    private From<?, T> getRelationPath(From<?, T> path, String[] split, Function<Join<?, ?>, Predicate> joinOn) {
        String currentPath = "";
        for (int i = 0; i < split.length - 1; i++) {
            String attributeName = split[i];
            currentPath += "." + attributeName;
            path = getPath(currentPath, path, attributeName);
        }
        return addRestrictions(path, joinOn);
    }

    private From<?, T> addRestrictions(From<?, T> path, Function<Join<?, ?>, Predicate> joinOn) {
        if (path instanceof Join && joinOn != null) {
            @SuppressWarnings("unchecked")
            Join<?, T> join = (Join<?, T>) path;
            path = join.on(joinOn.apply(join));
        }
        return path;
    }

    private Join<?, T> getPath(String currentPath, From<?, T> path, String attributeName) {
        return joinCache.computeIfAbsent(currentPath, ignored -> path.join(attributeName, JoinType.LEFT));
    }

    private void disjunct() {
        predicates.set(disjunction());
        predicates.freeze();
    }

    @SuppressWarnings("unchecked")
    private static <T> PredicateContext<T> conjunction() {
        return (PredicateContext<T>) CONJUNCTION;
    }

    @SuppressWarnings("unchecked")
    private static <T> PredicateContext<T> disjunction() {
        return (PredicateContext<T>) DISJUNCTION;
    }
}
