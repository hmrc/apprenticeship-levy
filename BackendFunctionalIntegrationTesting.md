# Running Local DES Stubbed Endpoints

It's possible to execute a mock of the backend endpoints this API facades using the [wiremock|http://wiremock.org/] server implemented in the integration tests including in this repository. To run simply enter the integration test console using `sbt it:console`, and then enter the paste mode by using `:paste`, finally copy paste the following block of code and complete with ctrl-D:

```scala
import uk.gov.hmrc.apprenticeshiplevy.util._
WiremockService.start()
```

This starts WireMock configured with stubbed endpoints with the same test data that is/will be available in the API sandbox environment. Each of the endpoints can be accessed using a tool such as curl. e.g. `curl -vvvv -data "{}" http://localhost:8080/registration`. Hopefully this will support consumers of the API with initial functional integration without necessitating the requirements for additional security measures.
