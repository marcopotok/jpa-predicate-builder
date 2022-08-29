package io.github.marcopotok.jpb;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.FetchParent;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;

final class FakeFetch<X, Y> implements Fetch<X, Y> {

    private final Set<Fetch<Y, ?>> fetches = new LinkedHashSet<>();
    private final String attribute;

    FakeFetch(String attribute) {
        this.attribute = attribute;
    }

    @Override
    public String toString() {
        return String.format("%s%s", attribute, formatNestedFetches());
    }

    private String formatNestedFetches() {
        if (fetches.isEmpty()) {
            return "";
        }
        return fetches.stream().map(Objects::toString).collect(Collectors.joining(",", "(", ")"));
    }

    @Override
    public <X1, Y2> Fetch<X1, Y2> fetch(String attributeName) {
        FakeFetch<Y, X1> fetch = new FakeFetch<>(String.format("fetch %s", attributeName));
        fetches.add(fetch);
        @SuppressWarnings("unchecked")
        Fetch<X1, Y2> cast = (Fetch<X1, Y2>) fetch;
        return cast;
    }

    @Override
    public <X1, Y1> Fetch<X1, Y1> fetch(String attributeName, JoinType jt) {
        return fetch(attributeName);
    }

    @Override
    public Attribute<? super X, ?> getAttribute() {
        return null;
    }

    @Override
    public FetchParent<?, X> getParent() {
        return null;
    }

    @Override
    public JoinType getJoinType() {
        return null;
    }

    @Override
    public Set<Fetch<Y, ?>> getFetches() {
        return fetches;
    }

    @Override
    public <Y1> Fetch<Y, Y1> fetch(SingularAttribute<? super Y, Y1> attribute) {
        return null;
    }

    @Override
    public <Y1> Fetch<Y, Y1> fetch(SingularAttribute<? super Y, Y1> attribute, JoinType jt) {
        return null;
    }

    @Override
    public <Y1> Fetch<Y, Y1> fetch(PluralAttribute<? super Y, ?, Y1> attribute) {
        return null;
    }

    @Override
    public <Y1> Fetch<Y, Y1> fetch(PluralAttribute<? super Y, ?, Y1> attribute, JoinType jt) {
        return null;
    }
}
