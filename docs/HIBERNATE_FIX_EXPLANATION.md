# Fix: Hibernate MultipleBagFetchException

## The Error

```
org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags: 
[com.deryncullen.resume.model.Profile.certifications, com.deryncullen.resume.model.Profile.educations]
```

## What Caused It

In your `ProfileRepository.java`, you tried to fetch 4 collections in a single query:

```java
@Query("""
    SELECT DISTINCT p FROM Profile p
    LEFT JOIN FETCH p.experiences
    LEFT JOIN FETCH p.educations      // ← Can't do this
    LEFT JOIN FETCH p.skills           // ← Multiple bags
    LEFT JOIN FETCH p.certifications   // ← Not allowed
    WHERE p.id = :id
""")
```

**Why it fails:** Hibernate can't efficiently fetch multiple `@OneToMany` collections (called "bags") in a single SQL query because it would create a Cartesian product.

## The Solution: Use @EntityGraph

Instead of multiple `JOIN FETCH`, use Spring Data JPA's `@EntityGraph`:

```java
@EntityGraph(attributePaths = {"experiences", "educations", "skills", "certifications"})
@Query("SELECT p FROM Profile p WHERE p.id = :id")
Optional<Profile> findByIdWithAllRelations(@Param("id") Long id);
```

### Why @EntityGraph Works

`@EntityGraph` tells Spring Data JPA which associations to fetch, and it handles the multiple queries internally to avoid the MultipleBagFetchException. It essentially:
1. Fetches the Profile in one query
2. Fetches each collection in separate queries (behind the scenes)
3. Assembles them into one result

This is more efficient than doing N+1 queries manually.

## What Changed

**Updated in ProfileRepository.java:**
- `findByIdWithAllRelations()` - Now uses `@EntityGraph` instead of multiple `JOIN FETCH`
- `findAllActiveWithRelations()` - Also uses `@EntityGraph` for consistency

## Files to Update

**Replace:**
```
src/main/java/com/deryncullen/resume/repository/ProfileRepository.java
```

**With:**
```
ProfileRepository.java (in outputs folder)
```

## Run the Tests

```bash
./gradlew gaugeTest
```

## Expected Result

```
Total Scenarios: 9
✅ Passed: 9
❌ Failed: 0
Success Rate: 100%
```

The "Profile with Complete Resume Data" test should now pass!

## Alternative Solutions (Not Recommended)

If you wanted to keep multiple `JOIN FETCH`, you could:

1. **Change Collections to Sets** - But this changes your domain model
   ```java
   private Set<Experience> experiences;  // Instead of List
   ```

2. **Use Multiple Queries** - Manually fetch each collection
   ```java
   Profile profile = findById(id);
   findByIdWithExperiences(id);
   findByIdWithEducations(id);
   // etc...
   ```

3. **Use @Fetch(FetchMode.SUBSELECT)** - But requires entity changes

**Best solution:** Use `@EntityGraph` as shown above! It's clean, efficient, and doesn't require entity model changes.

## Learn More

- [Hibernate MultipleBagFetchException](https://vladmihalcea.com/hibernate-multiplebagfetchexception/)
- [Spring Data JPA Entity Graphs](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.entity-graph)
