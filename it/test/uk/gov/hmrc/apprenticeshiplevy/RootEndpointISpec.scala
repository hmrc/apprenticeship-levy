/*
 * Copyright 2024 HM Revenue & Customs
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
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, FORBIDDEN, LENGTH_REQUIRED, NOT_FOUND, OK, REQUEST_TIMEOUT, SERVICE_UNAVAILABLE, TOO_MANY_REQUESTS, UNAUTHORIZED}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsJson, contentType, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.apprenticeshiplevy.util.StubbingData._
import util.WireMockHelper

class RootEndpointISpec
  extends AnyWordSpec
    with GuiceOneAppPerSuite
    with WireMockHelper
    with ScalaCheckPropertyChecks {

  override def fakeApplication(): Application = {
    val conf = wireMockConfiguration(server.port())
    GuiceApplicationBuilder()
      .configure(conf)
      .build()
  }

  "Root Endpoint" when {
    "accessed via sandbox url" should {
      "return valid json" in {
        val request = FakeRequest(GET, "/sandbox/").withHeaders(standardDesHeaders(): _*)

        // test
        val result = route(app, request).get

        // check
        status(result) shouldBe OK
        contentType(result) shouldBe Some("application/hal+json")

        val json = contentAsJson(result)
        (json \ "_links" \ "self" \ "href").as[String] shouldBe "/"
        (json \ "_links" \ "840/MODES17" \ "href").as[String] shouldBe "/epaye/840%2FMODES17"
      }

      "when calling /" when {
        "backend systems not failing" should {
          "return links for each empref" in {
            // set up
            val response = """{"allEnrolments": [{
                             |    "key": "IR-PAYE",
                             |    "identifiers": [{ "key": "TaxOfficeNumber", "value": "123" },
                             |    { "key": "TaxOfficeReference", "value": "AB12345" }],
                             |    "state": "Activated"
                             |  }
                             |  ]}""".stripMargin
           stubPostServerWithId(aResponse()
              .withBody(response),
              "/auth/authorise")
            val request = FakeRequest(GET, s"/").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe OK
            contentType(result) shouldBe Some("application/hal+json")
            val json = contentAsJson(result)
            (json \ "_links" \ "self" \ "href").as[String] shouldBe "/"
            (json \ "_links" \ "123/AB12345" \ "href").as[String] shouldBe "/epaye/123%2FAB12345"
          }
        }

        "errors occur" should {
          "return 401 when Auth returns 401" in {
            // set up
            stubPostServerWithId(aResponse()
              .withStatus(UNAUTHORIZED)
              .withStatusMessage("Not authorised.")
              .withHeader("WWW-Authenticate", "MDTP detail=\"SessionRecordNotFound\""),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe UNAUTHORIZED
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_UNAUTHORIZED","message":"No active session error"}""")
          }

          "return 403 when Auth returns 403" in {
            // set up
            stubPostServerWithId(aResponse()
              .withStatus(FORBIDDEN)
              .withStatusMessage("Forbidden."),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe FORBIDDEN
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_FORBIDDEN","message":"Auth forbidden error"}""")
          }

          "return 503 when Auth returns 404" in {
            // set up
            stubPostServerWithId(aResponse()
              .withStatus(NOT_FOUND)
              .withStatusMessage("Not found."),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_OTHER","message":"Auth 4xx error"}""")
          }
        }

        "backend systems failing" should {
          "return http status 503 when connection closed" in {
            // set up
            stubPostServerWithId(aResponse()
              .withFault(Fault.MALFORMED_RESPONSE_CHUNK),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_IO","message":"Auth connection error"}""")
          }

          "return 503 when returning empty response and connection closed" in {
            // set up
            stubPostServerWithId(aResponse()
              .withFault(Fault.EMPTY_RESPONSE),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_IO","message":"Auth connection error"}""")
          }

          "return 408 when timed out" in {
            // set up
            stubPostServerWithId(aResponse()
              .withStatus(OK)
              .withFixedDelay(1000*60),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe REQUEST_TIMEOUT
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_GATEWAY_TIMEOUT","message":"Auth not responding error"}""")
          }

          "return 503 when Auth returns 503" in {
            // set up
            stubPostServerWithId(aResponse()
              .withStatus(SERVICE_UNAVAILABLE)
              .withStatusMessage("Backend systems failing"),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_BACKEND_FAILURE","message":"Auth 5xx error"}""")
          }

          "return http status 429 when Auth HTTP 429" in {
            // set up
            stubPostServerWithId(aResponse()
              .withStatus(TOO_MANY_REQUESTS)
              .withBody("""{"reason" : "Drowning in requests"}"""),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe TOO_MANY_REQUESTS
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_TOO_MANY_REQUESTS","message":"Auth too many requests"}""")
          }

          "return http status 503 when Auth HTTP 409" in {
            // set up
            stubPostServerWithId(aResponse()
              .withStatus(LENGTH_REQUIRED)
              .withBody("""{"reason" : "Some Auth 411 error"}"""),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_OTHER","message":"Auth 4xx error"}""")
          }

          "return http status 408 when Auth HTTP 408" in {
            // set up
            stubPostServerWithId(aResponse()
              .withStatus(REQUEST_TIMEOUT)
              .withBody("""{"reason" : "Not responding"}"""),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe REQUEST_TIMEOUT
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_TIMEOUT","message":"Auth not responding error"}""")
          }

          "return http status 503 when Auth HTTP 400" in {
            // set up
            stubPostServerWithId(aResponse()
              .withStatus(BAD_REQUEST)
              .withBody("""{"reason" : "Not responding"}"""),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_OTHER","message":"Auth 4xx error"}""")
          }
        }
      }
    }
  }
}
