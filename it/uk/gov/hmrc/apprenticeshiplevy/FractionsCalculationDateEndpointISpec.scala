package uk.gov.hmrc.apprenticeshiplevy

import org.scalatest._
import org.scalatest.Matchers._

import org.scalacheck.Gen

import play.api.test.{FakeRequest, Helpers, RouteInvokers}
import play.api.test.Helpers._
import play.api.libs.json.Json
import play.api.Play
import play.api.Play._
import views.html.helper

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault

import uk.gov.hmrc.apprenticeshiplevy.util._
import uk.gov.hmrc.play.test.UnitSpec

@DoNotDiscover
class FractionsCalculationDateEndpointISpec extends WiremockFunSpec  {
  describe("Fractions Calculation Date Endpoint") {
    val contexts = Seq("/sandbox", "")
    contexts.foreach { case (context) =>
      describe (s"should when calling ${localMicroserviceUrl}$context/fraction-calculation-date") {
        describe (s"when no backend systems failing") {
          it (s"return date") {
            // set up
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                                                              "Environment"->"isit",
                                                                                              "Authorization"->"Bearer 2423324")

            // test
            val result = route(request).get

            // check
            status(result) shouldBe OK
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse(""""2016-03-15"""")
          }
        }

        describe ("when backend systems failing") {
          it (s"should throw IOException? when connection closed") {
            // set up
            WireMock.reset()
            stubFor(get(urlEqualTo("/fraction-calculation-date")).withId(uuid).willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)))
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[java.io.IOException] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }

            WiremockService.wireMockServer.resetToDefaultMappings()
          }

          it (s"should throw TimeoutException? when timed out") {
            // set up
            WireMock.reset()
            stubFor(get(urlEqualTo("/fraction-calculation-date")).withId(uuid).willReturn(aResponse().withStatus(200).withFixedDelay(1000*60)))
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.GatewayTimeoutException] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }

            WiremockService.wireMockServer.resetToDefaultMappings()
          }

          it (s"should throw IOException? when empty response") {
            // set up
            WireMock.reset()
            stubFor(get(urlEqualTo("/fraction-calculation-date")).withId(uuid).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)))
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[java.io.IOException] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }

            WiremockService.wireMockServer.resetToDefaultMappings()
          }

          it (s"DES HTTP 500") {
            // set up
            WireMock.reset()
            stubFor(get(urlEqualTo("/fraction-calculation-date")).withId(uuid).willReturn(aResponse().withStatus(500)))
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.Upstream5xxResponse] {
              // test
              val result = route(request).get

              // check
              status(result) shouldBe 500
            }

            WiremockService.wireMockServer.resetToDefaultMappings()
          }

          it (s"DES HTTP 503") {
            // set up
            WireMock.reset()
            stubFor(get(urlEqualTo("/fraction-calculation-date")).withId(uuid).willReturn(aResponse().withStatus(503)))
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.Upstream5xxResponse] {
              // test
              val result = route(request).get

              // check
              status(result) shouldBe 503
            }

            WiremockService.wireMockServer.resetToDefaultMappings()
          }
        }
      }
    }
  }
}