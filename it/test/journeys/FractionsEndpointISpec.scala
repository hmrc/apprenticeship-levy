/*
 * Copyright 2026 HM Revenue & Customs
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

package journeys

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.http.Fault
import models.des.{Fraction, FractionCalculation}
import org.scalacheck.Gen
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{BAD_REQUEST, OK, SERVICE_UNAVAILABLE}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsJson, contentAsString, contentType, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import util.StubbingData.*
import util.ISpecBase
import views.html.helper

import java.time.LocalDate

class FractionsEndpointISpec
  extends ISpecBase {

  stubGetServerWithId(aResponse().withStatus(OK), validReadURL1, auuid1)
  stubGetServerWithId(aResponse().withStatus(OK), validReadURL2, auuid2)
  stubGetServerWithId(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK), faultURL1, auuid3)
  stubGetServerWithId(aResponse().withStatus(OK), invalidReadURL1, auuid4)
  stubGetServerWithId(aResponse().withStatus(OK), validRead, auuid5)

  private val lastYear = LocalDate.now().getYear - 1

  "Fractions Endpoint" when {
    Seq("/sandbox", "").foreach { context =>
      s"calling $context/epaye/<empref>/fractions" when {
        "with no parameters"  must {
          "return fractions" in {
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/fractions").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            contentType(result) mustBe Some("application/json")
            val json = contentAsJson(result)
            (json \ "empref").as[String] mustBe "123/AB12345"
            val fractions = (json \ "fractionCalculations").as[Array[FractionCalculation]]
            val f1 = Seq(Fraction("England", BigDecimal(0.83)))
            val f2 = Seq(Fraction("England", BigDecimal(0.78)))
            val date_1 = LocalDate.of(2016, 12, 23)
            val date_2 = LocalDate.of(2015, 8, 18)
            fractions must contain.atLeastOneOf(FractionCalculation(date_1, f1), FractionCalculation(date_2, f2))
          }

          "return fractions with correct empref values" in {
            val request = FakeRequest(GET, s"$context/epaye/864%2FTZ00000/fractions").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            contentType(result) mustBe Some("application/json")
            val json = contentAsJson(result)
            (json \ "empref").as[String] mustBe "864/TZ123"
          }
        }

        "with valid parameters" must {
          "?fromDate=2017-09-01: return fractions" in {
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/fractions?fromDate=2015-09-01")
              .withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            contentType(result) mustBe Some("application/json")
            val json = contentAsJson(result)
            (json \ "empref").as[String] mustBe "123/AB12345"
            val fractions = (json \ "fractionCalculations").as[Array[FractionCalculation]]
            val f1 = Seq(Fraction("England", BigDecimal(0.83)))
            val f2 = Seq(Fraction("England", BigDecimal(0.78)))
            val date_1 = LocalDate.of(2016, 12, 23)
            val date_2 = LocalDate.of(2016, 8, 18)
            fractions must contain.atLeastOneOf(FractionCalculation(date_1, f1), FractionCalculation(date_2, f2))
          }

          s"?toDate=$lastYear-09-01: return fractions" in {
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/fractions?toDate=$lastYear-09-01")
              .withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            contentType(result) mustBe Some("application/json")
            val json = contentAsJson(result)
            (json \ "empref").as[String] mustBe "123/AB12345"
            val fractions = (json \ "fractionCalculations").as[Array[FractionCalculation]]
            val f1 = Seq(Fraction("England", BigDecimal(0.83)))
            val f2 = Seq(Fraction("England", BigDecimal(0.78)))
            val date_1 = LocalDate.of(2016, 12, 23)
            val date_2 = LocalDate.of(2016, 8, 18)
            fractions must contain.atLeastOneOf(FractionCalculation(date_1, f1), FractionCalculation(date_2, f2))
          }

          "?fromDate=2017-08-01&toDate=2017-09-01: return fractions" in {
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/fractions?fromDate=2017-08-01&toDate=2017-09-01")
              .withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            contentType(result) mustBe Some("application/json")
            val json = contentAsJson(result)
            (json \ "empref").as[String] mustBe "123/AB12345"
            val fractions = (json \ "fractionCalculations").as[Array[FractionCalculation]]
            val f1 = Seq(Fraction("England", BigDecimal(0.83)))
            val f2 = Seq(Fraction("England", BigDecimal(0.78)))
            val date_1 = LocalDate.of(2016, 12, 23)
            val date_2 = LocalDate.of(2016, 8, 18)
            fractions must contain.atLeastOneOf(FractionCalculation(date_1, f1), FractionCalculation(date_2, f2))
          }
        }

        "with invalid paramters" must {
          Seq("fromDate", "toDate").foreach { param =>
            s"return 400 when $param param is invalid" in {
              val dates = for {str <- Gen.listOf(Gen.alphaNumChar)} yield str.mkString

              forAll(dates) { (date: String) =>
                whenever(date.nonEmpty) {
                  val requestUrl = param match {
                    case "fromDate" => s"$context/epaye/123%2FAB12345/fractions?fromDate=${helper.urlEncode(date)}&toDate=2015-06-30"
                    case _ => s"/sandbox/epaye/123%2FAB12345/fractions?fromDate=2015-06-03&toDate=${helper.urlEncode(date)}"
                  }
                  val request = FakeRequest(GET, requestUrl).withHeaders(standardDesHeaders()*)

                  val result = route(app, request).get
                  val httpStatus = status(result)

                  httpStatus mustBe 400
                  contentType(result) mustBe Some("application/json")
                  contentAsString(result) must include("""date parameter is in the wrong format. Should be '^(\\d{4})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$' where date format is yyyy-MM-dd and year is 2000 or later.""")
                }
              }
            }
          }

          "return http status 400 when DES HTTP 400" in {
            val request = FakeRequest(GET, s"$context/epaye/400%2FAB12345/fractions")
              .withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe BAD_REQUEST
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_BAD_REQUEST","message":"Bad request error"}""")
          }

          "return http status 401 when DES HTTP 401" in {
            val request = FakeRequest(GET, s"$context/epaye/401%2FAB12345/fractions")
              .withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe 401
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_UNAUTHORIZED","message":"DES unauthorised error"}""")
          }

          "return http status 403 when DES HTTP 403" in {
            val request = FakeRequest(GET, s"$context/epaye/403%2FAB12345/fractions")
              .withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe 403
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_FORBIDDEN","message":"DES forbidden error"}""")
          }

          "return http status 404 when DES HTTP 404" in {
            val request = FakeRequest(GET, s"$context/epaye/404%2FAB12345/fractions")
              .withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe 404
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_NOT_FOUND","message":"DES endpoint or EmpRef not found"}""")
          }

          "return 400 when to date is before from date" in {
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/fractions?fromDate=2015-06-03&toDate=2015-03-30").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get
            val httpStatus = status(result)

            httpStatus mustBe 400
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"BAD_REQUEST","message":"From date was after to date"}""")
          }
        }

        "backend systems failing" must {
          "return http status 503 when connection closed" in {
            val request = FakeRequest(GET, s"$context/epaye/999%2FAB12345/fractions")
              .withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe SERVICE_UNAVAILABLE
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")
          }

          "return http status 408 when timed out" in {
            val request = FakeRequest(GET, s"$context/epaye/777%2FAB12345/fractions")
              .withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe 408
            contentType(result) mustBe Some("application/json")
            contentAsString(result) must include("DES not responding error")
          }

          "return http status 503 when empty response" in {
            val request = FakeRequest(GET, s"$context/epaye/888%2FAB12345/fractions")
              .withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe SERVICE_UNAVAILABLE
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")
          }

          "return http status 503 when DES HTTP 500" in {
            val request = FakeRequest(GET, s"$context/epaye/500%2FAB12345/fractions")
              .withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe SERVICE_UNAVAILABLE
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")
          }

          "return http status 503 when DES HTTP 503" in {
            val request = FakeRequest(GET, s"$context/epaye/503%2FAB12345/fractions")
              .withHeaders(standardDesHeaders()*)
            val result = route(app, request).get

            status(result) mustBe SERVICE_UNAVAILABLE
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")
          }
        }
      }
    }
  }
}
