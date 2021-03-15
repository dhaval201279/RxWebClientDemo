# Application demonstrating approach for implementing [REST](https://en.wikipedia.org/wiki/Representational_state_transfer) client with desired [NFRs](https://en.wikipedia.org/wiki/Non-functional_requirement)

Reference implementation of my [article](http://dhaval-shah.com/rest-client-with-desired-nfrs-using-spring-webclient/) demonstrating how to implement [REST](https://en.wikipedia.org/wiki/Representational_state_transfer) client using [Spring Rest Template](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html) 
and [Apache HTTP Client](https://hc.apache.org/httpcomponents-client-ga/) which is :
* Optimized in terms of connection pool management along with its externalized configurability
* Resilient to handle errors along with configurable retries
* Easy to monitor Connection Pool as a resource by capturing required connection pool metrics
* Having capability to conditionally access secured REST endpoints

## Bootstrapping downstream system a.k.a 'alias-service-api' which is invoked by [Spring Webclient](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html)
1. Go to `bin` directory of [Go](https://golang.org/)
2. Run this command - `go run <Full Path>\alias-service-api.go`
