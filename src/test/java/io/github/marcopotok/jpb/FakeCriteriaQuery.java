package io.github.marcopotok.jpb;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.AbstractQuery;
import jakarta.persistence.criteria.CollectionJoin;
import jakarta.persistence.criteria.CommonAbstractCriteria;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.ListJoin;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.SetJoin;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.EntityType;

class FakeCriteriaQuery implements CriteriaQuery<Object> {

    private String query = "";

    @Override
    public String toString() {
        return query;
    }

    @Override
    public CriteriaQuery<Object> distinct(boolean distinct) {
        if (distinct) {
            query += " distinct";
        }
        return this;
    }

    @Override
    public CriteriaQuery<Object> groupBy(List<Expression<?>> grouping) {
        query += " group by " + grouping.stream().map(Objects::toString).collect(Collectors.joining(","));
        return this;
    }


    @Override
    public CriteriaQuery<Object> multiselect(List<Selection<?>> selectionList) {
        query += " select " + selectionList.stream().map(Objects::toString).collect(Collectors.joining(","));
        return this;
    }


    @Override
    public <U> Subquery<U> subquery(Class<U> type) {
        return new Subquery<U>() {

            private String subQuery = "";

            @Override
            public String toString() {
                return subQuery;
            }

            @Override
            public Subquery<U> select(Expression<U> expression) {
                subQuery += expression;
                return this;
            }

            @Override
            public Subquery<U> where(Expression<Boolean> restriction) {
                return null;
            }

            @Override
            public Subquery<U> where(Predicate... restrictions) {
                return null;
            }

            @Override
            public Subquery<U> groupBy(Expression<?>... grouping) {
                return null;
            }

            @Override
            public Subquery<U> groupBy(List<Expression<?>> grouping) {
                return null;
            }

            @Override
            public Subquery<U> having(Expression<Boolean> restriction) {
                return null;
            }

            @Override
            public Subquery<U> having(Predicate... restrictions) {
                return null;
            }

            @Override
            public Subquery<U> distinct(boolean distinct) {
                return null;
            }

            @Override
            public <Y> Root<Y> correlate(Root<Y> parentRoot) {
                return null;
            }

            @Override
            public <X, Y> Join<X, Y> correlate(Join<X, Y> parentJoin) {
                return null;
            }

            @Override
            public <X, Y> CollectionJoin<X, Y> correlate(CollectionJoin<X, Y> parentCollection) {
                return null;
            }

            @Override
            public <X, Y> SetJoin<X, Y> correlate(SetJoin<X, Y> parentSet) {
                return null;
            }

            @Override
            public <X, Y> ListJoin<X, Y> correlate(ListJoin<X, Y> parentList) {
                return null;
            }

            @Override
            public <X, K, V> MapJoin<X, K, V> correlate(MapJoin<X, K, V> parentMap) {
                return null;
            }

            @Override
            public AbstractQuery<?> getParent() {
                return null;
            }

            @Override
            public CommonAbstractCriteria getContainingQuery() {
                return null;
            }

            @Override
            public Expression<U> getSelection() {
                return null;
            }

            @Override
            public Set<Join<?, ?>> getCorrelatedJoins() {
                return null;
            }

            @Override
            public Class<? extends U> getJavaType() {
                return null;
            }

            @Override
            public String getAlias() {
                return null;
            }

            @Override
            public <X> Root<X> from(Class<X> entityClass) {
                return new FakeRoot<X>(entityClass.getSimpleName());
            }

            @Override
            public <X> Root<X> from(EntityType<X> entity) {
                return null;
            }

            @Override
            public Set<Root<?>> getRoots() {
                return null;
            }

            @Override
            public List<Expression<?>> getGroupList() {
                return null;
            }

            @Override
            public Predicate getGroupRestriction() {
                return null;
            }

            @Override
            public boolean isDistinct() {
                return false;
            }

            @Override
            public Class<U> getResultType() {
                return null;
            }

            @Override
            public <U> Subquery<U> subquery(Class<U> type) {
                return null;
            }

            @Override
            public Predicate getRestriction() {
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
            public <X> Expression<X> as(Class<X> type) {
                return null;
            }

            @Override
            public Selection<U> alias(String name) {
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
        };
    }

    @Override
    public CriteriaQuery<Object> select(Selection<?> selection) {
        return null;
    }

    @Override
    public CriteriaQuery<Object> multiselect(Selection<?>... selections) {
        return null;
    }

    @Override
    public <X> Root<X> from(Class<X> entityClass) {
        return null;
    }

    @Override
    public <X> Root<X> from(EntityType<X> entity) {
        return null;
    }

    @Override
    public CriteriaQuery<Object> where(Expression<Boolean> restriction) {
        return null;
    }

    @Override
    public CriteriaQuery<Object> where(Predicate... predicates) {
        return null;
    }

    @Override
    public CriteriaQuery<Object> groupBy(Expression<?>... grouping) {
        return null;
    }

    @Override
    public CriteriaQuery<Object> having(Expression<Boolean> restriction) {
        return null;
    }

    @Override
    public CriteriaQuery<Object> having(Predicate... restrictions) {
        return null;
    }

    @Override
    public CriteriaQuery<Object> orderBy(Order... o) {
        return null;
    }

    @Override
    public CriteriaQuery<Object> orderBy(List<Order> o) {
        return null;
    }

    @Override
    public Set<Root<?>> getRoots() {
        return null;
    }

    @Override
    public Selection<Object> getSelection() {
        return null;
    }

    @Override
    public List<Expression<?>> getGroupList() {
        return null;
    }

    @Override
    public Predicate getGroupRestriction() {
        return null;
    }

    @Override
    public boolean isDistinct() {
        return false;
    }

    @Override
    public Class<Object> getResultType() {
        return null;
    }

    @Override
    public List<Order> getOrderList() {
        return null;
    }

    @Override
    public Set<ParameterExpression<?>> getParameters() {
        return null;
    }

    @Override
    public Predicate getRestriction() {
        return null;
    }
}
