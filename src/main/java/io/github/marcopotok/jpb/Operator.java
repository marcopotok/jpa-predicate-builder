package io.github.marcopotok.jpb;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

@FunctionalInterface
interface Operator<U> {

    Predicate toPredicate(U value, Expression<U> path, CriteriaBuilder criteriaBuilder);
}
