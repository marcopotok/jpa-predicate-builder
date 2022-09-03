package io.github.marcopotok.jpb;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

class FakeJoin<X, Y> implements Join<X, Y> {

    int joinCounter = 0;
    private final String attribute;

    FakeJoin(String attribute) {
        this.attribute = attribute;
    }

    @Override
    public String toString() {
        return attribute;
    }

    @Override
    public <X1, Y1> Join<X1, Y1> join(String attributeName) {
        joinCounter++;
        return new FakeJoin<>(this + " join " + attributeName);
    }

    @Override
    public <X1, Y1> Join<X1, Y1> join(String attributeName, JoinType jt) {
        return join(attributeName);
    }

    @Override
    public <Y1> Path<Y1> get(String attributeName) {
        return new FakeRoot<>(this.attribute + " " + attributeName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> Expression<U> as(Class<U> type) {
        return (Expression<U>) this;
    }

    @Override
    public Join<X, Y> on(Expression<Boolean> restriction) {
        return new FakeJoin<>(this.attribute + " on [" + restriction + "]");
    }

    @Override
    public Join<X, Y> on(Predicate... restrictions) {
        return new FakeJoin<>(this.attribute + " on " + Arrays.toString(restrictions));
    }

    @Override
    public Predicate getOn() {
        return null;
    }

    @Override
    public Attribute<? super X, ?> getAttribute() {
        return null;
    }

    @Override
    public From<?, X> getParent() {
        return null;
    }

    @Override
    public JoinType getJoinType() {
        return null;
    }

    @Override
    public Set<Join<Y, ?>> getJoins() {
        return null;
    }

    @Override
    public boolean isCorrelated() {
        return false;
    }

    @Override
    public From<X, Y> getCorrelationParent() {
        return null;
    }

    @Override
    public <Y1> Join<Y, Y1> join(SingularAttribute<? super Y, Y1> attribute) {
        return null;
    }

    @Override
    public <Y1> Join<Y, Y1> join(SingularAttribute<? super Y, Y1> attribute, JoinType jt) {
        return null;
    }

    @Override
    public <Y1> CollectionJoin<Y, Y1> join(CollectionAttribute<? super Y, Y1> collection) {
        return null;
    }

    @Override
    public <Y1> SetJoin<Y, Y1> join(SetAttribute<? super Y, Y1> set) {
        return null;
    }

    @Override
    public <Y1> ListJoin<Y, Y1> join(ListAttribute<? super Y, Y1> list) {
        return null;
    }

    @Override
    public <K, V> MapJoin<Y, K, V> join(MapAttribute<? super Y, K, V> map) {
        return null;
    }

    @Override
    public <Y1> CollectionJoin<Y, Y1> join(CollectionAttribute<? super Y, Y1> collection, JoinType jt) {
        return null;
    }

    @Override
    public <Y1> SetJoin<Y, Y1> join(SetAttribute<? super Y, Y1> set, JoinType jt) {
        return null;
    }

    @Override
    public <Y1> ListJoin<Y, Y1> join(ListAttribute<? super Y, Y1> list, JoinType jt) {
        return null;
    }

    @Override
    public <K, V> MapJoin<Y, K, V> join(MapAttribute<? super Y, K, V> map, JoinType jt) {
        return null;
    }

    @Override
    public <X1, Y1> CollectionJoin<X1, Y1> joinCollection(String attributeName) {
        return null;
    }

    @Override
    public <X1, Y1> SetJoin<X1, Y1> joinSet(String attributeName) {
        return null;
    }

    @Override
    public <X1, Y1> ListJoin<X1, Y1> joinList(String attributeName) {
        return null;
    }

    @Override
    public <X1, K, V> MapJoin<X1, K, V> joinMap(String attributeName) {
        return null;
    }

    @Override
    public <X1, Y1> CollectionJoin<X1, Y1> joinCollection(String attributeName, JoinType jt) {
        return null;
    }

    @Override
    public <X1, Y1> SetJoin<X1, Y1> joinSet(String attributeName, JoinType jt) {
        return null;
    }

    @Override
    public <X1, Y1> ListJoin<X1, Y1> joinList(String attributeName, JoinType jt) {
        return null;
    }

    @Override
    public <X1, K, V> MapJoin<X1, K, V> joinMap(String attributeName, JoinType jt) {
        return null;
    }

    @Override
    public Set<Fetch<Y, ?>> getFetches() {
        return null;
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

    @Override
    public <X1, Y1> Fetch<X1, Y1> fetch(String attributeName) {
        return null;
    }

    @Override
    public <X1, Y1> Fetch<X1, Y1> fetch(String attributeName, JoinType jt) {
        return null;
    }

    @Override
    public Bindable<Y> getModel() {
        return null;
    }

    @Override
    public Path<?> getParentPath() {
        return null;
    }

    @Override
    public <Y1> Path<Y1> get(SingularAttribute<? super Y, Y1> attribute) {
        return null;
    }

    @Override
    public <E, C extends Collection<E>> Expression<C> get(PluralAttribute<Y, C, E> collection) {
        return null;
    }

    @Override
    public <K, V, M extends Map<K, V>> Expression<M> get(MapAttribute<Y, K, V> map) {
        return null;
    }

    @Override
    public Expression<Class<? extends Y>> type() {
        return null;
    }

    @Override
    public Predicate isNull() {
        return null;
    }

    @Override
    public Predicate isNotNull() {
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
    public Predicate in(Collection<?> values) {
        return null;
    }

    @Override
    public Predicate in(Expression<Collection<?>> values) {
        return null;
    }

    @Override
    public Selection<Y> alias(String name) {
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
    public Class<? extends Y> getJavaType() {
        return null;
    }

    @Override
    public String getAlias() {
        return null;
    }
}
