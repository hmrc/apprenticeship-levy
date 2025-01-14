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
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, FORBIDDEN, INTERNAL_SERVER_ERROR, NOT_FOUND, OK, SERVICE_UNAVAILABLE}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsJson, contentAsString, contentType, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.apprenticeshiplevy.data.api.{LevyDeclaration, PayrollPeriod}
import uk.gov.hmrc.apprenticeshiplevy.util.StubbingData._
import util.WireMockHelper
import views.html.helper

import java.time.{LocalDate, LocalDateTime}

class DeclarationsEndpointISpec
  extends AnyWordSpec
    with GuiceOneAppPerSuite
    with WireMockHelper
    with ScalaCheckPropertyChecks {

  val sixteenToSeventeen = "16-17"

  stubGetServerWithId(aResponse().withStatus(OK), validReadURL1, auuid1)
  stubGetServerWithId(aResponse().withStatus(OK), validReadURL2, auuid2)
  stubGetServerWithId(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK), faultURL1, auuid3)
  stubGetServerWithId(aResponse().withStatus(OK), invalidReadURL1, auuid4)
  stubGetServerWithId(aResponse().withStatus(OK), validRead, auuid5)

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(wireMockConfiguration(server.port()))
      .build()

  "Declarations Endpoint" when {
    val contexts = Seq("/sandbox", "")
    contexts.foreach { context =>
      s"calling $context/epaye/<empref>/declarations with valid paramters" should {
        "return levy declarations" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/123%2FAB12341/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          contentType(result) shouldBe Some("application/json")

          val json = contentAsJson(result)
          (json \ "empref").as[String] shouldBe "123/AB12341"
          val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
          declarations.length shouldBe 9
          info(declarations.mkString("\n"))
        }

        "return levy declarations where only fromDate specified" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/123%2FAB12342/declarations?fromDate=2016-10-12").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          contentType(result) shouldBe Some("application/json")

          val json = contentAsJson(result)
          (json \ "empref").as[String] shouldBe "123/AB12342"
          val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
          declarations.length shouldBe 2
        }

        "return levy declarations where only toDate specified" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/123%2FAB12343/declarations?toDate=2016-02-21").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          contentType(result) shouldBe Some("application/json")

          val json = contentAsJson(result)
          (json \ "empref").as[String] shouldBe "123/AB12343"
          val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
          declarations.length shouldBe 5
        }

        "return levy declarations where fromDate and toDate are specified" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/123%2FAB12344/declarations?fromDate=2016-05-19&toDate=2016-05-21").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          contentType(result) shouldBe Some("application/json")

          val json = contentAsJson(result)
          (json \ "empref").as[String] shouldBe "123/AB12344"
          val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
          declarations.length shouldBe 1
        }


        "handle no payment period" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/123%2FAB12341/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          contentType(result) shouldBe Some("application/json")

          val json = contentAsJson(result)
          (json \ "empref").as[String] shouldBe "123/AB12341"
          val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
          declarations(2) shouldBe LevyDeclaration(567742487410L,
            LocalDateTime.parse("2016-04-20T14:25:32.000"),
            None,
            None,
            None,
            Some(PayrollPeriod(sixteenToSeventeen, 8)),
            None,
            None,
            Some(true),
            56774248741L)
        }

        "handle inactive period" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/123%2FAB12341/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          contentType(result) shouldBe Some("application/json")

          val json = contentAsJson(result)
          (json \ "empref").as[String] shouldBe "123/AB12341"
          val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
          declarations(7) shouldBe LevyDeclaration(65732154551L,
            LocalDateTime.parse("2016-04-20T14:25:32.000"),
            None,
            Some(LocalDate.parse("2016-08-06")),
            Some(LocalDate.parse("2016-11-05")),
            None,
            None,
            None,
            None,
            6573215455L)
        }

        "handle ceased trading" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/123%2FAB12341/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          contentType(result) shouldBe Some("application/json")

          val json = contentAsJson(result)
          (json \ "empref").as[String] shouldBe "123/AB12341"
          val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
          declarations(6) shouldBe LevyDeclaration(567742487423L,
            LocalDateTime.parse("2016-04-20T14:25:32.000"),
            Some(LocalDate.parse("2016-06-06")),
            None,
            None,
            None,
            None,
            None,
            None, 56774248742L)
        }

        "handle levies" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/123%2FAB12341/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          contentType(result) shouldBe Some("application/json")

          val json = contentAsJson(result)
          (json \ "empref").as[String] shouldBe "123/AB12341"
          val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
          declarations(0) shouldBe LevyDeclaration(567742487432L,
            LocalDateTime.parse("2016-05-20T14:25:32.000"),
            None,
            None,
            None,
            Some(PayrollPeriod(sixteenToSeventeen, 2)),
            Some(98.64),
            Some(15000),
            None,
            56774248743L)
          declarations(3) shouldBe LevyDeclaration(567742487412L,
            LocalDateTime.parse("2016-04-20T14:25:32.000"),
            None,
            None,
            None,
            Some(PayrollPeriod(sixteenToSeventeen, 11)),
            Some(24.27),
            Some(15000),
            None,
            56774248741L)
          declarations(5) shouldBe LevyDeclaration(567742487422L,
            LocalDateTime.parse("2016-04-20T14:25:32.000"),
            None,
            None,
            None,
            Some(PayrollPeriod(sixteenToSeventeen, 11)),
            Some(24.27),
            Some(15000),
            None,
            56774248742L)
          declarations(8) shouldBe LevyDeclaration(65732154552L,
            LocalDateTime.parse("2016-04-20T14:25:32.000"),
            None,
            None,
            None,
            Some(PayrollPeriod(sixteenToSeventeen, 2)),
            Some(124.27),
            Some(15000),
            None,
            6573215455L)
        }

        "handle bad timestamps returned from DES in levies" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/123%2FAB88888/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          contentType(result) shouldBe Some("application/json")

          val json = contentAsJson(result)
          (json \ "empref").as[String] shouldBe "123/AB88888"
          val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
          declarations(0) shouldBe LevyDeclaration(567742487410L,
            LocalDateTime.parse("2013-04-17T09:00:55"),
            None,
            None,
            None,
            Some(PayrollPeriod(sixteenToSeventeen, 8)),
            None,
            None,
            Some(true),
            56774248741L)
        }
      }

      s"calling $context/epaye/<empref>/declarations with invalid paramters" should {
        Seq("fromDate", "toDate").foreach { param =>
          s"return 400 when $param param is invalid" in {
            // set up
            val dates = for {str <- Gen.listOf(Gen.alphaNumChar)} yield str.mkString

            forAll(dates) { (date: String) =>
              whenever(date.nonEmpty) {
                val requestUrl = param match {
                  case "fromDate" => s"$context/epaye/123%2FAB12345/declarations?fromDate=${helper.urlEncode(date)}&toDate=2015-06-30"
                  case _ => s"/sandbox/epaye/123%2FAB12345/declarations?fromDate=2015-06-03&toDate=${helper.urlEncode(date)}"
                }
                val request = FakeRequest(GET, requestUrl).withHeaders(standardDesHeaders(): _*)

                // test
                val result = route(app, request).get
                val httpStatus = status(result)

                // check
                httpStatus shouldBe BAD_REQUEST
                contentType(result) shouldBe Some("application/json")
                contentAsString(result) should include (""" date parameter is in the wrong format. Should be '^(\\d{4})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$' where date format is yyyy-MM-dd and year is 2000 or later.""")
              }
            }
          }
        }

        "return 400 when empref is unknown" in {
          // set up
          val emprefs = for {empref <- genEmpref} yield empref

          forAll(emprefs) { (empref: String) =>
            whenever(empref.nonEmpty) {
              val request = FakeRequest(GET, s"$context/epaye/${helper.urlEncode(empref)}/declarations").withHeaders(standardDesHeaders(): _*)

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

        "return 400 when DES returns 400" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/400%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe BAD_REQUEST
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BAD_REQUEST","message":"Bad request error"}""")
        }

        "return http status 401 when DES returns 401" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/401%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result)
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_UNAUTHORIZED","message":"DES unauthorised error"}""")
        }

        "return http status 403 when DES returns 403" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/403%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe FORBIDDEN
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_FORBIDDEN","message":"DES forbidden error"}""")
        }

        "return 404 when DES returns 404" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/404%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe NOT_FOUND
        }

        "return 400 when to date is before from date" in {
          // set up
          val request =
            FakeRequest(GET, s"$context/epaye/123%2FAB12345/declarations?fromDate=2015-06-03&toDate=2015-03-30")
              .withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get
          val httpStatus = status(result)

          // check
          httpStatus shouldBe BAD_REQUEST
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"BAD_REQUEST","message":"From date was after to date"}""")
        }
      }

      s"calling $context/epaye/<empref>/declarations when backend systems failing" +
        "\nbut not as per specification dated 07/11/2016" should {

        "return http status 404 when empty json returned" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/999%2FAB999999/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe NOT_FOUND
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"NOT_FOUND","message":"Resource was not found"}""")
        }

        "return http status 500 when error json object returned on Http Status 200" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/999%2FAB999998/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe INTERNAL_SERVER_ERROR
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES backend error"}""")
        }

        "return http status 404 when just empref field in Json object is returned" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/999%2FAB999997/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe NOT_FOUND
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"NOT_FOUND","message":"Resource was not found"}""")
        }

        "return eps if using old fieldname of 'declarations'" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/999%2FAB999996/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe OK
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse(
            """{"empref":"999/AB999996","declarations":[
              |{"id":65732154551,"submissionTime":"2016-10-20T14:25:32.000",
              |"inactiveFrom":"2016-08-06",
              |"inactiveTo":"2016-11-05",
              |"submissionId":6573215455},
              |{"id":65732154552,
              |"submissionTime":"2016-10-20T14:25:32.000",
              |"payrollPeriod":{"year":"16-17","month":1},
              |"levyDueYTD":124.27,
              |"levyAllowanceForFullYear":15000,
              |"submissionId":6573215455}]}""".stripMargin)
        }
      }

      s"calling $context/epaye/<empref>/declarations when backend systems failing" should {

        "return 503 when connection closed" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/999%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe SERVICE_UNAVAILABLE
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")
        }

        "return http status 503 when empty response" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/888%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe SERVICE_UNAVAILABLE
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")
        }

        "should return http status 408 when timed out" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/777%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe 408
          contentType(result) shouldBe Some("application/json")
          contentAsString(result) should include("""{"code":"DES_ERROR_GATEWAY_TIMEOUT","message":"DES not responding error""")
        }

        "return http status 503 when DES HTTP 500" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/500%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe SERVICE_UNAVAILABLE
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")
        }

        "return http status 503 when DES HTTP 503" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/503%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe SERVICE_UNAVAILABLE
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")
        }

        "return http status 404 when empty json returned" in {
          // set up
          val request = FakeRequest(GET, s"$context/epaye/123%2FAB99999/declarations").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe NOT_FOUND
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"NOT_FOUND","message":"Resource was not found"}""")
        }
      }
    }
  }
}
