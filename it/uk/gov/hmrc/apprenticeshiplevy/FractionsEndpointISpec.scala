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
import uk.gov.hmrc.apprenticeshiplevy.data.{FractionCalculation,Fraction}

@DoNotDiscover
class FractionsEndpointISpec extends WiremockFunSpec  {
  describe("Fractions Endpoint") {
    val contexts = Seq("/sandbox", "")
    contexts.foreach { case (context) =>
      describe (s"should when calling ${localMicroserviceUrl}$context/epaye/<empref>/fractions") {
        describe (s"with valid paramters") {
          it (s"should return fractions") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/fractions").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/json")
            if (contentAsString(result).contains("NOT_IMPLEMENTED")) {
              info("NOT_IMPLEMENTED")
              throw new org.scalatest.exceptions.TestPendingException()
            } else {
              val json = contentAsJson(result)
              (json \ "empref").as[String] shouldBe "123/AB12345"
              val fractions = (json \ "fractionCalculations").as[Array[FractionCalculation]]
              val f1 = Seq(Fraction("England", BigDecimal(0.83)),
                           Fraction("Scotland", BigDecimal(0.11)),
                           Fraction("Wales", BigDecimal(0.06)),
                           Fraction("Northern Ireland", BigDecimal(0)))
              val f2 = Seq(Fraction("England", BigDecimal(0.78)),
                           Fraction("Scotland", BigDecimal(0.16)),
                           Fraction("Wales", BigDecimal(0.06)),
                           Fraction("Northern Ireland", BigDecimal(0)))
              fractions should contain atLeastOneOf (FractionCalculation(new LocalDate(2016, 3, 15),f1), FractionCalculation(new LocalDate(2015, 11, 18),f2))
            }
          }
        }

        describe ("with invalid paramters") {
          it (s"should return 404") {
            pending
          }
        }

        describe ("when backend systems failing") {
          it (s"should throw IOException? when connection closed") {
            pending
          }
        }
      }
    }
  }
}