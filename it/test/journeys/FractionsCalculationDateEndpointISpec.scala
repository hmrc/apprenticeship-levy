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

class FractionsCalculationDateEndpointISpec
  extends ISpecBase {

  "Fractions Calculation Date Endpoint" when {
    Seq("/sandbox", "").foreach { context =>
      s"calling $context/fraction-calculation-date" when {
        "no backend systems failing" must {
          "return date" in {
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe OK
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse(""""2016-03-15"""")
          }
        }

        "backend systems failing" must {
          "return http status 503 when connection closed" in {
            stubGetServerWithId(aResponse()
              .withFault(Fault.MALFORMED_RESPONSE_CHUNK),
              "/apprenticeship-levy/fraction-calculation-date")
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe SERVICE_UNAVAILABLE
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")
          }

          "return http status 408 when timed out" in {
            stubGetServerWithId(aResponse()
              .withStatus(OK)
              .withFixedDelay(1000 * 60),
              "/apprenticeship-levy/fraction-calculation-date")
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe REQUEST_TIMEOUT
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_GATEWAY_TIMEOUT","message":"DES not responding error"}""")
          }

          "return http status 503 when empty response" in {
            stubGetServerWithId(aResponse()
              .withFault(Fault.EMPTY_RESPONSE),
              "/apprenticeship-levy/fraction-calculation-date")
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe SERVICE_UNAVAILABLE
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")
          }

          "return http status 404 when DES HTTP 404" in {
            stubGetServerWithId(aResponse()
              .withStatus(NOT_FOUND)
              .withBody("""{"reason" : "Not found"}"""),
              "/apprenticeship-levy/fraction-calculation-date")
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe NOT_FOUND
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_NOT_FOUND","message":"DES endpoint or EmpRef not found"}""")
          }

          "return http status 503 when DES HTTP 500" in {
            stubGetServerWithId(aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("""{"reason" : "DES not working"}"""),
              "/apprenticeship-levy/fraction-calculation-date")
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe SERVICE_UNAVAILABLE
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")
          }

          "return http status 503 when DES HTTP 503" in {
            stubGetServerWithId(aResponse()
              .withStatus(SERVICE_UNAVAILABLE)
              .withBody("""{"reason" : "Backend systems not working"}"""),
              "/apprenticeship-levy/fraction-calculation-date")
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe SERVICE_UNAVAILABLE
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")
          }

          "return http status 401 when DES HTTP 401" in {
            stubGetServerWithId(aResponse()
              .withStatus(UNAUTHORIZED)
              .withBody("""{"reason" : "Not authorized"}"""),
              "/apprenticeship-levy/fraction-calculation-date")
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe 401
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_UNAUTHORIZED","message":"DES unauthorised error"}""")
          }

          "return http status 403 when DES HTTP 403" in {
            stubGetServerWithId(aResponse()
              .withStatus(FORBIDDEN)
              .withBody("""{"reason" : "Forbidden"}"""),
              "/apprenticeship-levy/fraction-calculation-date")
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe FORBIDDEN
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_FORBIDDEN","message":"DES forbidden error"}""")
          }

          "return http status 429 when DES HTTP 429" in {
            stubGetServerWithId(aResponse()
              .withStatus(TOO_MANY_REQUESTS)
              .withBody("""{"reason" : "Drowning in requests"}"""),
              "/apprenticeship-levy/fraction-calculation-date")
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe TOO_MANY_REQUESTS
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_TOO_MANY_REQUESTS","message":"DES too many requests"}""")
          }

          "return http status 408 when DES HTTP 408" in {
            stubGetServerWithId(aResponse()
              .withStatus(REQUEST_TIMEOUT)
              .withBody("""{"reason" : "Not responding"}"""),
              "/apprenticeship-levy/fraction-calculation-date")
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe REQUEST_TIMEOUT
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_TIMEOUT","message":"DES not responding error"}""")
          }

          "return http status 503 when DES HTTP 409" in {
            stubGetServerWithId(aResponse()
              .withStatus(CONFLICT)
              .withBody("""{"reason" : "Some 4xxx error"}"""),
              "/apprenticeship-levy/fraction-calculation-date")
            val request = FakeRequest(GET, s"$context/fraction-calculation-date").withHeaders(standardDesHeaders()*)

            val result = route(app, request).get

            status(result) mustBe SERVICE_UNAVAILABLE
            contentType(result) mustBe Some("application/json")
            contentAsJson(result) mustBe Json.parse("""{"code":"DES_ERROR_OTHER","message":"DES 4xx error"}""")
          }
        }
      }
    }
  }
}
