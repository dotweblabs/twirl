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


###Setup

```java
@Inject
ObjectStore store;

Friend friend = createComplexFriend();

Key key = store.put(friend);

Friend saved = store.get(Friend.class, friend.getId()); 

Iterator<Friend> all = store.find(Friend.class).greaterThanOrEqual("name", "John").now();

Friend one = store.findOne(Friend.class).greaterThanOrEqual("name", "John").now();
```


###Updating

####Save

Passing an object to the save(..) method will do the job.

```java
Friend joe = new Friend("Joe", 27);

store.save(joe);
joe.age = 28;
store.save(joe);
```

####Update
```java
store.update(Friend.class).equals("name", "Joe").with("age").increment(1);  
store.update(Friend.class).equals("name", "Joe").with("address").set(new Address(...)); 
store.update(Friend.class).equals("name", "Joe").with(new Friend(..));
```

####Insert
```java
```

####Remove
```java
```

###Object Mapping
```java
```


###Querying
```java
```


Version
-

0.0.1

Tech
-----------

appObjects uses a number of open source projects to work properly:

* [GAE SDK] - SDK for the AppEngine platform (GAE, AppScale or CapeDwarf)

Installation
--------------

```
mvn clean install
```

or add this to your POM:

    <repositories>
	    <repository>
	        <id>appobjects</id>
	        <url>https://raw.github.com/appobjects/appobjects/mvn-repo/</url>
	        <snapshots>
	            <enabled>true</enabled>
	            <updatePolicy>always</updatePolicy>
	        </snapshots>
	    </repository>
    </repositories>

Dependency
--------------

        <dependency>
		  <groupId>org.appobjects</groupId>
		  <artifactId>appobjects</artifactId>
		  <version>0.0.1-SNAPSHOT</version>
		</dependency>

Contribution
--------------

* Anyone is welcome to contribute,  implement feature or fix bugs.

License
-

Apache License, Version 2.0
