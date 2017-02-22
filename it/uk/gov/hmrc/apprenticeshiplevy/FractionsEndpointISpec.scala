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
import org.scalatestplus.play._

@DoNotDiscover
class FractionsEndpointISpec extends WiremockFunSpec with ConfiguredServer  {
  describe("Fractions Endpoint") {
    val contexts = Seq("/sandbox", "")
    contexts.foreach { case (context) =>
      describe (s"should when calling ${localMicroserviceUrl}$context/epaye/<empref>/fractions") {
        describe (s"with no parameters") {
          it (s"should return fractions") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/fractions").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12345"
            val fractions = (json \ "fractionCalculations").as[Array[FractionCalculation]]
            val f1 = List(Fraction("England", BigDecimal(0.83)))
            val f2 = List(Fraction("England", BigDecimal(0.78)))
            fractions should contain atLeastOneOf (FractionCalculation(new LocalDate(2016, 12, 23),f1), FractionCalculation(new LocalDate(2015, 8, 18),f2))
          }
        }

        describe (s"with valid parameters") {
          it (s"?fromDate=2017-09-01 should return fractions") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/fractions?fromDate=2017-09-01")
                          .withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12345"
            val fractions = (json \ "fractionCalculations").as[Array[FractionCalculation]]
            val f1 = List(Fraction("England", BigDecimal(0.83)))
            val f2 = List(Fraction("England", BigDecimal(0.78)))
            fractions should contain atLeastOneOf (FractionCalculation(new LocalDate(2016, 12, 23),f1), FractionCalculation(new LocalDate(2016, 8, 18),f2))
          }

          it (s"?toDate=2017-09-01 should return fractions") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/fractions?toDate=2017-09-01")
                          .withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12345"
            val fractions = (json \ "fractionCalculations").as[Array[FractionCalculation]]
            val f1 = List(Fraction("England", BigDecimal(0.83)))
            val f2 = List(Fraction("England", BigDecimal(0.78)))
            fractions should contain atLeastOneOf (FractionCalculation(new LocalDate(2016, 12, 23),f1), FractionCalculation(new LocalDate(2016, 8, 18),f2))
          }

          it (s"?fromDate=2017-08-01&toDate=2017-09-01 should return fractions") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/fractions?fromDate=2017-08-01&toDate=2017-09-01")
                          .withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12345"
            val fractions = (json \ "fractionCalculations").as[Array[FractionCalculation]]
            val f1 = List(Fraction("England", BigDecimal(0.83)))
            val f2 = List(Fraction("England", BigDecimal(0.78)))
            fractions should contain atLeastOneOf (FractionCalculation(new LocalDate(2016, 12, 23),f1), FractionCalculation(new LocalDate(2016, 8, 18),f2))
          }
        }

        describe ("with invalid paramters") {
          Seq("fromDate", "toDate").foreach { case (param) =>
            it (s"should return 400 when $param param is invalid") {
              // set up
              val dates = for { str <- Gen.listOf(Gen.alphaNumChar) } yield str.mkString

              forAll(dates) { (date: String) =>
                whenever (!date.isEmpty) {
                  val requestUrl = param match {
                    case "fromDate" => s"$context/epaye/123%2FAB12345/fractions?fromDate=${helper.urlEncode(date)}&toDate=2015-06-30"
                    case _ => s"/sandbox/epaye/123%2FAB12345/fractions?fromDate=2015-06-03&toDate=${helper.urlEncode(date)}"
                  }
                  val request = FakeRequest(GET, requestUrl).withHeaders(standardDesHeaders: _*)

                  // test
                  val result = route(app, request).get
                  val httpStatus = status(result)

                  // check
                  httpStatus shouldBe 400
                  contentType(result) shouldBe Some("application/json")
                  contentAsString(result) should include ("""date parameter is in the wrong format. Should be ('^(\\d{4})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$' where data is yyyy-MM-dd and year is 2000 or later""")
                }
              }
            }
          }

          it (s"should return http status 400 when DES HTTP 400") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/400%2FAB12345/fractions")
                          .withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 400
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BAD_REQUEST","message":"Bad request error: a backend error"}""")
          }

          it (s"should return http status 401 when DES HTTP 401") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/401%2FAB12345/fractions")
                          .withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 401
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_UNAUTHORIZED","message":"DES unauthorised error: a backend error"}""")
          }

          it (s"should return http status 403 when DES HTTP 403") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/403%2FAB12345/fractions")
                          .withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 403
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_FORBIDDEN","message":"DES forbidden error: a backend error"}""")
          }

          it (s"should return http status 404 when DES HTTP 404") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/404%2FAB12345/fractions")
                          .withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 404
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_NOT_FOUND","message":"DES endpoint not found: a backend error"}""")
          }

          it (s"should return 400 when to date is before from date") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/fractions?fromDate=2015-06-03&toDate=2015-03-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get
            val httpStatus = status(result)

            // check
            httpStatus shouldBe 400
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"BAD_REQUEST","message":"From date was after to date"}""")
          }
        }

        describe ("when backend systems failing") {
          it (s"should return http status 503 when connection closed") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/999%2FAB12345/fractions")
                          .withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error: Remotely closed"}""")
          }

          it (s"should return http status 408 when timed out") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/777%2FAB12345/fractions")
                          .withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 408
            contentType(result) shouldBe Some("application/json")
            contentAsString(result) should include ("DES not responding error: GET of 'http://localhost:8080/apprenticeship-levy/employers/777AB12345/fractions'")
          }

          it (s"should return http status 503 when empty response") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/888%2FAB12345/fractions")
                          .withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error: Remotely closed"}""")
          }

          it (s"should return http status 503 when DES HTTP 500") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/500%2FAB12345/fractions")
                          .withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error: a backend error"}""")
          }

          it (s"should return http status 503 when DES HTTP 503") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/503%2FAB12345/fractions")
                          .withHeaders(standardDesHeaders: _*)
            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error: a backend error"}""")
          }
        }
      }
    }
  }
}