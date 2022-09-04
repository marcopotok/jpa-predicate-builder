# JPA Predicate Builder

A lightweight layer on top of Javax Persistence for easy query construction in Java.

# Project Description

Writing query for java applications faces many challenges, mainly cleanness of code, maintainability and performances. This project aims to address all three of these with a simple builder created on top of JPA APIs.
Other libraries already exists, but usually make the integration difficult and don't always work with other libraries (e.g. Lombok). In this case we wanted a library with no extra dependencies and ready to go.

Key features:

- predicates concatenation with a builder style
- works seamlessly with jpa specifications
- implementation agnostic
- remove duplicated joins
- easy fetch of related entities (prefetching)

# How to install

To install it is enough to add the dependency to your pom file.

# How to use

To create your predicate, start with one constructor or factory method available:
```java
import java.util.Collection;

import io.github.marcopotok.jpb.PredicateBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;

class UserService {

    private final EntityManager entityManager;

    public UserService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Collection<User> getUserWithName(String name) {
        Session session = entityManager.unwrap(Session.class);
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        query.where(PredicateBuilder.of(User.class).withProperty("name", name).build(root, query, criteriaBuilder));
        return session.createQuery(query).getResultList();
    }
}
```
## Specification (Spring Data JPA)
Another way to get the same result with Specifications:
```java
import java.util.Collection;

class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private Collection<User> getUserWithName(String name) {
        Specification<User> specification = PredicateBuilder.of(User.class).withProperty("name", name)::build;
        return userRepository.findAll(specification);
    }
}
```
## Joins
In order to filter by an attribute of a relation, use the dot notation. For example, if you want to find all the orders of a user, you can write:
```java
import java.util.Collection;

class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    private Collection<Order> getOrdersOfUser(String userId) {
        Specification<Order> specification = PredicateBuilder.of(Order.class).withProperty("user.id", userId)::build;
        return orderRepository.findAll(specification);
    }
}
```
## Prefetch
To avoid multiple queries with a lazy relationship with another entity, you can use the prefetch method.
```java
import java.util.Collection;

import io.github.marcopotok.jpb.PredicateBuilder;

class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    private Collection<Order> getOrdersOfUser(String userId) {
        Specification<Order> specification = PredicateBuilder.of(Order.class)
                .prefetch("user.[nested.deep,other.deep]")
                .withProperty("user.id", userId)::build;
        return orderRepository.findAll(specification);
    }
}
```
In this case the engine will perform a fetch on _user_, _user.nested_, _user.nested.deep_, _user.other_ and _user.other.deep_. Note that the fetch with the _user_ entity is **not** duplicated.
# Limitations and further improvements

Current limitation and possible future improvements:
- Only left joins -> possible auto detection
- Add field checking at build time