package io.github.marcopotok.jpb;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public interface PrefetchEngine {
    <T> void prefetch(String attributeList, Root<T> root, CriteriaQuery<?> query);
}
