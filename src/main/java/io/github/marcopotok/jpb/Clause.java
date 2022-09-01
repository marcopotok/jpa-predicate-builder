package io.github.marcopotok.jpb;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;

@FunctionalInterface
public interface Clause {

    Clause CONJUNCTION = (criteriaBuilder, pathProvider) -> criteriaBuilder.conjunction();
    Clause DISJUNCTION = (criteriaBuilder, pathProvider) -> criteriaBuilder.disjunction();

    /**
     * Converts the clause to a {@link Predicate}
     * @param criteriaBuilder
     * @param pathProvider
     * @return the predicate
     */
    Predicate toPredicate(CriteriaBuilder criteriaBuilder, PathProvider pathProvider);

    /**
     * Concatenates two clauses with a logic AND
     * @param clause
     * @return the result clause
     */
    default Clause and(Clause clause) {
        return ClauseComposition.composed(this, clause, CriteriaBuilder::and);
    }

    /**
     * Concatenates two clauses with a logic OR
     * @return the result clause
     */
    default Clause or(Clause clause) {
        return ClauseComposition.composed(this, clause, CriteriaBuilder::or);
    }
}
