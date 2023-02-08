package io.github.marcopotok.jpb;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.criteria.Predicate;

class ClauseCompositionTest {

    private static final PathProvider PATH_PROVIDER = null;
    private Clause first;
    private Clause second;
    private FakeCriteriaBuilder criteriaBuilder;

    @BeforeEach
    void setUp() {
        first = Clause.CONJUNCTION;
        second = Clause.DISJUNCTION;
        criteriaBuilder = new FakeCriteriaBuilder();
    }

    @Test
    void andComposeWithNull() {
        Clause and = first.and(null);
        assertNotNull(and);
        Predicate predicate = and.toPredicate(criteriaBuilder, PATH_PROVIDER);
        StringUtils.assertStringMatches(predicate, criteriaBuilder.conjunction());
    }

    @Test
    void orComposeWithNull() {
        Clause and = first.or(null);
        assertNotNull(and);
        Predicate predicate = and.toPredicate(criteriaBuilder, PATH_PROVIDER);
        StringUtils.assertStringMatches(predicate, criteriaBuilder.conjunction());
    }

    @Test
    void composeWithLeftClauseNull() {
        Clause composed = ClauseComposition.composed(null, second, (builder, lhs, rhs) -> builder.conjunction());
        assertNotNull(composed);
        Predicate predicate = composed.toPredicate(criteriaBuilder, PATH_PROVIDER);
        StringUtils.assertStringMatches(predicate, criteriaBuilder.disjunction());
    }

    @Test
    void composeWithBothClausesNull() {
        Clause composed = ClauseComposition.composed(null, null, (builder, lhs, rhs) -> builder.conjunction());
        assertNotNull(composed);
        Predicate predicate = composed.toPredicate(criteriaBuilder, PATH_PROVIDER);
        assertNull(predicate);
    }
}