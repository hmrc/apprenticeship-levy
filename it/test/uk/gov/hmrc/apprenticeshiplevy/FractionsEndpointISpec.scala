/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.uk.gov.hmrc.apprenticeshiplevy

import org.scalacheck.Gen
import org.scalatest.DoNotDiscover
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.play._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apprenticeshiplevy.data.des.{Fraction, FractionCalculation}
import views.html.helper

import java.time.LocalDate

@DoNotDiscover
class FractionsEndpointISpec extends WiremockFunSpec with ConfiguredServer with ScalaCheckPropertyChecks {

  val lastYear = LocalDate.now().getYear - 1
  describe("Fractions Endpoint") {
    val contexts = Seq("/sandbox", "")
    contexts.foreach { context =>
      describe(s"should when calling $localMicroserviceUrl$context/epaye/<empref>/fractions") {
        describe(s"with no parameters") {
          it(s"should return fractions") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/fractions").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12345"
            val fractions = (json \ "fractionCalculations").as[Array[FractionCalculation]]
            val f1 = List(Fraction("England", BigDecimal(0.83)))
            val f2 = List(Fraction("England", BigDecimal(0.78)))
            val date_1 = LocalDate.of(2016, 12, 23)
            val date_2 = LocalDate.of(2015, 8, 18)
            fractions should contain.atLeastOneOf(FractionCalculation(date_1, f1), FractionCalculation(date_2, f2))
          }

          it(s"should return fractions with correct empref values") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/864%2FTZ00000/fractions").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "864/TZ123"
          }
        }

        describe(s"with valid parameters") {
          it(s"?fromDate=2017-09-01 should return fractions") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/fractions?fromDate=2015-09-01")
              .withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12345"
            val fractions = (json \ "fractionCalculations").as[Array[FractionCalculation]]
            val f1 = List(Fraction("England", BigDecimal(0.83)))
            val f2 = List(Fraction("England", BigDecimal(0.78)))
            val date_1 = LocalDate.of(2016, 12, 23)
            val date_2 = LocalDate.of(2016, 8, 18)
            fractions should contain.atLeastOneOf(FractionCalculation(date_1, f1), FractionCalculation(date_2, f2))
          }

          it(s"?toDate=$lastYear-09-01 should return fractions") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/fractions?toDate=$lastYear-09-01")
              .withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12345"
            val fractions = (json \ "fractionCalculations").as[Array[FractionCalculation]]
            val f1 = List(Fraction("England", BigDecimal(0.83)))
            val f2 = List(Fraction("England", BigDecimal(0.78)))
            val date_1 = LocalDate.of(2016, 12, 23)
            val date_2 = LocalDate.of(2016, 8, 18)
            fractions should contain.atLeastOneOf(FractionCalculation(date_1, f1), FractionCalculation(date_2, f2))
          }

          it(s"?fromDate=2017-08-01&toDate=2017-09-01 should return fractions") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/fractions?fromDate=2017-08-01&toDate=2017-09-01")
              .withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12345"
            val fractions = (json \ "fractionCalculations").as[Array[FractionCalculation]]
            val f1 = List(Fraction("England", BigDecimal(0.83)))
            val f2 = List(Fraction("England", BigDecimal(0.78)))
            val date_1 = LocalDate.of(2016, 12, 23)
            val date_2 = LocalDate.of(2016, 8, 18)
            fractions should contain.atLeastOneOf(FractionCalculation(date_1, f1), FractionCalculation(date_2, f2))
          }
        }

        describe("with invalid paramters") {
          Seq("fromDate", "toDate").foreach { param =>
            it(s"should return 400 when $param param is invalid") {
              // set up
              val dates = for {str <- Gen.listOf(Gen.alphaNumChar)} yield str.mkString

              forAll(dates) { (date: String) =>
                whenever(date.nonEmpty) {
                  val requestUrl = param match {
                    case "fromDate" => s"$context/epaye/123%2FAB12345/fractions?fromDate=${helper.urlEncode(date)}&toDate=2015-06-30"
                    case _ => s"/sandbox/epaye/123%2FAB12345/fractions?fromDate=2015-06-03&toDate=${helper.urlEncode(date)}"
                  }
                  val request = FakeRequest(GET, requestUrl).withHeaders(standardDesHeaders(): _*)

                  // test
                  val result = route(app, request).get
                  val httpStatus = status(result)

                  // check
                  httpStatus shouldBe 400
                  contentType(result) shouldBe Some("application/json")
                  contentAsString(result) should include("""date parameter is in the wrong format. Should be '^(\\d{4})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$' where date format is yyyy-MM-dd and year is 2000 or later.""")
                }
              }
            }
          }

          it(s"should return http status 400 when DES HTTP 400") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/400%2FAB12345/fractions")
              .withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe BAD_REQUEST
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BAD_REQUEST","message":"Bad request error"}""")
          }

          it(s"should return http status 401 when DES HTTP 401") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/401%2FAB12345/fractions")
              .withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 401
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_UNAUTHORIZED","message":"DES unauthorised error"}""")
          }

          it(s"should return http status 403 when DES HTTP 403") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/403%2FAB12345/fractions")
              .withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 403
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_FORBIDDEN","message":"DES forbidden error"}""")
          }

          it(s"should return http status 404 when DES HTTP 404") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/404%2FAB12345/fractions")
              .withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 404
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_NOT_FOUND","message":"DES endpoint or EmpRef not found"}""")
          }

          it(s"should return 400 when to date is before from date") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/fractions?fromDate=2015-06-03&toDate=2015-03-30").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get
            val httpStatus = status(result)

            // check
            httpStatus shouldBe 400
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"BAD_REQUEST","message":"From date was after to date"}""")
          }
        }

        describe("when backend systems failing") {
          it("should return http status 503 when connection closed") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/999%2FAB12345/fractions")
              .withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")
          }

          it(s"should return http status 408 when timed out") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/777%2FAB12345/fractions")
              .withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 408
            contentType(result) shouldBe Some("application/json")
            contentAsString(result) should include("DES not responding error")
          }

          it(s"should return http status 503 when empty response") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/888%2FAB12345/fractions")
              .withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")
          }

          it(s"should return http status 503 when DES HTTP 500") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/500%2FAB12345/fractions")
              .withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")
          }

          it(s"should return http status 503 when DES HTTP 503") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/503%2FAB12345/fractions")
              .withHeaders(standardDesHeaders(): _*)
            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")
          }
        }
      }
    }
  }
}