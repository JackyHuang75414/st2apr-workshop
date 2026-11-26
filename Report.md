# ST2APR Spring Boot Workshop Report

Repository: https://github.com/JackyHuang75414/st2apr-workshop

## Question index

The grading grid asks 14 questions for Parts 1 to 4. The handout numbers the last two differently, so both numbers are in the headings below.

| Grading grid | Topic | Answer |
|---|---|---|
| Q 1.7 | What the IDE does when starting | Q 1.7 |
| Q 1.8 | Tomcat message, who installed it | Q 1.8 |
| Q 1.10 | @SpringBootApplication, run() | Q 1.10 |
| Q 1.11 | Why a restart is needed | Q 1.11 |
| Q 2.3 | Annotations, under the hood | Q 2.3 |
| Q 2.4 | Why not in main() | Q 2.4 |
| Q 2.7 | Who converts objects to JSON | Q 2.7 |
| Q 3.1 | Why it fails without configuration | Q 3.1 |
| Q 3.2 | Where jakarta.persistence comes from | Q 3.2 |
| Q 3.3 | POJO vs entity, default mapping | Q 3.3 |
| Q 3.4 | Where repository methods come from | Q 3.4 |
| Q 3.5 | Constructor injection | Q 3.5 |
| Q 4.1 | PUT vs PATCH (handout Q 4.2) | Q 4.1 |
| Q 4.2 | @Transactional (handout Q 4.3) | Q 4.2 |

Extra answers, not in the grid: Q 3.6, Q 5.1, Q 6.1.

## Part 1 - Create and run your project

### Q 1.7 - What does the IDE do before and while starting the application?
The IDE does not compile the code itself. It runs Maven, which downloads the missing dependencies and compiles src/main/java into target/classes. Then it starts a JVM with those classes and the dependency jars on the classpath, and calls main(). After that Spring Boot creates the context, runs the auto-configuration and starts Tomcat. The IDE only shows the console and keeps a handle on the process.

### Q 1.8 - What does "Tomcat started on port 8080 (http) with context path '/'" mean? Who installed Tomcat?
Tomcat runs inside our own application, in the same JVM. It listens on port 8080, and the context path is the root, so an endpoint is simply http://localhost:8080/programmers with no prefix. Nobody installed Tomcat. It arrives as jar files (tomcat-embed-*) with spring-boot-starter-webmvc, and Spring Boot starts it from code. That is why we have no Tomcat service, no CATALINA_HOME and no webapps folder.

### Q 1.10 - What is the role of @SpringBootApplication and SpringApplication.run(...)?
@SpringBootApplication is three annotations in one. @SpringBootConfiguration says this class is a configuration class. @EnableAutoConfiguration asks Spring Boot to build the beans it finds on the classpath, for example Tomcat and the DispatcherServlet. @ComponentScan scans the package of this class and its sub-packages, which is the reason every class must live under st2apr.workshop.
SpringApplication.run() starts everything: it creates the ApplicationContext, instantiates the beans, starts the server and returns the context.

### Q 1.11 - After each code change you must restart. Why?
A class is loaded once by the JVM and the .class file is not read again, so new code needs a new JVM. The Spring context is also built once at startup, and the routes are registered at that moment. A new @GetMapping would not be known. The cycle is always compile, new JVM, new context.

## Part 2 - Your first endpoint

### Q 2.3 - Which annotations do you need on the class and on the method?
@RestController on the class means the returned value goes into the HTTP response body as JSON. @GetMapping("/programmers") on the method links GET /programmers to it. When the application starts, RequestMappingHandlerMapping registers the route, and each request is sent to the method by the DispatcherServlet.

### Q 2.4 - Why is putting the code in the main method a bad idea?
main() should only start the application. Code in that method has no routing, so we could expose one URL at most, we could not test it without starting the whole application, and we would mix the startup with a feature. It also violates the single responsibility.

### Q 2.7 - Your method returns Java objects, yet the browser shows JSON. Who did the conversion, and when?
Jackson does it. Spring Boot has HTTP message converters, and MappingJackson2HttpMessageConverter writes the returned List<Programmer> as JSON when the response is sent. It uses the getters of the class, so a field without a getter does not appear in the JSON.

## Part 3 - Persistence with JPA and MariaDB

### Q 3.1 - Before configuring anything, restart: what is Spring Boot trying to do that it could not?
It fails with "Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured". The starter put DataSourceAutoConfiguration and Hibernate on the classpath, and they try to build a connection pool when the application starts. Nobody told them where the database is. So a starter is not just a jar: it activates a subsystem, but the configuration is still ours to write.

### Q 3.2 - Where does jakarta.persistence come from? Which dependency? Why not javax?
It is the Jakarta Persistence API, brought by spring-boot-starter-data-jpa. The packages were renamed from javax to jakarta with Jakarta EE 9. Spring Boot 3 and 4 use jakarta, so tutorials that import javax.persistence are for Spring Boot 2.

### Q 3.3 - Difference between the POJO and the entity? Which table and columns for lastName and favoriteBook?
The POJO is a normal class. The entity is the same class with JPA annotations, and Hibernate manages it: it tracks its state and can save or delete it. lastName goes to the column last_name and favoriteBook to favorite_book, because the default naming strategy converts camelCase to snake_case. We do not need @Table because the table name defaults to the class name in lowercase, Programmer becomes programmer. We do not need @Column(name = ...) because the column name defaults to the attribute name.

### Q 3.4 - The repository interface has no body. Where do findAll() and save() come from?
They are declared in JpaRepository. At startup Spring Data scans the interface and creates a proxy that implements it, then registers it as a bean. That proxy is what gets injected in the service, so we never write the implementation.

### Q 3.5 - What does @Service bring? Why constructor injection?
@Service is a @Component with a meaning: this class is the business layer. @RestController is the web layer and @Repository is the persistence layer, with exception translation. Constructor injection is better because the field can be final, the dependency is visible in the constructor, and a test can pass a fake object. With @Autowired on a field the dependency is hidden and cannot be final.

### Q 3.6 - Path of a request; which layer must never know HTTP? Which must never write SQL?
GET /programmers/3 calls ProgrammerController.getProgrammer(3), then ProgrammerService.findById(3), then ProgrammerRepository.findById(3), then Hibernate and MariaDB, and the result comes back the same way. The service and the repository must never know about HTTP. The controller and the service must never write SQL.

## Part 4 - Full CRUD

### Q 4.1 - PUT vs PATCH, which one fits here? (handout Q 4.2)
PUT replaces the whole resource. Sending the same request twice gives the same result, so it is idempotent. PATCH changes only the fields that are sent. In our case the client sends the complete programmer, so PUT is the right verb. ProgrammerController.updateProgrammer expects the full object in the body.

### Q 4.2 - @Transactional: where, how, and why it matters for update (handout Q 4.3)
I put it on the write methods of the service: create, update and delete. The read methods do not need it because Spring Data already runs them in a read-only transaction. It opens a transaction around the method, commits when the method ends normally and rolls back when a RuntimeException is thrown. It matters most for update because update reads first with findById and writes after with save. Without a transaction those two calls are not in the same unit of work.

### Additional - advantages and risks of @RequestMapping at class level (handout Q 4.1)
The base path is written once, so changing it or versioning it as /api/v1 touches one line and each method only shows its own part. The risk is that the full URL is invisible when you read one method, and deep paths like /{id}/batches/{batchId} become hard to follow.

## Part 5 - REST done right

### Q 5.1 - Why is this RPC style rather than REST? Rewrite the four routes.
RPC puts the action in the URL and uses HTTP only as a transport. REST exposes resources as plural nouns, and the HTTP verb carries the action, with the identifier in the path.

| RPC route | REST route |
|---|---|
| GET /getProgrammers | GET /programmers |
| POST /addProgrammer | POST /programmers |
| POST /deleteProgrammer/3 | DELETE /programmers/3 |
| POST /modifyProgrammer/3 | PUT /programmers/3 |

Our controller follows these rules. @RequestMapping("/programmers") is on the class, each method has one verb, and the id is a path variable.

### 5.2 - Status codes of the API

| Situation | Status | Body |
|---|---|---|
| One or several programmers retrieved | 200 OK | the programmers as JSON |
| The programmer does not exist | 404 Not Found | ProblemDetail (RFC 9457) |
| A programmer is created | 201 Created | the programmer and a Location header |
| A programmer is updated | 200 OK | the updated programmer |
| A programmer is deleted | 204 No Content | none |
| Update or deletion of an unknown id | 404 Not Found | ProblemDetail |
| Invalid data, for example an empty last name | 400 Bad Request | ProblemDetail |

## Part 6 - Clean error handling

### Q 6.1 - What did you gain? What should an error response never contain?
Handling "not found" once in @RestControllerAdvice keeps the controllers short: no if and no ResponseEntity.notFound() in the controller. A new error type costs one method in the advice instead of a change in every endpoint, and every client gets the same shape, a ProblemDetail. The status code is decided in one place.
An error response must never contain a stack trace, the SQL statement, the database exception, internal class names or server paths. Those belong in the logs. The client gets a short message like "Programmer 999 not found".

## Reflection

Tooling was the first problem. IntelliJ showed List.of and org.springframework in red while Maven compiled the same code without error. The module language level was 5, and the Maven wrapper was on 3.10, which the IDE could not import. I lost more time there than on the code itself.

The second problem was my first controller. It looked correct, but GET /programmers/1 returned 404 and GET /programmers?id=1 returned the whole list, because I put @RequestBody on a method without @PostMapping and used @RequestMapping for the find by id. Seeing it in the browser made the idea of a route clear for me.

The no-argument constructor was also strange until I understood that Hibernate builds an empty object and then fills the fields one by one.

@Transactional is the part I am still not sure about. I know what it should do, but I have not seen a rollback happen yet.
