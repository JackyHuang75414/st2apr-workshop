# ST2APR — Spring Boot workshop — Report

## Part 1 — Create and run your project

### Q 1.7 — What does the IDE do before and while starting the application?
The IDE delegates the build to Maven: it resolves the dependencies, compiles `src/main/java` into `target/classes`, then starts a JVM with those classes and the dependency jars on the classpath and calls `main()`. Spring Boot then creates the context, runs auto-configuration and starts Tomcat; the IDE only streams the console and keeps a handle on the process.

### Q 1.8 — What does "Tomcat started on port 8080 (http) with context path '/'" mean? Who installed Tomcat?
An embedded Tomcat runs inside the same JVM, listens on port 8080, and serves the application at the root path `/` (so `/programmers`, no prefix). Nobody installed it: `spring-boot-starter-webmvc` pulls the `tomcat-embed-*` jars and Spring Boot starts the server programmatically — no Tomcat service, no `CATALINA_HOME`.

### Q 1.10 — What is the role of @SpringBootApplication and of SpringApplication.run(...)?
`@SpringBootApplication` combines `@SpringBootConfiguration` (a configuration class), `@EnableAutoConfiguration` (configures beans from the classpath and the properties, e.g. Tomcat and the DispatcherServlet) and `@ComponentScan` (scans `st2apr.workshop` and its sub-packages).
`SpringApplication.run(...)` bootstraps the app: it creates and refreshes the ApplicationContext, which instantiates the beans and starts the server, then returns the context.

### Q 1.11 — After each code change you must restart the application, otherwise the change is not visible. Why?
The JVM loads a class once and never re-reads the `.class` file, so new bytecode requires a new JVM. The Spring context is also built once at startup (singletons instantiated, routes registered), so a new `@GetMapping` would not even be known. Hence: compile → new JVM → new context.

## Part 2 — Your first endpoint

### Q 2.3 — Which annotations do you need on the class and on the method?
`@RestController` on the class: the returned value is written to the HTTP response body, as JSON via Jackson. `@GetMapping("/programmers")` on the method binds `GET /programmers` to it.
Under the hood: at startup `RequestMappingHandlerMapping` registers the route; at each request `DispatcherServlet` looks it up and invokes the method.

### Q 2.4 — Why is putting the code in the main method a bad idea?
`main()` only bootstraps the application. Code placed there has no routing (one URL at best), cannot be tested without starting the whole app, mixes concerns (it violates the single responsibility), and every new endpoint would mean editing and restarting the entry point.

### Q 2.7 — Your method returns Java objects, yet the browser shows JSON. Who did the conversion, and when?
Spring Boot's HTTP message converters, with Jackson behind them: `MappingJackson2HttpMessageConverter` serialises the returned `List<Programmer>` when the response is written, using the getters of the objects.