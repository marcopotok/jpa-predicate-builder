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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

public class PredicateBuilder<T> {

    private static final String WILDCARD_REQUEST = "\\*";
    private static final String WILDCARD_DB = "%";

    private static final PredicateContext<?> DISJUNCTION = (root, query, cb) -> cb.disjunction();
    private static final PredicateContext<?> CONJUNCTION = (root, query, cb) -> cb.conjunction();

    private final PredicateRepository<T> predicates = new PredicateRepository<>();
    private final Map<String, Join<?, T>> joinCache = new HashMap<>();
    private final Collection<String> prefetches = new LinkedList<>();
    private final PrefetchEngine prefetchEngine;
    private final boolean isUniqueJoins;

    /**
     * Initialize a {@link PredicateBuilder} with default prefetch engine
     */
    public PredicateBuilder() {
        this(PredicateBuilderOptions.createDefault());
    }

    /**
     * Initialize a {@link PredicateBuilder} with custom prefetch engine
     *
     * @param options - must not be null
     */
    public PredicateBuilder(PredicateBuilderOptions options) {
        Objects.requireNonNull(options, "Options must not be null");
        prefetchEngine = options.getPrefetchEngine();
        isUniqueJoins = options.isJoinCacheIsEnabled();
    }

    /**
     * Initialize a {@link PredicateBuilder} with default prefetch engine
     */
    public static <T> PredicateBuilder<T> builder() {
        return new PredicateBuilder<>();
    }

    /**
     * Initialize a {@link PredicateBuilder} with custom prefetch engine
     *
     * @param clazz the class of predicate builder
     */
    public static <T> PredicateBuilder<T> of(Class<T> clazz) {
        return builder();
    }

    /**
     * Build the predicate
     *
     * @param root            - must not be null
     * @param query           - must not be null
     * @param criteriaBuilder - must not be null
     * @return the predicate created
     */
    public Predicate build(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        prefetches.forEach(prefetch -> prefetchEngine.prefetch(prefetch, root, query));
        return predicates.stream()
                .map(predicateContext -> predicateContext.toPredicate(root, query, criteriaBuilder))
                .reduce(criteriaBuilder::and)
                .orElse(conjunction().toPredicate(root, query, criteriaBuilder));
    }

    /**
     * Concatenate the {@code other} {@link PredicateBuilder} with the current one
     *
     * @param other - can be null
     * @return the conjunction of the builders
     */
    public PredicateBuilder<T> and(PredicateBuilder<T> other) {
        if (other != null) {
            other.predicates.stream().forEach(this.predicates::add);
            this.prefetches.addAll(other.prefetches);
        }
        return this;
    }

    /**
     * Syntactic sugar for concatenation of predicates
     *
     * @return the current builder
     */
    public PredicateBuilder<T> and() {
        return this;
    }

    /**
     * Make the query distinct
     *
     * @return the current builder
     */
    public PredicateBuilder<T> distinct() {
        predicates.add((root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);
            return criteriaBuilder.conjunction();
        });
        return this;
    }

    /**
     * Fetch the attributes
     *
     * @param attributes string representing the attributes to be prefetched
     * @return the current builder
     */
    public PredicateBuilder<T> prefetch(String attributes) {
        this.prefetches.add(attributes);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's id is equal to {@code id}
     *
     * @param id value of the id to filter by
     * @return the current builder
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
     * @return the current builder
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
     * @return the current builder
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
     * @param value If null, no filtering will be performed.
     * @return the current builder
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
     * @param value If null or empty, no filtering will be performed.
     * @return the current builder
     */
    public <U extends String> PredicateBuilder<T> withRequiredProperty(String name, U value) {
        return withRequiredProperty(name, value == null || value.isEmpty() ? null : (Object) value);
    }

    /**
     * Add a predicate on where clause for entity's {@code name} is not equal to {@code value}.
     * If {@code value} is null, no predicate will be added.
     *
     * @param name  name of the field of the entity to filter.
     * @param value If null, no filtering will be performed.
     * @return the current builder
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
     * @param value If null, no filtering will be performed.
     * @return the current builder
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
     * @param values collection of values for a where clause. If null, no filtering will be performed.
     * @return the current builder
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
     * @return the current builder
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
     * @param values collection of values for a where clause. If null, no filtering will be performed.
     * @return the current builder
     */
    public PredicateBuilder<T> withPropertyNotIn(String name, Collection<?> values) {
        addPredicateContextIfHasValue(name, Operators.NOT_IN, values);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's {@code name} is {@code null}.
     *
     * @param name name of the field of the entity to filter.
     * @return the current builder
     */
    public PredicateBuilder<T> withNullProperty(String name) {
        addPredicateContext(name, Operators.IS_NULL, null);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's {@code name} is not {@code null}.
     *
     * @param name name of the field of the entity to filter.
     * @return the current builder
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
     * @param value If null, no filtering will be performed.
     * @return the current builder
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
     * @param value If null, no filtering will be performed.
     * @return the current builder
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
     * @param from If null, no filtering will be performed.
     * @return the current builder
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
     * @param from If null, no filtering will be performed.
     * @return the current builder
     */
    public <X extends Comparable<? super X>> PredicateBuilder<T> withPropertyAfterInclusive(String name, X from) {
        addPredicateContextIfHasValue(name, (value, path, cb) -> cb.greaterThanOrEqualTo(path, value), from);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's property {@code name} is less than {@code from}.
     * If {@code value} is null, no predicate will be added.
     *
     * @param name name of the field of the entity to filter.
     * @param to   If null, no filtering will be performed.
     * @return the current builder
     */
    public <X extends Comparable<? super X>> PredicateBuilder<T> withPropertyBefore(String name, X to) {
        addPredicateContextIfHasValue(name, (value, path, cb) -> cb.lessThan(path, value), to);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's property {@code name} is less than or equal to {@code from}.
     * If {@code value} is null, no predicate will be added.
     *
     * @param name name of the field of the entity to filter.
     * @param to   If null, no filtering will be performed.
     * @return the current builder
     */
    public <X extends Comparable<? super X>> PredicateBuilder<T> withPropertyBeforeInclusive(String name, X to) {
        addPredicateContextIfHasValue(name, (value, path, cb) -> cb.lessThanOrEqualTo(path, value), to);
        return this;
    }

    /**
     * Add a predicate on where clause for entity's property {@code name} with max value.
     *
     * @param entityClass   the class of the entity.
     * @param propertyClass the class of the property.
     * @param name          name of the property
     * @return the current builder
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
     *
     * @param value    - can be null
     * @param operator - must not be null
     * @return the current builder
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
     *
     * @param clause - can be null
     * @return the current builder
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
     *
     * @param names - must not be null
     * @return the current builder
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
     *
     * @param names - must not be null
     * @return the current builder
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
        From<?, T> fromPath = path;
        for (int i = 0; i < split.length - 1; i++) {
            String attributeName = split[i];
            currentPath += "." + attributeName;
            fromPath = getPath(currentPath, fromPath, attributeName);
        }
        return addRestrictions(fromPath, joinOn);
    }

    private From<?, T> addRestrictions(From<?, T> path, Function<Join<?, ?>, Predicate> joinOn) {
        if (path instanceof Join && joinOn != null) {
            @SuppressWarnings("unchecked")
            Join<?, T> join = (Join<?, T>) path;
            return join.on(joinOn.apply(join));
        }
        return path;
    }

    private Join<?, T> getPath(String currentPath, From<?, T> path, String attributeName) {
        return isUniqueJoins ?
                joinCache.computeIfAbsent(currentPath, ignored -> path.join(attributeName, JoinType.LEFT)) :
                path.join(attributeName, JoinType.LEFT);
    }

    private void disjunct() {
        predicates.set(disjunction());
        predicates.freeze();
    }

    @SuppressWarnings("unchecked")
    private PredicateContext<T> conjunction() {
        return (PredicateContext<T>) CONJUNCTION;
    }

    @SuppressWarnings("unchecked")
    private PredicateContext<T> disjunction() {
        return (PredicateContext<T>) DISJUNCTION;
    }
}
