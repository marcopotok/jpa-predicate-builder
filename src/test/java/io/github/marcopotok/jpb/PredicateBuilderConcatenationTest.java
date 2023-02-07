package io.github.marcopotok.jpb;

import static io.github.marcopotok.jpb.StringUtils.assertStringMatchesAndNotBlank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import javax.persistence.criteria.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PredicateBuilderConcatenationTest {

    private PredicateBuilder<Object> builder;
    private FakeCriteriaQuery query;
    private PredicateBuilderOptions options;
    private FakeRoot<Object> expectedRoot;

    private PredicateBuilder<Object> expectedBuilder;
    private FakeCriteriaQuery expectedQuery;
    private PredicateBuilderOptions expectedOptions;
    private FakeRoot<Object> root;

    @BeforeEach
    void setUp() {
        setupBuilder();
        setupExpectedBuilder();
    }

    private void setupBuilder() {
        root = new FakeRoot<>("");
        query = new FakeCriteriaQuery();
        options = PredicateBuilderOptions.builder().withPrefetchEngine(new FakePrefetchEngine()).build();
        builder = new PredicateBuilder<>(options);
    }

    private void setupExpectedBuilder() {
        expectedRoot = new FakeRoot<>("");
        expectedQuery = new FakeCriteriaQuery();
        expectedOptions = PredicateBuilderOptions.builder().withPrefetchEngine(new FakePrefetchEngine()).build();
        expectedBuilder = new PredicateBuilder<>(expectedOptions);
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
    void concatenateNullBuilderShouldResultInOriginalPredicate() {
        builder = builder.with((criteriaBuilder, pathProvider) -> new FakePredicate("this")).and(null);
        expectedBuilder.with((criteriaBuilder, pathProvider) -> new FakePredicate("this"));
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
        assertStringMatchesAndNotBlank(expectedOptions.getPrefetchEngine().toString(),
                options.getPrefetchEngine().toString());
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
