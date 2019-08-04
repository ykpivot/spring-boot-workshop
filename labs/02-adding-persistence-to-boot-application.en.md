
# Adding Persistence to Boot Application

In this lab we'll utilize Spring Boot, Spring Data, and Spring Data REST to create a fully-functional hypermedia-driven RESTful web service.  We'll then deploy it to Pivotal Cloud Foundry.  Along the way we'll take a brief look at [Flyway](https://flywaydb.org) which can help us manage updates to database schema and data.

## Create a Hypermedia-Driven RESTful Web Service with Spring Data REST (using JPA)

This application will allow us to create, read update and delete records in an [in-memory](http://www.h2database.com/html/quickstart.html) relational repository. We'll continue building upon the Spring Boot application we built out in Lab 1.  The first stereotype we will need is the domain model itself, which is `City`.

## Use Spring Data and Spring Data REST

* Edit **pom.xml** and add the following dependencies within the `dependencies`

```xml
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-rest</artifactId>
		</dependency>
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.data</groupId>
			<artifactId>spring-data-rest-hal-browser</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-hateoas</artifactId>
		</dependency>
```

## Add the domain object - City

* Create the package `io.pivotal.sample.domain` and in that package create the class `City`. Into that file you can paste the following source code, which represents cities based on postal codes, global coordinates, etc:

```java
package io.pivotal.sample.domain;

@Data
@Entity
@Table(name="city")
public class City implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String county;

    @Column(nullable = false)
    private String stateCode;

    @Column(nullable = false)
    private String postalCode;

    @Column
    private String latitude;

    @Column
    private String longitude;

}
```

Notice that we're using [JPA](http://docs.oracle.com/javaee/6/tutorial/doc/bnbpz.html) annotations on the class and its fields. We're also employing [Lombok](https://projectlombok.org/features/all), so we don't have to write a bunch of boilerplate code (e.g., getter and setter methods).  You'll need to use your IDE's features to add the appropriate import statements.

* Edit **pom.xml** and add the following dependencies within the `dependencies`

```xml
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
```

-> Hint: imports should start with `javax.persistence` and `lombok`

* Create the package `io.pivotal.sample.repositories` and in that package create the interface `CityRepository`. Paste the following code and add appropriate imports:

```java
package io.pivotal.sample.repositories;

@RepositoryRestResource(collectionResourceRel = "cities", path = "cities")
public interface CityRepository extends PagingAndSortingRepository<City, Long> {
}
```

You’ll need to use your IDE’s features to add the appropriate import statements.

-> Hint: imports should start with `org.springframework.data.rest.core.annotation` and `org.springframework.data.repository`

## Use Flyway to manage schema

* Edit **pom.xml** and add the following dependencies within the `dependencies`

```xml
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
        </dependency>
```

* Create a new file named `V1_0__init_database.sql` underneath **sample/src/main/resources/db/migration**, add the following lines and save.

```sql
CREATE TABLE city (
   ID INTEGER PRIMARY KEY AUTO_INCREMENT,
   NAME VARCHAR(100) NOT NULL,
   COUNTY VARCHAR(100) NOT NULL,
   STATE_CODE VARCHAR(10) NOT NULL,
   POSTAL_CODE VARCHAR(10) NOT NULL,
   LATITUDE VARCHAR(15) NOT NULL,
   LONGITUDE VARCHAR(15) NOT NULL
);
```

Spring Boot comes with out-of-the-box [integration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto-database-initialization.html#howto-execute-flyway-database-migrations-on-startup) support for [Flyway](https://flywaydb.org/documentation/plugins/springboot).  When we start the application it will execute a versioned [SQL migration](https://flywaydb.org/documentation/migrations#sql-based-migrations) that will create a new table in the database.

* Add the following lines to **sample/src/main/resources/application.properties**

```yml
spring.datasource.hikari.connection-timeout=60000
spring.datasource.hikari.maximum-pool-size=5
```
[Hikari](https://github.com/brettwooldridge/HikariCP/blob/dev/README.md) is a database connection pool implementation. We are limiting the number of database connections an individual application instance may consume.

## Run the _sample_ Application

* Return to the Terminal session you opened previously

* Run the application

```bash
mvnw clean spring-boot:run
```
* Access the application using `curl` or your web browser using the newly added REST repository endpoint at http://localhost:8080/cities. You'll see that the primary endpoint automatically exposes the ability to page, size, and sort the response JSON.

```bash
{
  "_embedded" : {
    "cities" : [ ]
  },
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/cities{?page,size,sort}",
      "templated" : true
    },
    "profile" : {
      "href" : "http://localhost:8080/profile/cities"
    }
  },
  "page" : {
    "size" : 20,
    "totalElements" : 0,
    "totalPages" : 0,
    "number" : 0
  }
}
```

* To exit the application, type *Ctrl-C*.

So what have you done? Created four small classes, modified a build file, added some configuration and SQL migration scripts, resulting in a fully-functional REST microservice. The application's `DataSource` is created automatically by Spring Boot using the in-memory database because no other +DataSource+ was detected in the project.

Next we'll import some data.

## Importing Data

* Copy the [import.sql](https://raw.githubusercontent.com/Pivotal-Field-Engineering/devops-workshop/master/labs/import.sql) file found in *spring-boot-workshop/labs* to **sample/src/main/resources/db/migration**.
* Rename the file to be `V1_1__seed_data.sql`. (This is a small subset of a larger dataset containing all of the postal codes in the United States and its territories).

* Restart the application.

```bash
mvnw clean spring-boot:run
```

* Access the application again. Notice the appropriate hypermedia is included for *next*, *previous*, and *self*. You can also select pages and page size by utilizing `?size=n&page=n` on the URL string. Finally, you can sort the data utilizing `?sort=fieldName` (replace fieldName with a cities attribute).

```bash
{
  "_embedded" : {
    "cities" : [ {
      "name" : "HOLTSVILLE",
      "county" : "SUFFOLK",
      "stateCode" : "NY",
      "postalCode" : "00501",
      "latitude" : "+40.922326",
      "longitude" : "-072.637078",
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/cities/1"
        },
        "city" : {
          "href" : "http://localhost:8080/cities/1"
        }
      }
    },
    
    // ...
    
    {
      "name" : "CASTANER",
      "county" : "LARES",
      "stateCode" : "PR",
      "postalCode" : "00631",
      "latitude" : "+18.269187",
      "longitude" : "-066.864993",
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/cities/20"
        },
        "city" : {
          "href" : "http://localhost:8080/cities/20"
        }
      }
    } ]
  },
  "_links" : {
    "first" : {
      "href" : "http://localhost:8080/cities?page=0&size=20"
    },
    "self" : {
      "href" : "http://localhost:8080/cities{?page,size,sort}",
      "templated" : true
    },
    "next" : {
      "href" : "http://localhost:8080/cities?page=1&size=20"
    },
    "last" : {
      "href" : "http://localhost:8080/cities?page=2137&size=20"
    },
    "profile" : {
      "href" : "http://localhost:8080/profile/cities"
    }
  },
  "page" : {
    "size" : 20,
    "totalElements" : 42741,
    "totalPages" : 2138,
    "number" : 0
  }
}
```

* Try the following URL Paths in your browser to see how the application behaves:

`http://localhost:8080/cities?size=5`

`http://localhost:8080/cities?size=5&page=3`

`http://localhost:8080/cities?sort=postalCode,desc`

Next we'll add searching capabilities.

## Adding Search

* Let's add some additional finder methods to *CityRepository*:

```java
@RestResource(path = "name", rel = "name")
Page<City> findByNameIgnoreCase(@Param("q") String name, Pageable pageable);

@RestResource(path = "nameContains", rel = "nameContains")
Page<City> findByNameContainsIgnoreCase(@Param("q") String name, Pageable pageable);

@RestResource(path = "state", rel = "state")
Page<City> findByStateCodeIgnoreCase(@Param("q") String stateCode, Pageable pageable);

@RestResource(path = "postalCode", rel = "postalCode")
Page<City> findByPostalCode(@Param("q") String postalCode, Pageable pageable);

@Query(value ="select c from City c where c.stateCode = :stateCode")
Page<City> findByStateCode(@Param("stateCode") String stateCode, Pageable pageable);
```

-> Hint: imports should start with `org.springframework.data.domain`, `org.springframework.data.rest.core.annotation`, `org.springframework.data.repository.query`, and `org.springframework.data.jpa.repository`

* Run the application

```bash
mvnw clean spring-boot:run
```

* Access the application again. Notice that hypermedia for a new *search* endpoint has appeared.

```bash

// prior omitted
  "_links" : {
    "first" : {
      "href" : "http://localhost:8080/cities?page=0&size=20"
    },
    "self" : {
      "href" : "http://localhost:8080/cities{?page,size,sort}",
      "templated" : true
    },
    "next" : {
      "href" : "http://localhost:8080/cities?page=1&size=20"
    },
    "last" : {
      "href" : "http://localhost:8080/cities?page=2137&size=20"
    },
    "profile" : {
      "href" : "http://localhost:8080/profile/cities"
    },
    "search" : {
      "href" : "http://localhost:8080/cities/search"
    }
  },
  "page" : {
    "size" : 20,
    "totalElements" : 42741,
    "totalPages" : 2138,
    "number" : 0
  }
}
```

* Access the new **search** endpoint:
`http://localhost:8080/cities/search`

```bash
{
  "_links" : {
    "postalCode" : {
      "href" : "http://localhost:8080/cities/search/postalCode{?q,page,size,sort}",
      "templated" : true
    },
    "name" : {
      "href" : "http://localhost:8080/cities/search/name{?q,page,size,sort}",
      "templated" : true
    },
    "state" : {
      "href" : "http://localhost:8080/cities/search/state{?q,page,size,sort}",
      "templated" : true
    },
    "nameContains" : {
      "href" : "http://localhost:8080/cities/search/nameContains{?q,page,size,sort}",
      "templated" : true
    },
    "findByStateCode" : {
      "href" : "http://localhost:8080/cities/search/findByStateCode{?stateCode,page,size,sort}",
      "templated" : true
    },
    "self" : {
      "href" : "http://localhost:8080/cities/search"
    }
  }
}
```

Note that we now have new search endpoints for each of the finders that we added.

* Try a few of these endpoints in [Postman](https://www.getpostman.com). Feel free to substitute your own values for the parameters.

`http://localhost:8080/cities/search/postalCode?q=01229`

`http://localhost:8080/cities/search/name?q=Springfield`

`http://localhost:8080/cities/search/nameContains?q=West&size=1`

-> For further details on what's possible with Spring Data JPA, consult the [reference documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#dependencies.spring-boot)

## Pushing to Cloud Foundry

* Build the application

```
mvnw package
```
* You should already have an application manifest, **manifest.yml**, created in Lab 1; this can be reused.  You'll want to add a timeout param so that our service has enough time to initialize with its data loading:

```yaml
applications:
- name: sample-{your-initial}
  instances: 1
  path: .\target\sample-0.0.1-SNAPSHOT.jar
  timeout: 180 # to give time for the data to import
```

* Push to Cloud Foundry:

```bash
cf push
...

Showing health and status for app cloud-native-spring in org sample-labs / space development as someone@pivotal.io...
OK

requested state: started
instances: 1/1
usage: 1G x 1 instances
urls: sample-yk.cfapps.io
last uploaded: Thu Jul 28 23:29:21 UTC 2018
stack: cflinuxfs2
buildpack: java_buildpack_offline

     state     since                    cpu      memory         disk         details
#0   running   2018-07-28 04:30:22 PM   163.7%   395.7M of 1G   159M of 1G
```

* Access the application at the random route provided by CF:

`http GET https://sample-{your-initial}-{your-pcf-domain}.com/cities`

**Congratulations!** You’ve just learned how to utilie Spring Data REST in your Spring Boot application.
