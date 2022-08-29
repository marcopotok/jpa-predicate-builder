package io.github.marcopotok.jpb;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;

@FunctionalInterface
public interface Clause {

    Clause CONJUNCTION = (criteriaBuilder, pathProvider) -> criteriaBuilder.conjunction();
    Clause DISJUNCTION = (criteriaBuilder, pathProvider) -> criteriaBuilder.disjunction();

    Predicate toPredicate(CriteriaBuilder criteriaBuilder, PathProvider pathProvider);

    default Clause and(Clause clause) {
        return ClauseComposition.composed(this, clause, CriteriaBuilder::and);
    }

    default Clause or(Clause clause) {
        return ClauseComposition.composed(this, clause, CriteriaBuilder::or);
    }
}
