package io.github.marcopotok.jpb;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

final class FakeRoot<T> implements Root<T> {

    private final String attributeName;
    private final Set<Join<T, ?>> joins = new HashSet<>();
    private final Set<Fetch<T, ?>> fetches = new HashSet<>();

    FakeRoot(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public String toString() {
        return attributeName;
    }

    @Override
    public <X, Y> Join<X, Y> join(String attributeName) {
        FakeJoin<T, ?> join = new FakeJoin<>(this.attributeName);
        joins.add(join);
        return join.join(attributeName);
    }

    @Override
    public <X, Y> Join<X, Y> join(String attributeName, JoinType jt) {
        return join(attributeName);
    }

    @Override
    public <Y> Path<Y> get(String attributeName) {
        return new FakeRoot<>(attributeName);
    }

    @Override
    public Predicate isNull() {
        return new FakePredicate(this + " is null");
    }

    @Override
    public Predicate isNotNull() {
        return new FakePredicate(this + " is not null");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> Expression<X> as(Class<X> type) {
        return (Expression<X>) this;
    }

    @Override
    public Set<Fetch<T, ?>> getFetches() {
        return fetches;
    }

    @Override
    public Predicate in(Collection<?> values) {
        return new FakePredicate(
                attributeName + " in " + values.stream().map(Object::toString).collect(Collectors.joining(",")));
    }

    @Override
    public <X, Y> Fetch<X, Y> fetch(String attributeName) {
        Fetch<T, ?> fetch = new FakeFetch<>(this.attributeName);
        fetches.add(fetch);
        return fetch.fetch(attributeName);
    }

    @Override
    public <X, Y> Fetch<X, Y> fetch(String attributeName, JoinType jt) {
        return fetch(attributeName);
    }

    @Override
    public EntityType<T> getModel() {
        return null;
    }

    @Override
    public Path<?> getParentPath() {
        return null;
    }

    @Override
    public <Y> Path<Y> get(SingularAttribute<? super T, Y> attribute) {
        return null;
    }

    @Override
    public <E, C extends Collection<E>> Expression<C> get(PluralAttribute<T, C, E> collection) {
        return null;
    }

    @Override
    public <K, V, M extends Map<K, V>> Expression<M> get(MapAttribute<T, K, V> map) {
        return null;
    }

    @Override
    public Expression<Class<? extends T>> type() {
        return null;
    }

    @Override
    public Set<Join<T, ?>> getJoins() {
        return joins;
    }

    @Override
    public boolean isCorrelated() {
        return false;
    }

    @Override
    public From<T, T> getCorrelationParent() {
        return null;
    }

    @Override
    public <Y> Join<T, Y> join(SingularAttribute<? super T, Y> attribute) {
        return null;
    }

    @Override
    public <Y> Join<T, Y> join(SingularAttribute<? super T, Y> attribute, JoinType jt) {
        return null;
    }

    @Override
    public <Y> CollectionJoin<T, Y> join(CollectionAttribute<? super T, Y> collection) {
        return null;
    }

    @Override
    public <Y> SetJoin<T, Y> join(SetAttribute<? super T, Y> set) {
        return null;
    }

    @Override
    public <Y> ListJoin<T, Y> join(ListAttribute<? super T, Y> list) {
        return null;
    }

    @Override
    public <K, V> MapJoin<T, K, V> join(MapAttribute<? super T, K, V> map) {
        return null;
    }

    @Override
    public <Y> CollectionJoin<T, Y> join(CollectionAttribute<? super T, Y> collection, JoinType jt) {
        return null;
    }

    @Override
    public <Y> SetJoin<T, Y> join(SetAttribute<? super T, Y> set, JoinType jt) {
        return null;
    }

    @Override
    public <Y> ListJoin<T, Y> join(ListAttribute<? super T, Y> list, JoinType jt) {
        return null;
    }

    @Override
    public <K, V> MapJoin<T, K, V> join(MapAttribute<? super T, K, V> map, JoinType jt) {
        return null;
    }

    @Override
    public <X, Y> CollectionJoin<X, Y> joinCollection(String attributeName) {
        return null;
    }

    @Override
    public <X, Y> SetJoin<X, Y> joinSet(String attributeName) {
        return null;
    }

    @Override
    public <X, Y> ListJoin<X, Y> joinList(String attributeName) {
        return null;
    }

    @Override
    public <X, K, V> MapJoin<X, K, V> joinMap(String attributeName) {
        return null;
    }

    @Override
    public <X, Y> CollectionJoin<X, Y> joinCollection(String attributeName, JoinType jt) {
        return null;
    }

    @Override
    public <X, Y> SetJoin<X, Y> joinSet(String attributeName, JoinType jt) {
        return null;
    }

    @Override
    public <X, Y> ListJoin<X, Y> joinList(String attributeName, JoinType jt) {
        return null;
    }

    @Override
    public <X, K, V> MapJoin<X, K, V> joinMap(String attributeName, JoinType jt) {
        return null;
    }

    @Override
    public Predicate in(Object... values) {
        return null;
    }

    @Override
    public Predicate in(Expression<?>... values) {
        return null;
    }

    @Override
    public Predicate in(Expression<Collection<?>> values) {
        return null;
    }

    @Override
    public <Y> Fetch<T, Y> fetch(SingularAttribute<? super T, Y> attribute) {
        return null;
    }

    @Override
    public <Y> Fetch<T, Y> fetch(SingularAttribute<? super T, Y> attribute, JoinType jt) {
        return null;
    }

    @Override
    public <Y> Fetch<T, Y> fetch(PluralAttribute<? super T, ?, Y> attribute) {
        return null;
    }

    @Override
    public <Y> Fetch<T, Y> fetch(PluralAttribute<? super T, ?, Y> attribute, JoinType jt) {
        return null;
    }

    @Override
    public Selection<T> alias(String name) {
        return null;
    }

    @Override
    public boolean isCompoundSelection() {
        return false;
    }

    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
        return null;
    }

    @Override
    public Class<? extends T> getJavaType() {
        return null;
    }

    @Override
    public String getAlias() {
        return null;
    }
}
