package io.github.marcopotok.jpb;

import static io.github.marcopotok.jpb.StringUtils.assertStringMatches;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PredicateBuilderTest {

    private static final Object NO_VALUE = null;
    private static final String EMPTY_STRING = "";
    private static final Collection<?> NO_VALUES = null;
    private static final Collection<?> EMPTY_VALUES = List.of();
    private PredicateBuilder<Object> builder;
    private FakeCriteriaQuery query;
    private PrefetchEngine prefetchEngine;

    @BeforeEach
    void setUp() {
        query = new FakeCriteriaQuery();
        prefetchEngine = new FakePrefetchEngine();
        builder = new PredicateBuilder<>(PredicateBuilderOptions.builder().withPrefetchEngine(prefetchEngine).build());
    }

    @Test
    void canBuildAPredicate() {
        Assertions.assertDoesNotThrow(() -> build(builder));
    }

    @Test
    void canBuildAPredicateByFactoryMethod() {
        Assertions.assertDoesNotThrow(() -> build(PredicateBuilder.builder()));
    }

    @Test
    void canBuildAPredicateByTypedFactoryMethod() {
        Assertions.assertDoesNotThrow(() -> build(PredicateBuilder.of(Object.class)));
    }

    @Test
    void noPredicatesShouldResultInConjunction() {
        Predicate predicate = build(builder);
        assertStringMatches("1=1", predicate.toString());
    }

    @Test
    void withProperty() {
        Predicate predicate = build(builder.withProperty("attribute", "value"));
        assertStringMatches("attribute equal value", predicate.toString());
    }

    @Test
    void andShouldConcatenate() {
        Predicate predicate = build(builder.and().withProperty("attribute", "value"));
        assertStringMatches("attribute equal value", predicate.toString());
    }

    @Test
    void withPropertyNot() {
        Predicate predicate = build(builder.withPropertyNot("attribute", "value"));
        assertStringMatches("attribute not equal value", predicate.toString());
    }

    @Test
    void withPropertyNotIgnoreCase() {
        Predicate predicate = build(builder.withPropertyNotIgnoreCase("attribute", "value"));
        assertStringMatches("upper(attribute) not equal VALUE", predicate.toString());
    }

    @Test
    void withNoPropertyNotIgnoreCase() {
        Predicate predicate = build(builder.withPropertyNotIgnoreCase("attribute", (String) NO_VALUE));
        assertStringMatches("1=1", predicate.toString());
    }

    @Test
    void withPropertyIn() {
        Predicate predicate = build(builder.withPropertyIn("attribute", List.of("v1", "v2")));
        assertStringMatches("attribute in v1,v2", predicate.toString());
    }

    @Test
    void withPropertyNotIn() {
        Predicate predicate = build(builder.withPropertyNotIn("attribute", List.of("v1", "v2")));
        assertStringMatches("not attribute in v1,v2", predicate.toString());
    }

    @Test
    void withMultipleProperty() {
        Predicate predicate = build(builder.withProperty("attribute", "value").withProperty("attribute2", "value2"));
        assertStringMatches("attribute equal value and attribute2 equal value2", predicate.toString());
    }

    @Test
    void withMixedMultipleProperty() {
        Predicate predicate = build(builder.withProperty("attribute", "value")
                .withProperty("attribute2", "value2")
                .withPropertyIn("list", List.of(1, 2)));
        assertStringMatches("attribute equal value and attribute2 equal value2 and list in 1,2", predicate.toString());
    }

    @Test
    void withClause() {
        Predicate predicate = build(builder.with(
                (criteriaBuilder, pathProvider) -> criteriaBuilder.like(pathProvider.get("attribute").as(String.class),
                        "value")));
        assertStringMatches("attribute like value", predicate.toString());
    }

    @Test
    void withNullClause() {
        Predicate predicate = build(builder.with(null));
        assertStringMatches("1=1", predicate.toString());
    }

    @Test
    void withClauseAsType() {
        Predicate predicate = build(builder.with(
                (criteriaBuilder, pathProvider) -> criteriaBuilder.like(pathProvider.get("attribute", String.class),
                        "value")));
        assertStringMatches("attribute like value", predicate.toString());
    }

    @Test
    void withClauseAsTypeWithRestriction() {
        Predicate predicate = build(builder.with((criteriaBuilder, pathProvider) -> criteriaBuilder.like(
                pathProvider.get("attribute", join -> criteriaBuilder.equal(join.get("attribute"), "value"),
                        String.class), "value")));
        assertStringMatches("attribute like value", predicate.toString());
    }

    @Test
    void withClauseOperator() {
        Predicate predicate = build(builder.with("value",
                input -> (criteriaBuilder, pathProvider) -> criteriaBuilder.like(
                        pathProvider.get("attribute").as(String.class), input)));
        assertStringMatches("attribute like value", predicate.toString());
    }

    @Test
    void withNoValueClauseOperator() {
        Predicate predicate = build(builder.with((String) NO_VALUE,
                input -> (criteriaBuilder, pathProvider) -> criteriaBuilder.like(
                        pathProvider.get("attribute").as(String.class), input)));
        assertStringMatches("1=1", predicate.toString());
    }

    @Test
    void withComposedAndClause() {
        Clause clause = (criteriaBuilder, pathProvider) -> criteriaBuilder.like(
                pathProvider.get("attribute").as(String.class), "value");
        Predicate predicate = build(builder.with(clause.and(
                (criteriaBuilder, pathProvider) -> criteriaBuilder.equal(pathProvider.get("other"), "alternative"))));
        assertStringMatches("attribute like value and other equal alternative", predicate.toString());
    }

    @Test
    void withComposedAndNullClause() {
        Clause clause = (criteriaBuilder, pathProvider) -> criteriaBuilder.like(
                pathProvider.get("attribute").as(String.class), "value");
        Predicate predicate = build(builder.with(clause.and(null)));
        assertStringMatches("attribute like value", predicate.toString());
    }

    @Test
    void withComposedOrClause() {
        Clause clause = (criteriaBuilder, pathProvider) -> criteriaBuilder.like(
                pathProvider.get("attribute").as(String.class), "value");
        Predicate predicate = build(builder.with(clause.or(
                (criteriaBuilder, pathProvider) -> criteriaBuilder.equal(pathProvider.get("other"), "alternative"))));
        assertStringMatches("attribute like value or other equal alternative", predicate.toString());
    }

    @Test
    void withPropertyWithJoin() {
        Predicate predicate = build(builder.withProperty("attribute.nested.user.name", "name value"));
        assertStringMatches("join attribute join nested join user name equal name value", predicate.toString());
    }

    @Test
    void duplicatedJoinsShouldResultAsSingleJoin() {
        FakeRoot<Object> root = new FakeRoot<>("");
        builder.withProperty("attribute.name", "name")
                .withProperty("attribute.surname", "surname")
                .withProperty("attribute.email", "email")
                .build(root, query, new FakeCriteriaBuilder());
        Set<Join<Object, ?>> joins = root.getJoins();
        assertEquals(1, joins.size());
    }

    @Test
    void distinctJoinsShouldResultAsMultipleJoins() {
        FakeRoot<Object> root = new FakeRoot<>("");
        builder.withProperty("attribute.name", "name")
                .withProperty("other.name", "name")
                .build(root, query, new FakeCriteriaBuilder());
        Set<Join<Object, ?>> joins = root.getJoins();
        assertEquals(2, joins.size());
    }

    @Test
    void setDistinctShouldResultInDistinctQuery() {
        build(builder.distinct());
        assertStringMatches("distinct ", query.toString());
    }

    @Test
    void withId() {
        Predicate predicate = build(builder.withId("idValue"));
        assertStringMatches("id equal idValue", predicate.toString());
    }

    @Test
    void withRequiredProperty() {
        Predicate predicate = build(builder.withRequiredProperty("required", "value"));
        assertStringMatches("required equal value", predicate.toString());
    }

    @Test
    void requiredPropertyNotPresentShouldResultInDisjunction() {
        Predicate predicate = build(builder.withProperty("name", "ignored")
                .withRequiredProperty("required", (String) NO_VALUE)
                .withPropertyIn("profiles", List.of(1)));
        assertStringMatches("1=0", predicate.toString());
    }

    @Test
    void requiredPropertyEmptyStringShouldResultInDisjunction() {
        Predicate predicate = build(builder.withProperty("name", "ignored")
                .withRequiredProperty("required", EMPTY_STRING)
                .withPropertyIn("profiles", List.of(1)));
        assertStringMatches("1=0", predicate.toString());
    }

    @Test
    void requiredPropertyNotPresentShouldFreezePredicates() {
        Predicate predicate = build(builder.withProperty("name", "ignored")
                .withRequiredProperty("required", NO_VALUE)
                .withRequiredProperty("required", NO_VALUE)
                .withPropertyIn("profiles", List.of(1)));
        assertStringMatches("1=0", predicate.toString());
    }

    @Test
    void withRequiredPropertyIn() {
        Predicate predicate = build(builder.withRequiredPropertyIn("required", List.of("value")));
        assertStringMatches("required in value", predicate.toString());
    }

    @Test
    void withPropertyIgnoreCase() {
        Predicate predicate = build(builder.withPropertyIgnoreCase("case", "Value"));
        assertStringMatches("upper(case) equal VALUE", predicate.toString());
    }

    @Test
    void withPropertyNullIgnoreCase() {
        Predicate predicate = build(builder.withPropertyIgnoreCase("case", null));
        assertStringMatches("1=1", predicate.toString());
    }

    @Test
    void requiredPropertyInNotPresentShouldResultInDisjunction() {
        Predicate predicate = build(builder.withProperty("name", "ignored")
                .withRequiredPropertyIn("required", NO_VALUES)
                .withPropertyIn("profiles", List.of(1)));
        assertStringMatches("1=0", predicate.toString());
    }

    @Test
    void requiredPropertyInEmptyShouldResultInDisjunction() {
        Predicate predicate = build(builder.withProperty("name", "ignored")
                .withRequiredPropertyIn("required", EMPTY_VALUES)
                .withPropertyIn("profiles", List.of(1)));
        assertStringMatches("1=0", predicate.toString());
    }

    @Test
    void withNullProperty() {
        Predicate predicate = build(builder.withNullProperty("null"));
        assertStringMatches("null is null", predicate.toString());
    }

    @Test
    void withNotNullProperty() {
        Predicate predicate = build(builder.withNotNullProperty("name"));
        assertStringMatches("name is not null", predicate.toString());
    }

    @Test
    void withPropertyLikeIgnoreCase() {
        Predicate predicate = build(builder.withPropertyLikeIgnoreCase("name", "*n*"));
        assertStringMatches("upper(name) like %N%", predicate.toString());
    }

    @Test
    void withPropertyLikeIgnoreCaseNullValueShouldNotFilterOut() {
        Predicate predicate = build(builder.withPropertyLikeIgnoreCase("name", null));
        assertStringMatches("1=1", predicate.toString());
    }

    @Test
    void duplicatedConjunctionShouldResultInSingleConjunction() {
        Predicate predicate = build(builder.withProperty("name", null).withProperty("surname", null));
        assertStringMatches("1=1", predicate.toString());
    }

    @Test
    void withPropertyStartingWith() {
        Predicate predicate = build(builder.withPropertyStartingWith("name", "n"));
        assertStringMatches("upper(name) like N%", predicate.toString());
    }

    @Test
    void withNoPropertyStartingWith() {
        Predicate predicate = build(builder.withPropertyStartingWith("name", (String) NO_VALUE));
        assertStringMatches("upper(name) like %", predicate.toString());
    }

    @Test
    void withPropertyAfter() {
        Predicate predicate = build(builder.withPropertyAfter("version", 1));
        assertStringMatches("version > 1", predicate.toString());
    }

    @Test
    void withPropertyAfterInclusive() {
        Predicate predicate = build(builder.withPropertyAfterInclusive("version", 1));
        assertStringMatches("version >= 1", predicate.toString());
    }

    @Test
    void withPropertyBefore() {
        Predicate predicate = build(builder.withPropertyBefore("version", 1));
        assertStringMatches("version < 1", predicate.toString());
    }

    @Test
    void withPropertyBeforeInclusive() {
        Predicate predicate = build(builder.withPropertyBeforeInclusive("version", 1));
        assertStringMatches("version <= 1", predicate.toString());
    }

    @Test
    void withPropertyMaxValue() {
        Predicate predicate = build(builder.withPropertyMaxValue(Object.class, Long.class, "version"));
        assertStringMatches("version equal max(version)", predicate.toString());
    }

    @Test
    void joinOn() {
        Predicate predicate = build(builder.with((criteriaBuilder, pathProvider) -> {
            Path<?> path = pathProvider.get("relation.attribute",
                    (join) -> criteriaBuilder.equal(join.get("other"), "value"));
            return new FakePredicate(path.toString());
        }));
        assertStringMatches("join relation on [ join relation other equal value] attribute", predicate.toString());
    }

    @Test
    void groupBy() {
        build(builder.groupBy("name", "surname"));
        assertStringMatches("group by name,surname", query.toString());
    }

    @Test
    void project() {
        build(builder.project("name", "surname"));
        assertStringMatches("select name,surname", query.toString());
    }

    @Test
    void prefetch() {
        build(builder.prefetch("name").prefetch("surname"));
        assertStringMatches("prefetch name prefetch surname", prefetchEngine.toString());
    }

    private Predicate build(PredicateBuilder<Object> builder) {
        return builder.build(new FakeRoot<>(""), query, new FakeCriteriaBuilder());
    }
}