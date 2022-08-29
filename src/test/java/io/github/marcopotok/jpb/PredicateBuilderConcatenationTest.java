package io.github.marcopotok.jpb;

import static io.github.marcopotok.jpb.StringUtils.assertStringMatchesAndNotBlank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.criteria.Predicate;

class PredicateBuilderConcatenationTest {

    private PredicateBuilder<Object> builder;
    private FakeCriteriaQuery query;
    private PrefetchEngine prefetchEngine;
    private FakeRoot<Object> expectedRoot;

    private PredicateBuilder<Object> expectedBuilder;
    private FakeCriteriaQuery expectedQuery;
    private PrefetchEngine expectedPrefetchEngine;
    private FakeRoot<Object> root;

    @BeforeEach
    void setUp() {
        setupBuilder();
        setupExpectedBuilder();
    }

    private void setupBuilder() {
        root = new FakeRoot<>("");
        query = new FakeCriteriaQuery();
        prefetchEngine = new FakePrefetchEngine();
        builder = new PredicateBuilder<>(prefetchEngine);
    }

    private void setupExpectedBuilder() {
        expectedRoot = new FakeRoot<>("");
        expectedQuery = new FakeCriteriaQuery();
        expectedPrefetchEngine = new FakePrefetchEngine();
        expectedBuilder = new PredicateBuilder<>(expectedPrefetchEngine);
    }

    @Test
    void concatenateTwoBuildersShouldResultInCombinedPredicate() {
        builder.with((criteriaBuilder, pathProvider) -> new FakePredicate("this"))
                .and(new PredicateBuilder<>().with((criteriaBuilder, pathProvider) -> new FakePredicate("that")));
        expectedBuilder.with((criteriaBuilder, pathProvider) -> new FakePredicate("this"))
                .with((criteriaBuilder, pathProvider) -> new FakePredicate("that"));
        assertStringMatchesAndNotBlank(buildExpected().toString(), build().toString());
    }

    @Test
    void concatenationShouldPreserveOriginalDistinct() {
        builder.distinct().and(new PredicateBuilder<>());
        expectedBuilder.distinct();
        buildBoth();
        assertStringMatchesAndNotBlank(expectedQuery.toString(), query.toString());
    }

    @Test
    void concatenationShouldPreserveOtherDistinct() {
        builder.and(new PredicateBuilder<>().distinct());
        expectedBuilder.distinct();
        buildBoth();
        assertStringMatchesAndNotBlank(expectedQuery.toString(), query.toString());
    }

    @Test
    void concatenationShouldMergeJoins() {
        builder.and(new PredicateBuilder<>().withProperty("other.name", "name").withProperty("attribute.name", "name"));
        expectedBuilder.withProperty("attribute.name", "name")
                .withProperty("other.name", "name")
                .withProperty("other.surname", "surname");
        buildBoth();
        assertNotEquals(0, expectedRoot.getJoins().size());
        assertEquals(expectedRoot.getJoins().size(), root.getJoins().size());
    }

    @Test
    void concatenationShouldMergePrefetches() {
        builder.prefetch("name").and(new PredicateBuilder<>().prefetch("surname"));
        expectedBuilder.prefetch("name").prefetch("surname");
        buildBoth();
        assertStringMatchesAndNotBlank(expectedPrefetchEngine.toString(), prefetchEngine.toString());
    }

    private void buildBoth() {
        build();
        buildExpected();
    }

    private Predicate build() {
        return builder.build(root, query, new FakeCriteriaBuilder());
    }

    private Predicate buildExpected() {
        return expectedBuilder.build(expectedRoot, expectedQuery, new FakeCriteriaBuilder());
    }
}
