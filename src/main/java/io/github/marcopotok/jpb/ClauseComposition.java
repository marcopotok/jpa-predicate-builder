package io.github.marcopotok.jpb;

import java.io.Serializable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

class ClauseComposition {

    interface Combiner extends Serializable {
        Predicate combine(CriteriaBuilder builder, Predicate lhs, Predicate rhs);
    }

    static Clause composed(Clause lhs, Clause rhs, Combiner combiner) {
        return (builder, provider) -> {
            Predicate thisPredicate = toPredicate(lhs, builder, provider);
            Predicate otherPredicate = toPredicate(rhs, builder, provider);
            if (thisPredicate == null) {
                return otherPredicate;
            }
            return otherPredicate == null ? thisPredicate : combiner.combine(builder, thisPredicate, otherPredicate);
        };
    }

    private static Predicate toPredicate(Clause clause, CriteriaBuilder criteriaBuilder, PathProvider pathProvider) {
        return clause == null ? null : clause.toPredicate(criteriaBuilder, pathProvider);
    }
}