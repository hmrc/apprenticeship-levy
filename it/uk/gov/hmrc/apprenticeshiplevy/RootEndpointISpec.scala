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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault

import uk.gov.hmrc.apprenticeshiplevy.util._
import uk.gov.hmrc.play.test.UnitSpec

@DoNotDiscover
class RootEndpointISpec extends WiremockFunSpec  {
  describe("Root Endpoint") {
    val contexts = Seq("/sandbox", "")
    contexts.foreach { case (context) =>
      describe (s"should when calling ${localMicroserviceUrl}$context/") {
        describe (s"and backend systems not failing") {
          it (s"should return links for each empref") {
            // set up
            val response = dFileToStr("./it/resources/data/input/mapping_body")
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withBody(response)))
            val request = FakeRequest(GET, s"$context/").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

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
          it (s"HTTP 401") {
            // set up
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withStatus(401).withStatusMessage("Not authorised.")))
            val request = FakeRequest(GET, s"$context/").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.Upstream4xxResponse] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }
          }
        }

        describe ("when backend systems failing") {
          it (s"should throw IOException? when connection closed") {
            // set up
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)))
            val request = FakeRequest(GET, s"$context/").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[java.io.IOException] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }
          }
          it (s"HTTP 500") {
            // set up
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withStatus(500).withStatusMessage("Internal server error")))
            val request = FakeRequest(GET, s"$context/").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.Upstream5xxResponse] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }
          }
          it (s"HTTP 503") {
            // set up
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withStatus(503).withStatusMessage("Backend systems failing")))
            val request = FakeRequest(GET, s"$context/").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.Upstream5xxResponse] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }
          }
        }
      }
    }
  }
}