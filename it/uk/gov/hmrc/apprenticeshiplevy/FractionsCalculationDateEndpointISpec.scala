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
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/json")
            if (contentAsString(result).contains("NOT_IMPLEMENTED")) {
              info("NOT_IMPLEMENTED")
              throw new org.scalatest.exceptions.TestPendingException()
            } else {
              contentAsJson(result) shouldBe Json.parse(""""2016-06-19"""")
            }
          }
        }

        describe ("when backend systems failing") {
          it (s"should throw IOException? when connection closed") {
            // set up
            WireMock.reset()
            stubFor(get(urlEqualTo("/fraction-calculation-date")).withId(uuid).willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)))
            val request = FakeRequest(GET, "/sandbox/fraction-calculation-date").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[java.io.IOException] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }

            WiremockService.wireMockServer.resetToDefaultMappings()
          }
        }
      }
    }
  }
}