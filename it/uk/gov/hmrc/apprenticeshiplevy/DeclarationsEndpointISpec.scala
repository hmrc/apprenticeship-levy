package uk.gov.hmrc.apprenticeshiplevy

import org.scalatest._
import org.scalatest.Matchers._

import org.joda.time.{LocalDate, LocalDateTime}

import org.scalacheck.Gen

import play.api.test.{FakeRequest, Helpers, RouteInvokers}
import play.api.test.Helpers._
import play.api.libs.json.Json
import play.api.Play
import play.api.Play._
import play.api.Play.current
import play.api.libs.ws.WS
import views.html.helper

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault

import uk.gov.hmrc.apprenticeshiplevy.util._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.apprenticeshiplevy.data.{LevyDeclaration,PayrollPeriod}

@DoNotDiscover
class DeclarationsEndpointISpec extends WiremockFunSpec with IntegrationTestConfig {
  describe("Declarations Endpoint") {
    val contexts = Seq("/sandbox", "")
    contexts.foreach { case (context) =>
      describe (s"should when calling ${localMicroserviceUrl}$context/epaye/<empref>/declarations") {
        describe (s"with valid paramters") {
          it (s"should return levy declarations") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/declarations").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12345"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations.size shouldBe 9
            info(declarations.mkString("\n"))
          }

          it (s"should handle no payment period") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/declarations").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12345"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations(0) shouldBe LevyDeclaration(56774248744L,new LocalDateTime(2016, 5, 20, 14, 25, 32),None,None,None,Some(PayrollPeriod("16-17",8)),None,None,Some(true))
          }

          it (s"should handle inactive period") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/declarations").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12345"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations(3) shouldBe LevyDeclaration(6573215455L,new LocalDateTime(2016,4,20,14,25,32),None,Some(new LocalDate(2016,8,6)),Some(new LocalDate(2016,11,5)))
          }

          it (s"should handle ceased trading") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/declarations").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12345"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations(4) shouldBe LevyDeclaration(56774248742L,new LocalDateTime(2016,4,20,14,25,32),Some(new LocalDate(2016,6,6)))
          }

          it (s"should handle levies") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/declarations").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12345"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations(1) shouldBe LevyDeclaration(56774248743L,new LocalDateTime(2016,5,20,14,25,32),None,None,None,Some(PayrollPeriod("16-17",2)),Some(98.64),Some(15000))
            declarations(2) shouldBe LevyDeclaration(6573215455L,new LocalDateTime(2016,4,20,14,25,32),None,None,None,Some(PayrollPeriod("16-17",2)),Some(124.27),Some(15000))
          }
        }

        describe ("with invalid paramters") {
          it (s"404 for various emprefs") {
            pending
          }

          it (s"DES HTTP 400") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/400/declarations").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.BadRequestException] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }
          }

          it (s"DES HTTP 401") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/401/declarations").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.Upstream4xxResponse] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }
          }

          it (s"DES HTTP 403") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/403/declarations").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.Upstream4xxResponse] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }
          }

          it (s"DES HTTP 404") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/404/declarations").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 404
          }
        }

        describe ("when backend systems failing") {
          it (s"and connection closed it should throw IOException?") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/malformed/declarations").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[java.io.IOException] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }
          }

          it (s"and response is empty it should throw IOException?") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/empty/declarations").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[java.io.IOException] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }
          }

          it (s"should throw TimeoutException? when timed out") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/timeout/declarations").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.GatewayTimeoutException] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }
          }

          it (s"DES HTTP 500") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/500/declarations").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.Upstream5xxResponse] {
              // test
              val result = route(request).get

              // check
              status(result) shouldBe 500
            }
          }

          it (s"DES HTTP 503") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/503/declarations").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.Upstream5xxResponse] {
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
}