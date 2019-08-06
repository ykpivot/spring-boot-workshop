# Cloudfoundry Task Demo

This demonstrates how to run a Spring Cloudd Task-based application on Cloud Foundry [as a task](https://docs.cloudfoundry.org/devguide/using-tasks.html).

Build the application.

```bash
mvn clean package
```

Deploy the Spring Cloud Task-based application to the platform. This is a Spring Cloud Task-based application. It has no web endpoint. The platform's health check will try to ascertain the health of the application by checking whether it's responding to an HTTP request. We could also test that the application is bound to a non-HTTP port. Neither apply here, though. This task will start and then stop. So, when you deploy it make sure that there is no health-check specified.


```bash
cf push --health-check-type none -p target/ingest-1.0.0.BUILD-SNAPSHOT.jar file-ingest
```

Runs the task using the Task runner. This support for tasks is built into the platform.

```bash
cf run-task file-ingest ".java-buildpack/open_jdk_jre/bin/java org.springframework.boot.loader.JarLauncher" --name my-task
```

Check the logs

```bash
cf logs file-ingest --recent 
```