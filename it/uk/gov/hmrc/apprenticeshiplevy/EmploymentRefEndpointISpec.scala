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
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

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
          it (s"should return 404") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/404").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 404
          }
        }

        describe ("when backend systems failing") {
          it (s"should throw IOException? when connection closed") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/malformed").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[java.io.IOException] {
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