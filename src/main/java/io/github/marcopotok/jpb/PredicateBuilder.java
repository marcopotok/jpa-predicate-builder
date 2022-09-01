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

    private static final PredicateContext<?> DISJUNCTION = (root, query, cb) -> cb.disjunction();
    private static final PredicateContext<?> CONJUNCTION = (root, query, cb) -> cb.conjunction();

    private final PredicateRepository<T> predicates = new PredicateRepository<>();
    private final Map<String, Join<?, T>> joinCache = new HashMap<>();
    private final Collection<String> prefetches = new LinkedList<>();
    private final PrefetchEngine prefetchEngine;

    /**
     * Initialize a {@link PredicateBuilder} with default prefetch engine
     */
    public PredicateBuilder() {
        this(new DefaultPrefetchEngine());
    }

    /**
     * Initialize a {@link PredicateBuilder} with custom prefetch engine
     */
    public PredicateBuilder(PrefetchEngine prefetchEngine) {
        this.prefetchEngine = prefetchEngine;
    }

    /**
     * Initialize a {@link PredicateBuilder} with default prefetch engine
     */
    public static <T> PredicateBuilder<T> builder() {
        return new PredicateBuilder<>();
    }

    /**
     * Initialize a {@link PredicateBuilder} with custom prefetch engine
     */
    public static <T> PredicateBuilder<T> of(Class<T> clazz) {
        return builder();
    }

    /**
     * Build the predicate
     *
     * @return the predicate created
     */
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

    /**
     * Add a predicate on where clause for entity's id is equal to {@code id}
     */
    public <U> PredicateBuilder<T> withId(U id) {
        return withProperty("id", id);
    }

    /**
     * Add a predicate on where clause for entity's {@code name} is equal to {@code value}.
     * If {@code value} is null, no predicate will be added.
     *
     * @param name  name of the field of the entity to filter.
     * @param value If null, no filtering will be performed
     */
    public <U> PredicateBuilder<T> withProperty(String name, U value) {
        addPredicateContextIfHasValue(name, Operators.EQUALS, value);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's {@code name} is equal to {@code value}.
     * If {@code value} is null, a disjunction predicate will be added.
     *
     * @param name  name of the field of the entity to filter.
     * @param value If null, a disjunction predicate will be added.
     */
    public <U> PredicateBuilder<T> withRequiredProperty(String name, U value) {
        if (value == null) {
            disjunct();
            return this;
        }
        return withProperty(name, value);
    }

    /**
     * Add a predicate on where clause for entity's {@code name} is equal ignore case to {@code value}.
     * If {@code value} is null, no predicate will be added.
     *
     * @param name  name of the field of the entity to filter.
     * @param value If null, no filtering will be performed
     */
    public <U extends String> PredicateBuilder<T> withPropertyIgnoreCase(String name, U value) {
        addPredicateContextIfHasValue(name, Operators.EQUALS_UPPER_CASE,
                value != null ? value.toUpperCase(Locale.ROOT) : null);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's {@code name} is equal to {@code value}.
     * If {@code value} is null or empty, no predicate will be added.
     *
     * @param name  name of the field of the entity to filter.
     * @param value If null or empty, no filtering will be performed
     */
    public <U extends String> PredicateBuilder<T> withRequiredProperty(String name, U value) {
        return withRequiredProperty(name, value == null || value.isEmpty() ? null : (Object) value);
    }

    /**
     * Add a predicate on where clause for entity's {@code name} is not equal to {@code value}.
     * If {@code value} is null, no predicate will be added.
     *
     * @param name  name of the field of the entity to filter.
     * @param value If null, no filtering will be performed
     */
    public <U> PredicateBuilder<T> withPropertyNot(String name, U value) {
        addPredicateContextIfHasValue(name, Operators.NOT_EQUALS, value);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's {@code name} is not equal ignore case to {@code value}.
     * If {@code value} is null, no predicate will be added.
     *
     * @param name  name of the field of the entity to filter.
     * @param value If null, no filtering will be performed
     */
    public <U extends String> PredicateBuilder<T> withPropertyNotIgnoreCase(String name, U value) {
        addPredicateContextIfHasValue(name, Operators.NOT_EQUALS_UPPER_CASE,
                value != null ? value.toUpperCase(Locale.ROOT) : null);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's {@code name} is in {@code values}.
     * If {@code values} is null, no predicate will be added.
     *
     * @param name   name of the field of the entity to filter.
     * @param values collection of values for a where clause. If null, no filtering will be performed
     */
    public PredicateBuilder<T> withPropertyIn(String name, Collection<?> values) {
        addPredicateContextIfHasValue(name, Operators.IN, values);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's {@code name} is in {@code values}.
     * If {@code values} is null or empty, a disjunction predicate will be added.
     *
     * @param name   name of the field of the entity to filter.
     * @param values collection of values for a where clause. If null or empty, a disjunction predicate will be added.
     */
    public PredicateBuilder<T> withRequiredPropertyIn(String name, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            disjunct();
            return this;
        }
        return withPropertyIn(name, values);
    }

    /**
     * Add a predicate on where clause for entity's {@code name} is not in {@code values}.
     * If {@code values} is null, no predicate will be added.
     *
     * @param name   name of the field of the entity to filter.
     * @param values collection of values for a where clause. If null, no filtering will be performed
     */
    public PredicateBuilder<T> withPropertyNotIn(String name, Collection<?> values) {
        addPredicateContextIfHasValue(name, Operators.NOT_IN, values);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's {@code name} is {@code null}.
     *
     * @param name name of the field of the entity to filter.
     */
    public PredicateBuilder<T> withNullProperty(String name) {
        addPredicateContext(name, Operators.IS_NULL, null);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's {@code name} is not {@code null}.
     *
     * @param name name of the field of the entity to filter.
     */
    public PredicateBuilder<T> withNotNullProperty(String name) {
        addPredicateContext(name, Operators.NOT_NULL, null);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's property {@code name} is like ignore case to {@code value}.
     * If {@code value} is null, no predicate will be added.
     *
     * @param name  name of the field of the entity to filter.
     * @param value If null, no filtering will be performed
     */
    public PredicateBuilder<T> withPropertyLikeIgnoreCase(String name, String value) {
        if (value != null) {
            addPredicateContextIfHasValue(name, Operators.LIKE_UPPER_CASE,
                    value.replaceAll(WILDCARD_REQUEST, WILDCARD_DB).toUpperCase(Locale.ROOT));
        }
        return this;
    }

    /**
     * Add a predicate on where clause for entity's property {@code name} starts with {@code value}.
     * If {@code value} is null, no predicate will be added.
     *
     * @param name  name of the field of the entity to filter.
     * @param value If null, no filtering will be performed
     */
    public PredicateBuilder<T> withPropertyStartingWith(String name, String value) {
        String likeValue = value == null ? WILDCARD_DB : value.toUpperCase(Locale.ROOT) + WILDCARD_DB;
        addPredicateContextIfHasValue(name, Operators.LIKE_UPPER_CASE, likeValue);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's property {@code name} is greater than {@code from}.
     * If {@code value} is null, no predicate will be added.
     *
     * @param name name of the field of the entity to filter.
     * @param from If null, no filtering will be performed
     */
    public <U extends Comparable<? super U>> PredicateBuilder<T> withPropertyAfter(String name, U from) {
        addPredicateContextIfHasValue(name, (value, path, cb) -> cb.greaterThan(path, value), from);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's property {@code name} greater than or equal to {@code from}.
     * If {@code value} is null, no predicate will be added.
     *
     * @param name name of the field of the entity to filter.
     * @param from If null, no filtering will be performed
     */
    public <X extends Comparable<? super X>> PredicateBuilder<T> withPropertyAfterInclusive(String name, X from) {
        addPredicateContextIfHasValue(name, (value, path, cb) -> cb.greaterThanOrEqualTo(path, value), from);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's property {@code name} is less than {@code from}.
     * If {@code value} is null, no predicate will be added.
     *
     * @param name  name of the field of the entity to filter.
     * @param to If null, no filtering will be performed
     */
    public <X extends Comparable<? super X>> PredicateBuilder<T> withPropertyBefore(String name, X to) {
        addPredicateContextIfHasValue(name, (value, path, cb) -> cb.lessThan(path, value), to);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's property {@code name} is less than or equal to {@code from}.
     * If {@code value} is null, no predicate will be added.
     *
     * @param name  name of the field of the entity to filter.
     * @param to If null, no filtering will be performed
     */
    public <X extends Comparable<? super X>> PredicateBuilder<T> withPropertyBeforeInclusive(String name,
            X to) {
        addPredicateContextIfHasValue(name, (value, path, cb) -> cb.lessThanOrEqualTo(path, value), to);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's property {@code name} with max value. 
     */
    public <X extends Comparable<X>> PredicateBuilder<T> withPropertyMaxValue(Class<T> entityClass,
            Class<X> propertyClass, String name) {
        predicates.add((root, query, cb) -> {
            Subquery<X> subQuery = query.subquery(propertyClass);
            Root<T> subRoot = subQuery.from(entityClass);
            Path<X> x = subRoot.get(name);
            subQuery.select(cb.greatest(x));
            return cb.equal(root.get(name), subQuery);
        });
        return this;
    }

    /**
     * Add a predicate on where clause by the means of custom operator
     */
    public <U> PredicateBuilder<T> with(U value, Function<U, Clause> operator) {
        Objects.requireNonNull(operator, "Operator cannot be null");
        if (value != null) {
            with(operator.apply(value));
        }
        return this;
    }

    /**
     * Add a predicate on where clause by the means of custom {@link Clause}
     */
    public PredicateBuilder<T> with(Clause clause) {
        if (clause != null) {
            predicates.add((root, criteriaQuery, criteriaBuilder) -> clause.toPredicate(criteriaBuilder,
                    (path, joinOn) -> getPropertyPath(root, path, joinOn)));
        }
        return this;
    }

    /**
     * Add a predicate with group by clause on {@code names}
     */
    public PredicateBuilder<T> groupBy(String... names) {
        predicates.add((root, query, criteriaBuilder) -> {
            query.groupBy(Arrays.stream(names).map(root::get).collect(Collectors.toList()));
            return criteriaBuilder.conjunction();
        });
        return this;
    }

    /**
     * Add a predicate for selection only a subset of the entity's fields.
     */
    public PredicateBuilder<T> project(String... names) {
        predicates.add((root, query, criteriaBuilder) -> {
            query.multiselect(Arrays.stream(names).map(root::get).collect(Collectors.toList()));
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
