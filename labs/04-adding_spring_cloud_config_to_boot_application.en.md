# Adding Spring Cloud Config to Boot Application

In this lab we'll utilize Spring Boot and Spring Cloud Config to configure our application from a configuration dynamically retrieved from a configuration sever running in PCF.

## Update _Hello_ REST service

These features are added by adding _spring-cloud-services-starter-config-client_ to the classpath.  

* Add the spring-cloud-starter-config dependency.

```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
	<version>2.1.3.RELEASE</version>
    </dependency>
```
* Updated pom file should look as below

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.1.6.RELEASE</version>
		<relativePath /> <!-- lookup parent from repository -->
	</parent>
	<groupId>io.pivotal</groupId>
	<artifactId>sample</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>sample</name>
	<description>Demo project for Spring Boot</description>

	<properties>
		<java.version>1.8</java.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
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

		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>

		<dependency>
			<groupId>org.flywaydb</groupId>
			<artifactId>flyway-core</artifactId>
		</dependency>
		<dependency>
			<groupId>com.zaxxer</groupId>
			<artifactId>HikariCP</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-config</artifactId>
			<version>2.1.3.RELEASE</version>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

</project>

```
* Add an _@Value_ annotation, private field, and update the existing _@GetMapping_ annotated method to employ it in _io.pivotal.sample.controller.GreetingController_ (**sample/src/main/java/io/pivotal/sample/controller/GreetingController.java**):

```java
    @Value("${greeting:Hello}")
    private String greeting;

    @GetMapping("/hello")
    public String hello() {
        return String.join(" ", greeting, "World!");
    }
```

* Add a [@RefreshScope](https://cloud.spring.io/spring-cloud-static/spring-cloud-commons/2.1.0.RELEASE/single/spring-cloud-commons.html#refresh-scope) annotation to the top of the _GreetingController_ class declaration

```java
@RefreshScope
@RestController
public class GreetingController {
```

Completed:

```java
package io.pivotal.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@RestController
public class GreetingController {

    @Value("${greeting:Hello}")
    private String greeting;

    @GetMapping("/hello")
    public String hello() {
        return String.join(" ", greeting, "World!");
    }

}
```

* Run the application.

```bash
mvn clean spring-boot:run
```

When you access `http://localhost:8080/hello`, `Hello World` would be displayed.


## Run the _Config Server_ locally


* Open a new Terminal session and nagivate to an example Config Server included in the Labs git repo.

```bash
cd c:\Users\{your-username}\Downloads\spring\projects\spring-boot-workshop\code-samples\spring-config-server\config-server
```

* Run the _Config Server_

```bash
mvn spring-boot:run
```

## Run the _sample_ Application and verify dynamic config is working

* Create a file named `bootstrap.properties` in the *sample/src/main/resources* folder.

* Specify the name of our application in the `bootstrap.properties` file.

```bash
spring.application.name=sample
```

* Run the application using the `local` profile.

```bash
mvn clean spring-boot:run -Dspring.profiles.active=local
```

* Browse to `http://localhost:8080/hello` and verify you now see your new greeting, "Hola World!".

* Stop the _sample_ application

## Deploy *Config Server* to Cloud Foundry

Since Spring Cloud Service (SCS) is not installed on Cloud Foundry, we are going to manually deploy a standalone *Config Server* application to Cloud Foundry.

* In the CMD session for *Config Server*, build the *Config Server* application and push into Cloud Foundry.

```bash
mvn clean package
```
* Push *Config Server* application into Cloud Foundry

```bash
cf push
```

You can verify *Config Server* is properly working by navigating to the following URL in your browser.

```
http://config-server.apps.20.41.62.88.cf.pcfazure.com/sample/cloud
```

You should see a response similar to the following.

```json
{
  "name": "sample",
  "profiles": [
    "cloud"
  ],
  "label": null,
  "version": null,
  "state": null,
  "propertySources": [
    {
      "name": "classpath:/sample-cloud.yml",
      "source": {
        "greeting": "Cloudy"
      }
    }
  ]
}
```

## Associate the *Config Server* on Cloud Foundry

* We will now associate our application to the config-server in the `manifest.yml` file. Add these entries to the bottom of **sample/manifest.yml**

```yaml
  env:
    SPRING_PROFILES_ACTIVE: cloud
    SPRING_CLOUD_CONFIG_URI: https://config-server.{your-domain}
```
Completed:

```yaml
--- 
applications:
- name: sample-{your-initial}
  instances: 1
  path: .\target\sample-0.0.1-SNAPSHOT.jar
  timeout: 180 # to give time for the data to import
  env:
    SPRING_PROFILES_ACTIVE: cloud
    SPRING_CLOUD_CONFIG_URI: https://config-server.{your-domain}
```

Two environments variables, `SPRING_PROFILES_ACTIVE` and `SPRING_CLOUD_CONFIG_URI` will get injected into the container in which the application will run. When the application starts, they will be loaded and be used to set active profile as well as URI to the config server.


## Deploy and test application

* In the CMD session for *sample* app, build the application

```bash
mvn clean package
```
. Push application into Cloud Foundry

```bash
cf push
```

* Test your application by navigating to the **/hello** endpoint of the application.  You should now see a greeting that is read from the Cloud Config Server!

```
Cloudy World!

```
**Congratulations!** You’ve just learned how to leverage Spring Cloud Config Server in your application.
