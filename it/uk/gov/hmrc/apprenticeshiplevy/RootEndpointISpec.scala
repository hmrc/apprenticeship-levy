package uk.gov.hmrc.apprenticeshiplevy

import org.scalatest._
import org.scalatest.Matchers._

import org.scalacheck.Gen

import play.api.test.{FakeRequest, Helpers, RouteInvokers}
import play.api.test.Helpers._
import play.api.libs.json.Json
import play.api.Play
import play.api.libs.Crypto
import play.api.Play._
import views.html.helper
import play.Logger

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault

import uk.gov.hmrc.apprenticeshiplevy.util._
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatestplus.play._

@DoNotDiscover
class RootEndpointISpec extends WiremockFunSpec with ConfiguredServer {
  describe("Root Endpoint") {
    val contexts = Seq("/sandbox", "")
    contexts.foreach { case (context) =>
      describe (s"should when calling ${localMicroserviceUrl}$context/") {
        describe (s"and backend systems not failing") {
          it (s"should return links for each empref") {
            // set up
            val response = dFileToStr("./it/resources/data/input/mapping_body")
            println(response)
            println(s"'${aesKey}'")
            println(s"'${eStr(response)}'")
            println(s"'${dStr(eStr(response))}'")
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withBody(response)))
            val request = FakeRequest(GET, s"$context/").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe OK
            contentType(result) shouldBe Some("application/hal+json")
            val json = contentAsJson(result)
            (json \ "_links" \ "self" \ "href").as[String] shouldBe "/"
            (json \ "_links" \ "123/AB12345" \ "href").as[String] shouldBe "/epaye/123%2FAB12345"
          }
        }

        describe ("when errors occur") {
          it (s"should return 401 when Auth returns 401") {
            // set up
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withStatus(401).withStatusMessage("Not authorised.")))
            val request = FakeRequest(GET, s"$context/").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 401
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR","message":"Auth unauthorised error: GET of 'http://localhost:8080/auth/authority' returned 401. Response body: ''"}""")
          }

          it (s"should return 403 when Auth returns 403") {
            // set up
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withStatus(403).withStatusMessage("Forbidden.")))
            val request = FakeRequest(GET, s"$context/").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 403
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR","message":"Auth forbidden error: GET of 'http://localhost:8080/auth/authority' returned 403. Response body: ''"}""")
          }

          it (s"HTTP 404") {
            // set up
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withStatus(404).withStatusMessage("Not found.")))
            val request = FakeRequest(GET, s"$context/").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 404
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR","message":"Auth endpoint not found: GET of 'http://localhost:8080/auth/authority' returned 404 (Not Found). Response body: ''"}""")
          }
        }

        describe ("when backend systems failing") {
          it (s"should return http status 503 when connection closed") {
            // set up
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)))
            val request = FakeRequest(GET, s"$context/").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR","message":"Auth connection error: Remotely closed"}""")
          }

          it (s"should return 503 when returning empty response and connection closed") {
            // set up
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)))
            val request = FakeRequest(GET, s"$context/").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR","message":"Auth connection error: Remotely closed"}""")
          }

          it (s"should return 408 when timed out") {
            // set up
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withStatus(200).withFixedDelay(1000*60)))
            val request = FakeRequest(GET, s"$context/").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 408
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR","message":"Auth not responding error: GET of 'http://localhost:8080/auth/authority' timed out with message 'Request timeout to localhost/127.0.0.1:8080 after 500 ms'"}""")
          }

          it (s"should return 503 when Auth returns 500") {
            // set up
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withStatus(500).withStatusMessage("Internal server error")))
            val request = FakeRequest(GET, s"$context/").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR","message":"Auth 5xx error: GET of 'http://localhost:8080/auth/authority' returned 500. Response body: ''"}""")
          }

          it (s"should return 503 when Auth returns 503") {
            // set up
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withStatus(503).withStatusMessage("Backend systems failing")))
            val request = FakeRequest(GET, s"$context/").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR","message":"Auth 5xx error: GET of 'http://localhost:8080/auth/authority' returned 503. Response body: ''"}""")
          }
        }
      }
    }
  }
}