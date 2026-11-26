# ST2APR — Spring Boot workshop — Report

## Question index

The grading grid lists 14 questions (Parts 1–4). The handout numbers two of the Part 4 questions differently, so both numbers are given in the headings below.

| Grading grid | Topic | Answer |
|---|---|---|
| Q 1.7 | What the IDE does when starting the application | Q 1.7 |
| Q 1.8 | Tomcat message, who installed Tomcat | Q 1.8 |
| Q 1.10 | `@SpringBootApplication`, `SpringApplication.run(...)` | Q 1.10 |
| Q 1.11 | Why a restart is needed | Q 1.11 |
| Q 2.3 | Annotations, what happens under the hood | Q 2.3 |
| Q 2.4 | Why not in `main()` | Q 2.4 |
| Q 2.7 | Who converts objects to JSON, and when | Q 2.7 |
| Q 3.1 | Why it fails without configuration, what a starter does | Q 3.1 |
| Q 3.2 | Origin of `jakarta.persistence` | Q 3.2 |
| Q 3.3 | POJO vs entity, default table/column mapping | Q 3.3 |
| Q 3.4 | Where the repository methods come from | Q 3.4 |
| Q 3.5 | Constructor injection vs field injection | Q 3.5 |
| Q 4.1 | PUT vs PATCH *(handout: Q 4.2)* | Q 4.1 |
| Q 4.2 | `@Transactional` *(handout: Q 4.3)* | Q 4.2 |

Extra answers, beyond the grid (Parts 5–6 of the handout): Q 3.6, Q 5.1, Q 6.1.

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

## Part 3 — Persistence: MariaDB, JPA and the layered architecture

### Q 3.1 — Before configuring anything, restart: what is Spring Boot trying to do that it could not?
It fails with `Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured`. Adding the starter put `DataSourceAutoConfiguration` and Hibernate on the classpath, and they immediately try to build a connection pool — but nobody told them where the database is. A starter is not just a jar: it activates a whole subsystem through auto-configuration, while the configuration itself (URL, credentials) stays our responsibility.

### Q 3.2 — Where does `jakarta.persistence` come from? Which dependency? Why not `javax`?
It is the Jakarta Persistence API, brought transitively by `spring-boot-starter-data-jpa`. Since Jakarta EE 9 the `javax.*` packages were renamed to `jakarta.*`, and Spring Boot 3 (and 4) use the Jakarta namespace — so tutorials that import `javax.persistence` target Spring Boot 2.x.

### Q 3.3 — Difference between the POJO and the entity? Which table and columns for `lastName` and `favoriteBook`?
The POJO is a plain class; the entity is mapped to a table and managed by Hibernate (its state is tracked, it can be persisted, merged, deleted). `lastName` maps to `programmer.last_name` and `favoriteBook` to `programmer.favorite_book` through the default physical naming strategy (camelCase → snake_case). No `@Table` is needed because the table name defaults to the entity name (`Programmer` → `programmer`), and no `@Column(name = ...)` because the column name defaults to the attribute name.

### Q 3.4 — The repository interface has no body and no implementation. Where do `findAll()`, `save()`… come from?
They are declared by `JpaRepository`. At startup Spring Data scans the interface, generates a proxy that implements it (delegating to `SimpleJpaRepository`), and registers it as a bean — that is the object injected into the service.

### Q 3.5 — What does `@Service` bring? Why constructor injection instead of `@Autowired` on a field?
`@Service` is a specialisation of `@Component`: it declares a bean and documents the intent (business layer) — unlike `@RestController` (web layer, `@Controller` + `@ResponseBody`) and `@Repository` (persistence, with exception translation). Constructor injection makes the dependency `final` and mandatory, keeps it visible in the signature, and lets a test pass a fake without Spring; a field `@Autowired` hides the dependency and prevents `final`.

### Q 3.6 — Path of a request; which layer must never know HTTP? Which must never write SQL?
`GET /programmers/3` → `ProgrammerController.getProgrammer(3)` → `ProgrammerService.findById(3)` → `ProgrammerRepository.findById(3)` → Hibernate → MariaDB, then back the same way. The **service** and the **repository** must never know about HTTP; the **controller** and the **service** must never write SQL.

## Part 4 — Full CRUD

### Q 4.1 — PUT vs PATCH; which one fits here? *(handout Q 4.2)*
`PUT` replaces the whole representation of the resource: sending the same request twice leaves the server in the same state (idempotent). `PATCH` modifies only part of it. The client sends the complete programmer, so `PUT` is the right verb here — see `ProgrammerController.updateProgrammer`, which takes the full object in the body and overwrites every field.

### Q 4.2 — `@Transactional`: on which methods, what does it do, why does it matter most for update? *(handout Q 4.3)*
On the service methods that write and may touch several rows (`create`, `update`, `delete`) — not on the read-only finders, which Spring Data already runs in a read-only transaction. It opens a transaction around the method, commits on success and rolls back on a runtime exception. It matters most for **update** because that method reads then writes: `ProgrammerService.update` first calls `findById`, then `save`. Without a transaction those two calls belong to two different units of work, so a concurrent change can slip in between them.

### Additional — advantages and risks of `@RequestMapping` at class level *(handout Q 4.1, not in the grading grid)*
The base path is written once: changing it (or versioning it as `/api/v1`) touches one line, and each method only shows its own part of the path. The risk is that the full URL becomes invisible when reading a single method, and deep nesting (`/{id}/batches/{batchId}`) quickly becomes hard to follow.

## Part 5 — REST done right

### Q 5.1 — Why is this RPC style rather than REST? Rewrite the four routes.
RPC puts the action in the URL (`/getProgrammers`, `/addProgrammer`) and uses HTTP as a mere transport. REST exposes resources as plural nouns and lets the HTTP verb carry the action, with the identifier in the path:

| RPC route | REST route |
|---|---|
| `GET /getProgrammers` | `GET /programmers` |
| `POST /addProgrammer` | `POST /programmers` |
| `POST /deleteProgrammer/3` | `DELETE /programmers/3` |
| `POST /modifyProgrammer/3` | `PUT /programmers/3` |

Our controller follows these rules: `@RequestMapping("/programmers")` at class level, one verb-specific mapping per action, and the id as a path variable (`@PathVariable`).

### 5.2 — Status codes of the API

| Situation | Status | Body |
|---|---|---|
| One or several programmers retrieved | `200 OK` | the programmer(s) as JSON |
| The requested programmer does not exist | `404 Not Found` | `ProblemDetail` (RFC 9457) |
| A programmer is created | `201 Created` | the created programmer + `Location` header |
| A programmer is updated | `200 OK` | the updated programmer |
| A programmer is deleted | `204 No Content` | none |
| Update or deletion of a programmer that does not exist | `404 Not Found` | `ProblemDetail` |
| Invalid data sent by the client (e.g. empty last name) | `400 Bad Request` | `ProblemDetail` (Part 6.4 validation) |

## Part 6 — Clean error handling

### Q 6.1 — What did you gain? What should an error response never contain?
Handling "not found" once in `@RestControllerAdvice` means: the controllers stay thin (no `if`, no `ResponseEntity.notFound()`), a new error type costs one method instead of an edit in every endpoint, every client receives the same shape (RFC 9457 `ProblemDetail`), and the status code is decided in a single place.
An error response must never contain a stack trace, the SQL statement or the database exception, internal class names or server paths — those belong to the logs. The client gets a short, readable message ("Programmer 999 not found"), nothing that maps the internals of the server.