/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.apprenticeshiplevy

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.http.Fault
import org.scalacheck.Gen
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, OK, REQUEST_TIMEOUT, SERVICE_UNAVAILABLE}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsJson, contentAsString, contentType, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.apprenticeshiplevy.util.StubbingData._
import uk.gov.hmrc.apprenticeshiplevy.util.WireMockHelper
import views.html.helper

class EmploymentCheckEndpointISpec
  extends AnyWordSpec
    with GuiceOneAppPerSuite
    with WireMockHelper
    with ScalaCheckPropertyChecks {

  stubGetServerWithId(aResponse().withStatus(OK), validReadURL1, auuid1)
  stubGetServerWithId(aResponse().withStatus(OK), validReadURL2, auuid2)
  stubGetServerWithId(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK), faultURL1, auuid3)
  stubGetServerWithId(aResponse().withStatus(OK), invalidReadURL1, auuid4)
  stubGetServerWithId(aResponse().withStatus(OK), validRead, auuid5)

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(wireMockConfiguration(server.port()))
      .build()

  "Employment Check Endpoint" when {
    Seq("/sandbox", "").foreach { context =>
      s"calling $context/epaye/<empref>/employed/<nino>" when {
        "with valid parameters" should {
          "?fromDate=2015-03-03&toDate=2015-06-30: return 'employed'" in {
            // set up
            val request =
              FakeRequest(GET, s"$context/epaye/123%2FAB12345/employed/AA123456A?fromDate=2016-03-03&toDate=2016-06-30")
              .withHeaders(standardDesHeaders()*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            json shouldBe Json.parse("""{"empref":"123/AB12345","nino":"AA123456A","fromDate":"2016-03-03","toDate":"2016-06-30","employed":true}""")
          }

          "?fromDate=2016-03-03&toDate=2016-06-30: return 'not recognised'" in {
            // set up
            val request =
              FakeRequest(GET, s"$context/epaye/123%2FAB12345/employed/BB123456A?fromDate=2016-03-03&toDate=2016-06-30")
                .withHeaders(standardDesHeaders()*)

            // test
            val result = route(app, request).get

            // check
            info(contentAsString(result))
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            json shouldBe Json.parse("""{"code":"EPAYE_UNKNOWN","message":"The provided NINO or EMPREF was not recognised"}""")
          }

          "?fromDate=2015-03-03&toDate=2015-06-30: return 'not employed'" in {
            // set up
            val request =
              FakeRequest(GET, s"$context/epaye/123%2FAB12345/employed/EE123456A?fromDate=2016-03-03&toDate=2016-06-30")
                .withHeaders(standardDesHeaders()*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            json shouldBe Json.parse("""{"empref":"123/AB12345","nino":"EE123456A","fromDate":"2016-03-03","toDate":"2016-06-30","employed":false}""")
          }
        }

        "with invalid parameters" should {
          "return 400 when empref is badly formatted" in {
            // set up
            val emprefs = for { empref <- genEmpref } yield empref

            forAll(emprefs) { (empref: String) =>
              whenever(empref.nonEmpty) {
                val request =
                  FakeRequest(GET, s"$context/epaye/${helper.urlEncode(empref)}/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30")
                    .withHeaders(standardDesHeaders()*)

                // test
                val result = route(app, request).get
                val httpStatus = status(result)

                // check
                httpStatus shouldBe BAD_REQUEST
                contentType(result) shouldBe Some("application/json")
                contentAsString(result) should include("""is in the wrong format. Should be ^\\d{3}/[0-9A-Z]{1,10}$ and url encoded."""")
              }
            }
          }

          "should return 400 when nino is badly formatted" in {
            // set up

            // This list of inputs is based on the information obtained from: https://en.wikipedia.org/wiki/National_Insurance_number#Format
            val invalidNationalInsuranceNumbers = List(
              "12345", // Only 5 characters, all numbers
              "FXYZ", // Only 4 characters, all capital letters
              "bot", // Only 3 characters, all small letters
              "4A7B", // Only 4 characters, mixture of numbers and capital letters
              "7a5b", // Only 4 characters, mixture of numbers and small letters
              "A5b33Cd", // Only 7 characters, mixture of numbers, capital letters and small letters
              "hp010203b", // All characters are valid and format is correct but the letters are small
              "5S010203D", // First character is a number
              "@S010203D", // First character is special
              "D4010203D", // Second character is a number
              "D@010203D", // Second character is special
              "DS010203D", // First letter is D
              "TD010203D", // Second letter is D
              "FS010203D", // First letter is F
              "TF010203D", // Second letter is F
              "IS010203D", // First letter is I
              "TI010203D", // Second letter is I
              "QS010203D", // First letter is Q
              "TQ010203D", // Second letter is Q
              "US010203D", // First letter is U
              "TU010203D", // Second letter is U
              "VS010203D", // First letter is V
              "TV010203D", // Second letter is V
              "TO010203D", // Second letter is O
              "77010203D", // First two characters are numbers
              "?@010203D", // First two characters are special
              "BG010203D", // First two letters are BG
              "GB010203D", // First two letters are GB
              "KN010203D", // First two letters are KN
              "NK010203D", // First two letters are NK
              "NT010203D", // First two letters are NT
              "TN010203D", // First two letters are TN
              "ZZ010203D", // First two letters are ZZ
              "HP0102037", // Last character is a number
              "HP010203@", // Last character is special
              "HP010203Z", // Last character is not the letter A, B, C or D
              "HP@!<>?~B", // All digit characters are special
              "123456789", // All 9 characters are numbers
              "?<>~`@£!+", // All 9 characters are special
              "ABCDEFGHI", // All 9 characters are capital letters
              "jklmnopqr", // All 9 characters are small letters
            )

            for (invalidNationalInsuranceNumber <- invalidNationalInsuranceNumbers) {
              val request =
                FakeRequest(GET, s"$context/epaye/444%2FAB12345/employed/${helper.urlEncode(invalidNationalInsuranceNumber)}" +
                  s"?fromDate=2015-03-03&toDate=2015-06-30")
                  .withHeaders(standardDesHeaders()*)

              // test
              val result = route(app, request).get
              val httpStatus = status(result)

              // check
              httpStatus shouldBe BAD_REQUEST
              contentType(result) shouldBe Some("application/json")
              contentAsString(result) should include("""is in the wrong format. Should have a prefix (one of """)
            }
          }

          Seq("fromDate", "toDate").foreach { param =>
            s"return 400 when $param param is invalid" in {
              // set up
              val dates = for {str <- Gen.listOf(Gen.alphaNumChar)} yield str.mkString

              forAll(dates) { (date: String) =>
                whenever(date.nonEmpty) {
                  val requestUrl = param match {
                    case "fromDate" => s"$context/epaye/123%2FAB12345/employed/RA123456C?fromDate=${helper.urlEncode(date)}&toDate=2015-06-30"
                    case _ => s"/sandbox/epaye/123%2FAB12345/employed/RA123456C?fromDate=2015-06-03&toDate=${helper.urlEncode(date)}"
                  }
                  val request = FakeRequest(GET, requestUrl).withHeaders(standardDesHeaders()*)

                  // test
                  val result = route(app, request).get
                  val httpStatus = status(result)

                  // check
                  httpStatus shouldBe BAD_REQUEST
                  contentType(result) shouldBe Some("application/json")
                  contentAsString(result) should include(
                    """date parameter is in the wrong format. Should be
                      |'^(\\d{4})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$'
                      |where date format is yyyy-MM-dd and year is 2000 or later.""".stripMargin)
                }
              }
            }
          }

          "return 400 when to date is before from date" in {
            // set up
            val request =
              FakeRequest(GET, s"$context/epaye/400%2FAB12345/employed/RA123456C?fromDate=2015-06-03&toDate=2015-03-30")
              .withHeaders(standardDesHeaders()*)

            // test
            val result = route(app, request).get
            val httpStatus = status(result)

            // check
            httpStatus shouldBe BAD_REQUEST
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"BAD_REQUEST","message":"From date was after to date"}""")
          }

          "return 400 when DES returns 400" in {
            // set up
            val request =
              FakeRequest(GET, s"$context/epaye/400%2FAB12345/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30")
                .withHeaders(standardDesHeaders()*)

            // test
            val result = route(app, request).get

            // check
            val httpStatus = status(result)
            httpStatus shouldBe BAD_REQUEST
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BAD_REQUEST","message":"Bad request error"}""")
          }

          "return http status 401 when DES returns 40" in {
            // set up
            val request =
              FakeRequest(GET, s"$context/epaye/401%2FAB12345/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30")
                .withHeaders(standardDesHeaders()*)

            // test
            val result = route(app, request).get

            // check
            val httpStatus = status(result)
            httpStatus shouldBe 401
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_UNAUTHORIZED","message":"DES unauthorised error"}""")
          }

          "return http status 403 when DES returns 403" in {
            // set up
            val request =
              FakeRequest(GET, s"$context/epaye/403%2FAB12345/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30")
                .withHeaders(standardDesHeaders()*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 403
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_FORBIDDEN","message":"DES forbidden error"}""")
          }
        }

        "backend systems failing" should {
          "return 503 when connection closed" in {
            // set up
            val request =
              FakeRequest(GET, s"$context/epaye/999%2FAB12345/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30")
                .withHeaders(standardDesHeaders()*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")
          }

          "return http status 503 when empty response" in {
            // set up
            val request =
              FakeRequest(GET, s"$context/epaye/888%2FAB12345/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30")
                .withHeaders(standardDesHeaders()*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")
          }

          "return http status 408 when timed out" in {
            // set up
            val request =
              FakeRequest(GET, s"$context/epaye/777%2FAB12345/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30")
                .withHeaders(standardDesHeaders()*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe REQUEST_TIMEOUT
            contentType(result) shouldBe Some("application/json")
            contentAsString(result) should include ("DES not responding error")
          }

          "return http status 503 when DES HTTP 500" in {
            // set up
            val request =
              FakeRequest(GET, s"$context/epaye/500%2FAB12345/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30")
                .withHeaders(standardDesHeaders()*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")
          }

          "return http status 503 when DES HTTP 503" in {
            // set up
            val request =
              FakeRequest(GET, s"$context/epaye/503%2FAB12345/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30")
                .withHeaders(standardDesHeaders()*)

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
