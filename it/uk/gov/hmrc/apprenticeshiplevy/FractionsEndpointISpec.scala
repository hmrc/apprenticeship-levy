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
import views.html.helper

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault

import uk.gov.hmrc.apprenticeshiplevy.util._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.apprenticeshiplevy.data.des.{FractionCalculation,Fraction}

@DoNotDiscover
class FractionsEndpointISpec extends WiremockFunSpec  {
  describe("Fractions Endpoint") {
    val contexts = Seq("/sandbox", "")
    contexts.foreach { case (context) =>
      describe (s"should when calling ${localMicroserviceUrl}$context/epaye/<empref>/fractions") {
        describe (s"with no parameters") {
          it (s"should return fractions") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123AB12345/fractions").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123AB12345"
            val fractions = (json \ "fractionCalculations").as[Array[FractionCalculation]]
            val f1 = List(Fraction("England", BigDecimal(0.83)))
            val f2 = List(Fraction("England", BigDecimal(0.78)))
            fractions should contain atLeastOneOf (FractionCalculation(new LocalDate(2016, 12, 23),f1), FractionCalculation(new LocalDate(2015, 8, 18),f2))
          }
        }

        describe (s"with valid parameters") {
          it (s"?fromDate=2017-09-01 should return fractions") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123AB12345/fractions?fromDate=2017-09-01").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/json")
            if (contentAsString(result).contains("NOT_IMPLEMENTED")) {
              info("NOT_IMPLEMENTED")
              throw new org.scalatest.exceptions.TestPendingException()
            } else {
              val json = contentAsJson(result)
              (json \ "empref").as[String] shouldBe "123AB12345"
              val fractions = (json \ "fractionCalculations").as[Array[FractionCalculation]]
              val f1 = List(Fraction("England", BigDecimal(0.83)))
              val f2 = List(Fraction("England", BigDecimal(0.78)))
              fractions should contain atLeastOneOf (FractionCalculation(new LocalDate(2016, 12, 23),f1), FractionCalculation(new LocalDate(2016, 8, 18),f2))
            }
          }

          it (s"?toDate=2017-09-01 should return fractions") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123AB12345/fractions?toDate=2017-09-01").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/json")
            if (contentAsString(result).contains("NOT_IMPLEMENTED")) {
              info("NOT_IMPLEMENTED")
              throw new org.scalatest.exceptions.TestPendingException()
            } else {
              val json = contentAsJson(result)
              (json \ "empref").as[String] shouldBe "123AB12345"
              val fractions = (json \ "fractionCalculations").as[Array[FractionCalculation]]
              val f1 = List(Fraction("England", BigDecimal(0.83)))
              val f2 = List(Fraction("England", BigDecimal(0.78)))
              fractions should contain atLeastOneOf (FractionCalculation(new LocalDate(2016, 12, 23),f1), FractionCalculation(new LocalDate(2016, 8, 18),f2))
            }
          }

          it (s"?fromDate=2017-08-01&toDate=2017-09-01 should return fractions") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123AB12345/fractions?fromDate=2017-08-01&toDate=2017-09-01").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/json")
            if (contentAsString(result).contains("NOT_IMPLEMENTED")) {
              info("NOT_IMPLEMENTED")
              throw new org.scalatest.exceptions.TestPendingException()
            } else {
              val json = contentAsJson(result)
              (json \ "empref").as[String] shouldBe "123AB12345"
              val fractions = (json \ "fractionCalculations").as[Array[FractionCalculation]]
              val f1 = List(Fraction("England", BigDecimal(0.83)))
              val f2 = List(Fraction("England", BigDecimal(0.78)))
              fractions should contain atLeastOneOf (FractionCalculation(new LocalDate(2016, 12, 23),f1), FractionCalculation(new LocalDate(2016, 8, 18),f2))
            }
          }
        }

        describe ("with invalid paramters") {
          it (s"DES HTTP 400") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/400/fractions").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.BadRequestException] {
              // test
              val result = route(request).get

              // check
              status(result) shouldBe 400
            }
          }

          it (s"DES HTTP 401") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/401/fractions").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.Upstream4xxResponse] {
              // test
              val result = route(request).get

              // check
              status(result) shouldBe 401
            }
          }

          it (s"DES HTTP 403") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/403/fractions").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.Upstream4xxResponse] {
              // test
              val result = route(request).get

              // check
              status(result) shouldBe 403
            }
          }

          it (s"DES HTTP 404") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/404/fractions").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 404
          }
        }

        describe ("when backend systems failing") {
          it (s"should throw IOException? when connection closed") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/malformed/fractions").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[java.io.IOException] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }
          }

          it (s"should throw TimeoutException? when timed out") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/timeout/fractions").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.GatewayTimeoutException] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }
          }

          it (s"should throw IOException? when empty response") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/empty/fractions").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[java.io.IOException] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }
          }

          it (s"DES HTTP 500") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/500/fractions").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.Upstream5xxResponse] {
              // test
              val result = route(request).get

              // check
              status(result) shouldBe 500
            }
          }

          it (s"DES HTTP 503") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/503/fractions").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

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