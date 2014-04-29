appobjects
==========

Yet Another Google App Engine Datastore ORM with a twist!


```java
@Inject
ObjectStore store;

Friend friend = createComplexObject();

Key key = store.put(friend);

Friend saved = store.get(Friend.class, friend.getId()); 

// Find all Students with name staring with "John"
Iterator<Student> it = store.find(Friend.class).greaterThanOrEqual("name", "John").now();
```
