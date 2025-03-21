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
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, FORBIDDEN, NOT_FOUND, OK, REQUEST_TIMEOUT, SERVICE_UNAVAILABLE, UNAUTHORIZED}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsJson, contentAsString, contentType, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.apprenticeshiplevy.util.StubbingData._
import uk.gov.hmrc.apprenticeshiplevy.util.WireMockHelper

class EmploymentRefEndpointISpec
  extends AnyWordSpec
    with GuiceOneAppPerSuite
    with WireMockHelper
    with ScalaCheckPropertyChecks {

  def stubAuth: StubMapping = {
    val response =
      """{
        |  "allEnrolments": [{
        |    "key": "IR-PAYE",
        |    "identifiers": [
        |      { "key": "TaxOfficeNumber", "value": "123" },
        |      { "key": "TaxOfficeReference", "value": "AB12345" }
        |    ],
        |    "state": "Activated"
        |  }],
        |  "authProviderId": {
        |    "paClientId": "123"
        |  },
        |  "optionalCredentials": {
        |    "providerId": "123",
        |    "providerType": "paClientId"
        |  }
        |}""".stripMargin

    stubPostServerWithId(aResponse().withStatus(OK).withBody(response), "/auth/authorise", auuid6)
  }

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(wireMockConfiguration(server.port()))
      .build()

  "Empref Endpoint" when {
    Seq("/sandbox", "").foreach { context =>
      s"calling $context/epaye/<empref>" when {
        "with valid parameters" should {
          "return the declarations and fractions link for each empref" in {
            stubAuth
            val request = FakeRequest(GET, s"$context/epaye/840%2FMODES17").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) shouldBe OK
            contentType(result) shouldBe Some("application/hal+json")
            val json = contentAsJson(result)
            (json \ "_links" \ "self" \ "href").as[String] shouldBe "/epaye/840%2FMODES17"
            (json \ "_links" \ "fractions" \ "href").as[String] shouldBe "/epaye/840%2FMODES17/fractions"
            (json \ "_links" \ "declarations" \ "href").as[String] shouldBe "/epaye/840%2FMODES17/declarations"
            (json \ "employer" \ "name" \ "nameLine1").as[String] shouldBe "CHANNEL MASTERS BOATING"
            (json \ "communication" \ "name" \ "nameLine1").as[String] shouldBe "CHANNEL MASTERS BOATING"
          }
        }

        "with invalid parameters" should {
          "when DES returns 400, return 400" in {
            stubAuth
            val request = FakeRequest(GET, s"$context/epaye/400%2FAB12345").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) shouldBe BAD_REQUEST
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BAD_REQUEST","message":"Bad request error"}""")
          }

          "when DES returns unauthorized, return 401" in {
            stubAuth
            val request = FakeRequest(GET, s"$context/epaye/401%2FAB12345").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) shouldBe UNAUTHORIZED
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_UNAUTHORIZED","message":"DES unauthorised error"}""")
          }

          "when DES returns forbidden, return 403" in {
            stubAuth
            val request = FakeRequest(GET, s"$context/epaye/403%2FAB12345").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) shouldBe FORBIDDEN
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_FORBIDDEN","message":"DES forbidden error"}""")
          }

          "when DES returns 404, return 404" in {
            stubAuth
            val request = FakeRequest(GET, s"$context/epaye/404%2FAB12345").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) shouldBe NOT_FOUND
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_NOT_FOUND","message":"DES endpoint or EmpRef not found"}""")
          }
        }

        "backend systems failing" should {
          "return 503 when connection closed" in {
            stubAuth
            val request = FakeRequest(GET, s"$context/epaye/999%2FAB12345").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")
          }

          "return 503 when response is empty" in {
            stubAuth
            val request = FakeRequest(GET, s"$context/epaye/888%2FAB12345").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")
          }

          "return 408 when timed out" in {
            stubAuth
            val request = FakeRequest(GET, s"$context/epaye/777%2FAB12345").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) shouldBe REQUEST_TIMEOUT
            contentType(result) shouldBe Some("application/json")
            contentAsString(result) should include("DES not responding error")
          }

          "return 503 when DES returns 500" in {
            stubAuth
            val request = FakeRequest(GET, s"$context/epaye/500%2FAB12345").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")
          }

          "return 503 when DES returns 503" in {
            stubAuth
            val request = FakeRequest(GET, s"$context/epaye/503%2FAB12345").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")
          }

          "return the declarations and fractions link for each empref when employment details does not respond" in {
            stubAuth
            val request = FakeRequest(GET, s"$context/epaye/840%2FMODES18").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            val json = contentAsJson(result)

            status(result) shouldBe OK
            contentType(result) shouldBe Some("application/hal+json")
            (json \ "_links" \ "self" \ "href").as[String] shouldBe "/epaye/840%2FMODES18"
            (json \ "_links" \ "fractions" \ "href").as[String] shouldBe "/epaye/840%2FMODES18/fractions"
            (json \ "_links" \ "declarations" \ "href").as[String] shouldBe "/epaye/840%2FMODES18/declarations"
            (json \ "employer").asOpt[String] shouldBe None
          }

          "return the declarations and fractions link for each empref when communication details does not respond" in {
            stubAuth
            val request = FakeRequest(GET, s"$context/epaye/840%2FMODES19").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            val json = contentAsJson(result)

            status(result) shouldBe OK
            contentType(result) shouldBe Some("application/hal+json")
            (json \ "_links" \ "self" \ "href").as[String] shouldBe "/epaye/840%2FMODES19"
            (json \ "_links" \ "fractions" \ "href").as[String] shouldBe "/epaye/840%2FMODES19/fractions"
            (json \ "_links" \ "declarations" \ "href").as[String] shouldBe "/epaye/840%2FMODES19/declarations"
            (json \ "communication").asOpt[String] shouldBe None
          }
        }
      }
    }
  }
}
