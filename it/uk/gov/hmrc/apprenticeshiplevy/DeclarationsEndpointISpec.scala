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
import uk.gov.hmrc.apprenticeshiplevy.data.{LevyDeclaration,PayrollPeriod}

@DoNotDiscover
class DeclarationsEndpointISpec extends WiremockFunSpec  {
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
              declarations should contain atLeastOneOf (LevyDeclaration(12345684,
                                                                        new LocalDateTime(2016, 10, 15, 16, 5, 23, 123),
                                                                        Some(new LocalDate(2016, 9, 5))),
                                                        LevyDeclaration(12345679,
                                                                        new LocalDateTime(2015, 4, 7, 16, 5, 23, 123),
                                                                        payrollPeriod=Some(PayrollPeriod("15-16", 12)),
                                                                        noPaymentForPeriod=Some(true)))

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