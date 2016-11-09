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
class EmploymentRefEndpointISpec extends WiremockFunSpec  {
  describe("Empref Endpoint") {
    val contexts = Seq("/sandbox", "")
    contexts.foreach { case (context) =>
      describe (s"should when calling ${localMicroserviceUrl}$context/epaye/<empref>") {
        describe (s"with valid parameters") {
          it (s"should return the declarations and fractions link for each empref") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/hal+json")
            val json = contentAsJson(result)

            (json \ "_links" \ "self" \ "href").as[String] shouldBe "/epaye/123%2FAB12345"
            (json \ "_links" \ "fractions" \ "href").as[String] shouldBe "/epaye/123%2FAB12345/fractions"
            (json \ "_links" \ "declarations" \ "href").as[String] shouldBe "/epaye/123%2FAB12345/declarations"
          }
        }

        describe ("with invalid paramters") {
          it (s"should return 400") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/400%2FAB12345").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 400
          }

          it (s"should return 401") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/401%2FAB12345").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 401
          }

          it (s"should return 403") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/403%2FAB12345").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 403
          }

          it (s"should return 404") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/404%2FAB12345").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 404
          }
        }

        describe ("when backend systems failing") {
          it (s"should throw IOException? when connection closed") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/999%2FAB12345").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/json")
          }

          it (s"should throw IOException? when response is empty") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/888%2FAB12345").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/json")
          }

          it (s"should throw TimeoutException? when timed out") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/777%2FAB12345").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/json")
          }

          it (s"DES HTTP 500") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/500%2FAB12345").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 503
          }

          it (s"DES HTTP 503") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/503%2FAB12345").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 503
          }
        }
      }
    }
  }
}