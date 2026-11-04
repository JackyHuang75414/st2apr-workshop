### Q 1.7 — What does the IDE do before and while starting the application?
Before launching, the IDE delegates the build to the build tool: it resolves the
dependencies declared in pom.xml (from the local cache, downloading the missing ones),
compiles src/main/java into target/classes and processes src/main/resources. It then starts
a JVM with the project JDK, the compiled classes and the dependency jars on the classpath,
and calls the main method of the @SpringBootApplication class. While starting,
SpringApplication bootstraps the context: it creates the ApplicationContext, runs component
scanning and auto-configuration, instantiates the beans and starts the embedded Tomcat. The
IDE only streams the console output and keeps a handle on the process to stop or rerun it.

### Q 1.8 — What does "Tomcat started on port 8080 (http) with context path '/'" mean?
The embedded servlet container runs inside the same JVM as the application and listens on
TCP port 8080 for HTTP. The context path '/' means the application is deployed at the root,
so an endpoint is reached at http://localhost:8080/<path> with no prefix. Nobody installed
Tomcat: it is a set of jars (tomcat-embed-core, ...) pulled transitively by
spring-boot-starter-webmvc, and Spring Boot's auto-configuration creates and starts it
programmatically. That is why there is no Tomcat service, no CATALINA_HOME and no webapps
directory.

### Q 1.10 — Role of @SpringBootApplication and SpringApplication.run(...)?
@SpringBootApplication combines three annotations: @SpringBootConfiguration (the class is a
configuration class), @EnableAutoConfiguration (Spring Boot configures beans automatically
from the classpath and the properties, e.g. the embedded Tomcat and the DispatcherServlet
because Spring MVC is present) and @ComponentScan (scans the package of the annotated class
and its sub-packages for @Component, @Service, @Repository, @Controller — this is why every
class must live under st2apr.workshop). SpringApplication.run(...) bootstraps the
application: it creates and refreshes the ApplicationContext, which triggers component
scanning, auto-configuration and bean instantiation, starts the embedded server, runs the
CommandLineRunner / ApplicationRunner beans and returns the context.

### Q 1.11 — Why must the application be restarted after each code change?
A JVM loads a class once, the first time it is used, and keeps its bytecode in memory; it
never re-reads the .class file. After editing a .java file nothing is recompiled unless the
build runs again, and the running JVM still holds the old class. Moreover the Spring context
is built once at startup: singleton beans are instantiated and handler mappings registered
then, so a newly added @GetMapping would not be known. Hence the full cycle: compile, new
JVM, new context. DevTools automates it with a fast restart, but structural changes still
need a restart.