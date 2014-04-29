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
