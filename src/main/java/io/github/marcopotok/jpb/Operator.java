package io.github.marcopotok.jpb;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

@FunctionalInterface
interface Operator<U> {

    Predicate toPredicate(U value, Expression<U> path, CriteriaBuilder criteriaBuilder);
}
