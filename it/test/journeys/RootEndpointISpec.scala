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
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsJson, contentType, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import util.StubbingData.*
import util.ISpecBase

class RootEndpointISpec
  extends ISpecBase {

  "Root Endpoint" when {
    "accessed via sandbox url" must {
      "return valid json" in {
        val request = FakeRequest(GET, "/sandbox/").withHeaders(standardDesHeaders()*)

        val result = route(app, request).get

        status(result) mustBe OK
        contentType(result) mustBe Some("application/json")

        val json = contentAsJson(result)
        (json \ "_links" \ "self" \ "href").as[String] mustBe "/"
        (json \ "_links" \ "840/MODES17" \ "href").as[String] mustBe "/epaye/840%2FMODES17"
      }

      "when calling /" when {
        "backend systems not failing" must {
          "return links for each empref" in {
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
            val request = FakeRequest(GET, s"/").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe OK
            contentType(result) mustBe Some("application/json")
            val json = contentAsJson(result)
            (json \ "_links" \ "self" \ "href").as[String] mustBe "/"
            (json \ "_links" \ "123/AB12345" \ "href").as[String] mustBe "/epaye/123%2FAB12345"
          }
        }

        "errors occur" must {
          "return 401 when Auth returns 401" in {
            stubPostServerWithId(aResponse()
              .withStatus(UNAUTHORIZED)
              .withStatusMessage("Not authorised.")
              .withHeader("WWW-Authenticate", "MDTP detail=\"SessionRecordNotFound\""),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe UNAUTHORIZED
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"AUTH_ERROR_UNAUTHORIZED","message":"No active session error"}""")
          }

          "return 403 when Auth returns 403" in {
            stubPostServerWithId(aResponse()
              .withStatus(FORBIDDEN)
              .withStatusMessage("Forbidden."),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe FORBIDDEN
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"AUTH_ERROR_FORBIDDEN","message":"Auth forbidden error"}""")
          }

          "return 503 when Auth returns 404" in {
            stubPostServerWithId(aResponse()
              .withStatus(NOT_FOUND)
              .withStatusMessage("Not found."),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe SERVICE_UNAVAILABLE
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"AUTH_ERROR_OTHER","message":"Auth 4xx error"}""")
          }
        }

        "backend systems failing" must {
          "return http status 503 when connection closed" in {
            stubPostServerWithId(aResponse()
              .withFault(Fault.MALFORMED_RESPONSE_CHUNK),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe SERVICE_UNAVAILABLE
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"AUTH_ERROR_IO","message":"Auth connection error"}""")
          }

          "return 503 when returning empty response and connection closed" in {
            stubPostServerWithId(aResponse()
              .withFault(Fault.EMPTY_RESPONSE),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe SERVICE_UNAVAILABLE
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"AUTH_ERROR_IO","message":"Auth connection error"}""")
          }

          "return 408 when timed out" in {
            stubPostServerWithId(aResponse()
              .withStatus(OK)
              .withFixedDelay(1000*60),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe REQUEST_TIMEOUT
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"AUTH_ERROR_GATEWAY_TIMEOUT","message":"Auth not responding error"}""")
          }

          "return 503 when Auth returns 503" in {
            stubPostServerWithId(aResponse()
              .withStatus(SERVICE_UNAVAILABLE)
              .withStatusMessage("Backend systems failing"),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe SERVICE_UNAVAILABLE
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"AUTH_ERROR_BACKEND_FAILURE","message":"Auth 5xx error"}""")
          }

          "return http status 429 when Auth HTTP 429" in {
            stubPostServerWithId(aResponse()
              .withStatus(TOO_MANY_REQUESTS)
              .withBody("""{"reason" : "Drowning in requests"}"""),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe TOO_MANY_REQUESTS
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"AUTH_ERROR_TOO_MANY_REQUESTS","message":"Auth too many requests"}""")
          }

          "return http status 503 when Auth HTTP 409" in {
            stubPostServerWithId(aResponse()
              .withStatus(LENGTH_REQUIRED)
              .withBody("""{"reason" : "Some Auth 411 error"}"""),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe SERVICE_UNAVAILABLE
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"AUTH_ERROR_OTHER","message":"Auth 4xx error"}""")
          }

          "return http status 408 when Auth HTTP 408" in {
            stubPostServerWithId(aResponse()
              .withStatus(REQUEST_TIMEOUT)
              .withBody("""{"reason" : "Not responding"}"""),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe REQUEST_TIMEOUT
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"AUTH_ERROR_TIMEOUT","message":"Auth not responding error"}""")
          }

          "return http status 503 when Auth HTTP 400" in {
            stubPostServerWithId(aResponse()
              .withStatus(BAD_REQUEST)
              .withBody("""{"reason" : "Not responding"}"""),
              "/auth/authorise")
            val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe SERVICE_UNAVAILABLE
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"AUTH_ERROR_OTHER","message":"Auth 4xx error"}""")
          }
        }
      }
    }
  }
}
