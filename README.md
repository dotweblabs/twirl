appObjects
==========

Yet Another Google App Engine Datastore ORM with a twist!


```java

@Inject
ObjectStore store;

Friend friend = createComplexObject();

Key key = store.put(friend);

Friend saved = store.get(Friend.class, friend.getId()); 

// Find all Friend objects with name staring with "John"
Iterator<Friend> it = store.find(Friend.class).greaterThanOrEqual("name", "John").now();




```
