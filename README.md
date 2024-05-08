# Application demonstrating approach for implementing [REST](https://en.wikipedia.org/wiki/Representational_state_transfer) client with desired [NFRs](https://en.wikipedia.org/wiki/Non-functional_requirement)

Reference implementation of my [article](https://www.dhaval-shah.com/performant-and-optimal-spring-webclient/) demonstrating how to implement [REST](https://en.wikipedia.org/wiki/Representational_state_transfer) client using [Spring WebClient](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)
with below considerations:
* Optimized in terms of connection pool management along with its externalized configurability
* Resilient to handle errors along with configurable retries
* Having capability to conditionally access secured REST endpoints
* WebClient recommendations

## Bootstrapping downstream system a.k.a 'alias-service-api' which is invoked by [Spring Webclient](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html)
1. Go to `bin` directory of [Go](https://golang.org/)
2. Run this command - `go run <Full Path>\alias-service-api.go`
