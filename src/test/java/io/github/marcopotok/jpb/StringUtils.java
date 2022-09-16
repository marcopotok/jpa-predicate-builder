package io.github.marcopotok.jpb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class StringUtils {

    private StringUtils() {
    }

    static void assertStringMatches(String expected, String actual) {
        assertEquals(expected.trim(), actual.trim());
    }

    static void assertStringMatches(Object expected, Object actual) {
        assertStringMatches(expected.toString(), actual.toString());
    }

    static void assertStringMatchesAndNotBlank(String expected, String actual) {
        assertNotNull(expected);
        assertFalse(expected.isBlank(), "Expected should not be blank");
        assertStringMatches(expected, actual);
    }
}
