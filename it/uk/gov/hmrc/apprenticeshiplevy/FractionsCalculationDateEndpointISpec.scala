package uk.gov.hmrc.apprenticeshiplevy

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{status => _, _}
import com.github.tomakehurst.wiremock.http.Fault
import org.scalatest.Matchers._
import org.scalatest._
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apprenticeshiplevy.util._

@DoNotDiscover
class FractionsCalculationDateEndpointISpec extends WiremockFunSpec with ConfiguredServer  {
  describe("Fractions Calculation Date Endpoint") {
    val contexts = Seq("/sandbox", "")
    contexts.foreach { case (context) =>
      describe (s"should when calling ${localMicroserviceUrl}$context/fraction-calculation-date") {
        describe (s"when no backend systems failing") {
          it (s"return date") {
            // set up
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe OK
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse(""""2016-03-15"""")
          }
        }

        describe ("when backend systems failing") {
          it (s"should return http status 503 when connection closed") {
            // set up
            WireMock.reset()
            stubFor(get(urlEqualTo("/apprenticeship-levy/fraction-calculation-date"))
                    .withId(uuid).willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)))
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")

            WiremockService.wireMockServer.resetToDefaultMappings()
          }

          it (s"should return http status 408 when timed out") {
            // set up
            WireMock.reset()
            stubFor(get(urlEqualTo("/apprenticeship-levy/fraction-calculation-date"))
                    .withId(uuid).willReturn(aResponse().withStatus(200).withFixedDelay(1000*60)))
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 408
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_GATEWAY_TIMEOUT","message":"DES not responding error"}""")

            WiremockService.wireMockServer.resetToDefaultMappings()
          }

          it (s"should return http status 503 when empty response") {
            // set up
            WireMock.reset()
            stubFor(get(urlEqualTo("/apprenticeship-levy/fraction-calculation-date"))
                    .withId(uuid).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)))
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")

            WiremockService.wireMockServer.resetToDefaultMappings()
          }

          it (s"should return http status 404 when DES HTTP 404") {
            // set up
            WireMock.reset()
            stubFor(get(urlEqualTo("/apprenticeship-levy/fraction-calculation-date"))
                    .withId(uuid).willReturn(aResponse().withStatus(404).withBody("""{"reason" : "Not found"}""")))
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 404
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_NOT_FOUND","message":"DES endpoint or EmpRef not found"}""")

            WiremockService.wireMockServer.resetToDefaultMappings()
          }

          it (s"should return http status 503 when DES HTTP 500") {
            // set up
            WireMock.reset()
            stubFor(get(urlEqualTo("/apprenticeship-levy/fraction-calculation-date"))
                    .withId(uuid).willReturn(aResponse().withStatus(500).withBody("""{"reason" : "DES not working"}""")))
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")

            WiremockService.wireMockServer.resetToDefaultMappings()
          }

          it (s"should return http status 503 when DES HTTP 503") {
            // set up
            WireMock.reset()
            stubFor(get(urlEqualTo("/apprenticeship-levy/fraction-calculation-date"))
                    .withId(uuid).willReturn(aResponse().withStatus(503).withBody("""{"reason" : "Backend systems not working"}""")))
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")

            WiremockService.wireMockServer.resetToDefaultMappings()
          }

          it (s"should return http status 401 when DES HTTP 401") {
            // set up
            WireMock.reset()
            stubFor(get(urlEqualTo("/apprenticeship-levy/fraction-calculation-date"))
                    .withId(uuid).willReturn(aResponse().withStatus(401).withBody("""{"reason" : "Not authorized"}""")))
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 401
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_UNAUTHORIZED","message":"DES unauthorised error"}""")

            WiremockService.wireMockServer.resetToDefaultMappings()
          }

          it (s"should return http status 403 when DES HTTP 403") {
            // set up
            WireMock.reset()
            stubFor(get(urlEqualTo("/apprenticeship-levy/fraction-calculation-date"))
                    .withId(uuid).willReturn(aResponse().withStatus(403).withBody("""{"reason" : "Forbidden"}""")))
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 403
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_FORBIDDEN","message":"DES forbidden error"}""")

            WiremockService.wireMockServer.resetToDefaultMappings()
          }

          it (s"should return http status 429 when DES HTTP 429") {
            // set up
            WireMock.reset()
            stubFor(get(urlEqualTo("/apprenticeship-levy/fraction-calculation-date"))
                    .withId(uuid).willReturn(aResponse().withStatus(429).withBody("""{"reason" : "Drowning in requests"}""")))
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 429
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_TOO_MANY_REQUESTS","message":"DES too many requests"}""")

            WiremockService.wireMockServer.resetToDefaultMappings()
          }

          it (s"should return http status 408 when DES HTTP 408") {
            // set up
            WireMock.reset()
            stubFor(get(urlEqualTo("/apprenticeship-levy/fraction-calculation-date"))
                    .withId(uuid).willReturn(aResponse().withStatus(408).withBody("""{"reason" : "Not responding"}""")))
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 408
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_TIMEOUT","message":"DES not responding error"}""")

            WiremockService.wireMockServer.resetToDefaultMappings()
          }

          it (s"should return http status 503 when DES HTTP 409") {
            // set up
            WireMock.reset()
            stubFor(get(urlEqualTo("/apprenticeship-levy/fraction-calculation-date"))
                    .withId(uuid).willReturn(aResponse().withStatus(409).withBody("""{"reason" : "Some 4xxx error"}""")))
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_OTHER","message":"DES 4xx error"}""")

            WiremockService.wireMockServer.resetToDefaultMappings()
          }
        }
      }
    }
  }
}