package io.github.marcopotok.jpb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultPrefetchEngineTest {

    private DefaultPrefetchEngine engine;
    private FakeRoot<Object> root;
    private FakeCriteriaQuery query;

    @BeforeEach
    void setUp() {
        engine = new DefaultPrefetchEngine();
        root = new FakeRoot<>("");
        query = new FakeCriteriaQuery();
    }

    @Test
    void noPrefetchAttributesShouldResultInNoAction() {
        engine.prefetch("", root, query);
        assertStringMatches("", getFetches(root));
    }

    @Test
    void prefetchAttributesShouldResultInFetches() {

        engine.prefetch("attribute", root, query);
        assertStringMatches("(fetch attribute)", getFetches(root));
    }

    @Test
    void prefetchSameLevelAttributesShouldResultInFetches() {
        engine.prefetch("name,surname", root, query);
        engine.prefetch("name,surname", root, query);
        assertStringMatches("(fetch name)-(fetch surname)", getFetches(root));
    }

    @Test
    void prefetchChainedAttributesShouldResultInChainedFetches() {
        engine.prefetch("attribute.nested", root, query);
        assertStringMatches("(fetch attribute(fetch nested))", getFetches(root));
    }

    @Test
    void multipleNestedPrefetches() {
        engine.prefetch("attribute.[nested,other]", root, query);
        assertStringMatches("(fetch attribute(fetch nested,fetch other))", getFetches(root));
    }

    @Test
    void multipleNestedPrefetchesWithAttributes() {
        engine.prefetch("attribute.[nested.deep,other.deep]", root, query);
        assertStringMatches("(fetch attribute(fetch nested(fetch deep),fetch other(fetch deep)))", getFetches(root));
    }

    @Test
    void multipleNestingPrefetchesWithNestedAttributes() {
        engine.prefetch("first,attribute.[nested.[deep,other.[deeper,x]]]", root, query);
        assertStringMatches(
                "(fetch attribute(fetch nested(fetch deep,fetch other(fetch deeper,fetch x))))-(fetch first)",
                getFetches(root));
    }

    @Test
    void duplicatedPrefetchesShouldResultAsSingleFetch() {
        engine.prefetch("attribute", root, query);
        engine.prefetch("attribute.nested", root, query);
        assertStringMatches("(fetch attribute(fetch nested))", getFetches(root));
    }

    private String getFetches(FakeRoot<Object> root) {
        return root.getFetches().stream().map(Objects::toString).sorted().collect(Collectors.joining("-"));
    }

    private void assertStringMatches(String expected, String actual) {
        assertEquals(expected.trim(), actual.trim());
    }
}