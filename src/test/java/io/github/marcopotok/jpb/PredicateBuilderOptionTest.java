package io.github.marcopotok.jpb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import javax.persistence.criteria.Join;

import org.junit.jupiter.api.Test;

class PredicateBuilderOptionTest {

    @Test
    void duplicatedJoinsShouldResultAsSingleJoin() {
        FakeCriteriaQuery query = new FakeCriteriaQuery();
        PrefetchEngine prefetchEngine = new FakePrefetchEngine();
        PredicateBuilder<Object> builder = new PredicateBuilder<>(
                PredicateBuilderOptions.builder().withPrefetchEngine(prefetchEngine).withoutJoinsCache().build());
        FakeRoot<Object> root = new FakeRoot<>("");
        builder.withProperty("attribute.name", "name")
                .withProperty("attribute.surname", "surname")
                .withProperty("attribute.email", "email")
                .build(root, query, new FakeCriteriaBuilder());
        Set<Join<Object, ?>> joins = root.getJoins();
        assertEquals(3, joins.size());
    }
}