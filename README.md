appObjects
==========

Yet Another Google App Engine Datastore ORM with a twist!

```
                    _______ __    __            __         
 .---.-.-----.-----|   _   |  |--|__.-----.----|  |_.-----.
 |  _  |  _  |  _  |.  |   |  _  |  |  -__|  __|   _|__ --|
 |___._|   __|   __|.  |   |_____|  |_____|____|____|_____|
       |__|  |__|  |:  1   |    |___|                      
                   |::.. . |                               
                   `-------'     
```
                                                           
appObjects aims to provide a lightweight Object mapping framework. Without adding complexity into the api. 



```java
@Inject
ObjectStore store;

Friend friend = createComplexObject();

Key key = store.put(friend);

Friend saved = store.get(Friend.class, friend.getId()); 

// Find all Friend objects with name staring with "John"
Iterator<Friend> it = store.find(Friend.class).greaterThanOrEqual("name", "John").now();
```
