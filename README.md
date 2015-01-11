twist
=====

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/textquo/twist?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Yet Another Google App Engine Datastore ORM with a twist! (We need your support, fork it, and try it)

```
  __            __       __   
 |  |_.--.--.--|__.-----|  |_ 
 |   _|  |  |  |  |__ --|   _|
 |____|________|__|_____|____|
 :: Twist :: Object Mapping ::
                                
```
                                                           
twists aims to provide a lightweight Object mapping framework. Without adding complexity into the api.

Persisting POJO's, Maps and (soon Primitive types) directly into the com.textquo.twist.datastore.

[![Show me a Demo at Codio](https://codio-public.s3.amazonaws.com/sharing/demo-in-ide.png)](https://codio.com/kerbymart/twist)

[![Build Status](https://travis-ci.org/textquo/twist.svg?branch=master)](https://travis-ci.org/textquo/twist)

##Sample

Usage sample can be found here: https://github.com/kerbymart/twist-appengine-guestbook-java

##Setup

```java
@Inject
ObjectStore store;

Friend friend = createComplexFriend();

Key key = store.put(friend);

Friend saved = store.get(Friend.class, friend.getId()); 

Iterator<Friend> all = store.find(Friend.class).greaterThanOrEqual("name", "Joe").now();

Friend one = store.findOne(Friend.class).greaterThanOrEqual("name", "Joe").now();
```


##Updating

####Save

Passing an object to the put(..) method will do the job.

```java
Friend joe = new Friend("Joe", 27);

store.put(joe);
joe.age = 28;
store.put(joe);
```

####Update
```java
store.update(Friend.class).equals("name", "Joe").increment("age",1).now();
store.update(Friend.class).equals("name", "Joe").set("address", new Address(...)).now();
store.update(Friend.class).equals("name", "Joe").with(new Friend(..)).now();
```

####Insert
```java
store.put(new Friend(..));
store.put(new Friend(..), new Friend(..));
```

####Remove
```java
store.delete(key);
store.delete(friend.getId());
```

##Object Mapping
```java
@Entity(name="CloseFriends") // Optional name
public class Friend {
    @Id
    private long id; // Can be long, Long or String only
    
    @Parent
    private Circle circle;
    
    @Child
    private Box box;
    
    @Embedded
    private Map map;
    
    private List<String> notes; 
}
```


##Querying
```java
```

###Find & FindOne
```java
Iterator<Friend> all = store.find(Friend.class).greaterThanOrEqual("name", "Joe").now();
Friend one = store.findOne(Friend.class).greaterThanOrEqual("name", "Joe").now();
```

####Projection and Field selection
```java
store.find(Friend.class).projection("firstName", "address").greaterThanOrEqual("name", "Joe").now();
```

####Sorting
```java
store.find(Friend.class).sortAscending("firstName").now();
store.find(Friend.class).sortDescending("lastName").now();
```

####Skip and Limit
```java
store.find(Friend.class).skip(20).now();
store.find(Friend.class).limit(10).now();
```
####Cursors

```java
String cursor = getRequestFromWebQuery();
	
// Should be empty String 
// or valid cursor String only
Cursor startCursor = new Cursor(cursor);
        
ListResult<Post> posts = store.find(Post.class)
       	.limit(10)
        .withCursor(startCursor) 
        .sortAscending("date")
        .asList();
                
Cursor nextCursor = posts.getCursor();
return nextCursor.getWebSafeString();;       
```

##Features
```java
```

####Easy for JSON storage

```java
// Note: Working but needs more work:
String jsonString = "{\"content\" : {\"Sample post\"}";
Map deserialized = deserialize(jsonString);
deserialized.put("__kind__", "Post");
store.put(deserialized);
```

Or using a POJO :

```java
public class JSONEntity {
    @Kind
    private String kind;
    @Id
    private String id;
    @Flat
    private Map<String,Object> fields;
}

JSONEntity entity = new JSONEntity();

entity.setKind("Post");
entity.setId("msgid:456");
entity.getFields().put("content", "Sample post");
entity.getFields().put("uid", 123);

store.put(entity);
```



####Storing Primitive and Basic Java types

Using the keyvalue-extenstion:
```java
KeyValueStore<String,String> messages = new StringKeyValueStore("Messages");
messages.put("uid:123:msgid:456", "Ahoy! We are saving primitives.");
String message = messages.get("uid:123:msgid:456");
```

See: https://github.com/textquo/twist-keyvalue-extension




Version
-

0.0.1

Tech
-----------

twist uses a number of open source projects to work properly:

* [GAE SDK] - SDK for the AppEngine platform (GAE, AppScale or CapeDwarf)
* boon

Installation
--------------

```
mvn clean install
```

or add this to your POM:

        <repositories>
            <!-- Snapshot repository -->
            <repository>
                <id>oss-jfrog-artifactory-snapshots</id>
                <name>oss-jfrog-artifactory-snapshots</name>
                <url>http://oss.jfrog.org/artifactory/oss-snapshot-local</url>
            </repository>
        </repositories>

Dependency
--------------

        <dependency>
		  <groupId>com.textquo</groupId>
		  <artifactId>twist</artifactId>
		  <version>0.0.1-SNAPSHOT</version>
		</dependency>

Contribution
--------------

* Anyone is welcome to contribute,  implement feature or fix bugs.

License
-

Apache License, Version 2.0
