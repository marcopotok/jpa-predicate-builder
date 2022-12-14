package io.github.marcopotok.jpb;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

class FakePrefetchEngine implements PrefetchEngine {

    private String prefetch = "";

    @Override
    public String toString() {
        return prefetch;
    }

    @Override
    public <T> void prefetch(String attributeList, Root<T> root, CriteriaQuery<?> query) {
        prefetch += " prefetch " + attributeList;
    }
}
