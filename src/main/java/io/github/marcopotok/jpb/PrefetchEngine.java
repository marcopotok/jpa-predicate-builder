package io.github.marcopotok.jpb;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public interface PrefetchEngine {
    /**
     * Fetch the attributes
     *
     * @param attributeList - must not be null.
     * @param root          - must not be null.
     * @param query         - must not be null.
     * @param <T>           class of the root
     */
    <T> void prefetch(String attributeList, Root<T> root, CriteriaQuery<?> query);
}
