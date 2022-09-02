package io.github.marcopotok.jpb;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public interface PrefetchEngine {
    <T> void prefetch(String attributeList, Root<T> root, CriteriaQuery<?> query);
}
