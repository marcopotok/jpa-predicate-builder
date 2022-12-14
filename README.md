# JPA Predicate Builder

![Build](https://github.com/marcopotok/jpa-predicate-builder/actions/workflows/build.yml/badge.svg?branch=main&label=string)
[![Maven Deploy](https://github.com/marcopotok/jpa-predicate-builder/actions/workflows/maven-deploy.yml/badge.svg)](https://github.com/marcopotok/jpa-predicate-builder/actions/workflows/maven-deploy.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.marcopotok/jpa-predicate-builder.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.marcopotok%22%20AND%20a:%22jpa-predicate-builder%22)
[![codecov](https://codecov.io/gh/marcopotok/jpa-predicate-builder/branch/main/graph/badge.svg?token=IOJ9VPAENT)](https://codecov.io/gh/marcopotok/jpa-predicate-builder)

A lightweight layer on top of JPA for easy query construction in Java.


# Project Description

Writing query for java applications faces many challenges, mainly cleanness of code, maintainability and performances. This project aims to address all three of these with a simple builder created on top of JPA APIs.
Other libraries already exists, but usually make the integration difficult and don't always work with other libraries (e.g. Lombok). In this case we wanted a library with no extra dependencies and ready to go.

Key features:

- predicates concatenation with a builder style
- works seamlessly with Spring Data JPA `Specifications`
- implementation agnostic
- remove duplicated joins
- easy fetch of related entities (prefetching)

## Versions and compatibility

The JPA APIs were originally exposed under the package `javax.persistence` replaced in 2019 by the Jakarta's package `jakarta.persistence`. The Predicate Builder supports both versions of JPA APIs with the following versions:
- **1.x.x**: Javax Persistence
- **2.x.x**: Jakarta Persistence



# How to install

Just add the dependency to your pom file.

# How to use

To create your predicate, start with one constructor or factory method available, for example:
```
PredicateBuilder<User> builder = PredicateBuilder.of(User.class);
```
Let's see a more complete example:
```java
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

## Complex example

With REST API it is often necessary to expose multiple optional filters. In this case the Predicate Builder is useful because null (optional) values are handled natively.

Let's assume we have a value object `OrderRequest` with the following optional fields:

- ids: `Collection<Long>`
- type: `String`
- userId: `Long`
- userName: `String`
- fromDate: `Instant`
- toDate: `Instant`

### With Predicate Builder

```java
class OrderService {

    private final EntityManager entityManager;

    public OrderService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Collection<Order> getOrders(OrderRequest request) {
        Session session = entityManager.unwrap(Session.class);
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Order> query = criteriaBuilder.createQuery(Order.class);
        Root<Order> root = query.from(Order.class);

        PredicateBuilder<Order> builder = PredicateBuilder.of(Order.class)
                .prefetch("user.profile")
                .withPropertyIn("id", request.ids)
                .withProperty("type", request.type)
                .withProperty("user.id", request.userId)
                .withPropertyLikeIgnoreCase("user.name", request.userName)
                .withPropertyAfterInclusive("date", request.fromDate)
                .withPropertyBeforeInclusive("date", request.toDate);

        query.where(builder.build(root, query, criteriaBuilder));
        return session.createQuery(query).getResultList();
    }
}
```

### Without Predicate Builder:

```java
class OrderService {

    private final EntityManager entityManager;

    public OrderService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Collection<Order> getOrders(OrderRequest request) {
        Session session = entityManager.unwrap(Session.class);
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Order> query = criteriaBuilder.createQuery(Order.class);
        Root<Order> root = query.from(Order.class);

        List<Predicate> predicates = new LinkedList<>();
        if (request.ids != null) {
            predicates.add(root.get("id").in(request.ids));
        }
        if (request.type != null) {
            predicates.add(criteriaBuilder.equal(root.get("type"), request.type));
        }
        if (request.userId != null || request.userName != null) {
            Join<Object, Object> userJoin = root.join("user", JoinType.LEFT);
            if (request.userId != null) {
                predicates.add((criteriaBuilder.equal(userJoin.get("id"), request.userId)));
            }
            if (request.userName != null) {
                predicates.add((criteriaBuilder.like(criteriaBuilder.upper(userJoin.get("name")),
                        request.userName.toUpperCase(Locale.ROOT))));
            }
        }
        if (request.fromDate != null) {
            predicates.add((criteriaBuilder.greaterThanOrEqualTo(root.get("date"), request.fromDate)));
        }
        if (request.toDate != null) {
            predicates.add((criteriaBuilder.lessThanOrEqualTo(root.get("date"), request.toDate)));
        }
        root.fetch("user", JoinType.LEFT).fetch("profile", JoinType.LEFT);
        criteriaQuery.where(predicates.toArray(Predicate[]::new));

        return session.createQuery(query).getResultList();
    }
}
```

# Limitations and further improvements

Current limitation and possible future improvements:

- Only left joins -> possible auto detection
- Add field checking at build time