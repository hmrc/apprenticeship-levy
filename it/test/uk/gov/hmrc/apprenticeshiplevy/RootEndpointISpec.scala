/*
 * Copyright 2019 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock.{status => _, _}
import com.github.tomakehurst.wiremock.http.Fault
import org.scalatest.matchers.should.Matchers._
import org.scalatest._
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

@DoNotDiscover
class RootEndpointISpec extends WiremockFunSpec with ConfiguredServer {
  describe("Root Endpoint") {
    it("when accessed via sandbox url") {
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

    describe (s"should when calling $localMicroserviceUrl/") {
      describe (s"and backend systems not failing") {
        it (s"should return links for each empref") {
          // set up
          val response = """{"allEnrolments": [{
                           |    "key": "IR-PAYE",
                           |    "identifiers": [{ "key": "TaxOfficeNumber", "value": "123" },
                           |    { "key": "TaxOfficeReference", "value": "AB12345" }],
                           |    "state": "Activated"
                           |  }
                           |  ]}""".stripMargin
          stubFor(post(urlEqualTo("/auth/authorise")).withId(uuid).willReturn(aResponse().withBody(response)))
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

      describe ("when errors occur") {
        it (s"should return 401 when Auth returns 401") {
          // set up
          stubFor(post(urlEqualTo("/auth/authorise")).withId(uuid).willReturn(aResponse().withStatus(401).withStatusMessage("Not authorised.").withHeader("WWW-Authenticate", "MDTP detail=\"SessionRecordNotFound\"")))
          val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe 401
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_UNAUTHORIZED","message":"No active session error"}""")
        }

        it (s"should return 403 when Auth returns 403") {
          // set up
          stubFor(post(urlEqualTo("/auth/authorise")).withId(uuid).willReturn(aResponse().withStatus(403).withStatusMessage("Forbidden.")))
          val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe 403
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_FORBIDDEN","message":"Auth forbidden error"}""")
        }

        it (s"should return 503 when Auth returns 404") {
          // set up
          stubFor(post(urlEqualTo("/auth/authorise")).withId(uuid).willReturn(aResponse().withStatus(404).withStatusMessage("Not found.")))
          val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe 503
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_OTHER","message":"Auth 4xx error"}""")
        }
      }

      describe ("when backend systems failing") {
        it ("should return http status 503 when connection closed") {
          // set up
          stubFor(post(urlEqualTo("/auth/authorise")).withId(uuid).willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)))
          val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe SERVICE_UNAVAILABLE
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_IO","message":"Auth connection error"}""")
        }

        it ("should return 503 when returning empty response and connection closed") {
          // set up
          stubFor(post(urlEqualTo("/auth/authorise")).withId(uuid).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)))
          val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe SERVICE_UNAVAILABLE
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_IO","message":"Auth connection error"}""")
        }

        it (s"should return 408 when timed out") {
          // set up
          stubFor(post(urlEqualTo("/auth/authorise")).withId(uuid).willReturn(aResponse().withStatus(200).withFixedDelay(1000*60)))
          val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe 408
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_GATEWAY_TIMEOUT","message":"Auth not responding error"}""")
        }

        it (s"should return 503 when Auth returns 500") {
          // set up
          stubFor(post(urlEqualTo("/auth/authorise")).withId(uuid).willReturn(aResponse().withStatus(500).withStatusMessage("Internal server error")))
          val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe SERVICE_UNAVAILABLE
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_BACKEND_FAILURE","message":"Auth 5xx error"}""")
        }

        it (s"should return 503 when Auth returns 503") {
          // set up
          stubFor(post(urlEqualTo("/auth/authorise")).withId(uuid).willReturn(aResponse().withStatus(503).withStatusMessage("Backend systems failing")))
          val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe SERVICE_UNAVAILABLE
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_BACKEND_FAILURE","message":"Auth 5xx error"}""")
        }

        it (s"should return http status 429 when Auth HTTP 429") {
          // set up
          stubFor(post(urlEqualTo("/auth/authorise")).withId(uuid).willReturn(aResponse().withStatus(429).withBody("""{"reason" : "Drowning in requests"}""")))
          val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe 429
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_TOO_MANY_REQUESTS","message":"Auth too many requests"}""")
        }

        it (s"should return http status 503 when Auth HTTP 409") {
          // set up
          stubFor(post(urlEqualTo("/auth/authorise")).withId(uuid).willReturn(aResponse().withStatus(411).withBody("""{"reason" : "Some Auth 411 error"}""")))
          val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe SERVICE_UNAVAILABLE
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_OTHER","message":"Auth 4xx error"}""")
        }

        it (s"should return http status 408 when Auth HTTP 408") {
          // set up
          stubFor(post(urlEqualTo("/auth/authorise")).withId(uuid).willReturn(aResponse().withStatus(408).withBody("""{"reason" : "Not responding"}""")))
          val request = FakeRequest(GET, "/").withHeaders(standardDesHeaders(): _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe 408
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse("""{"code":"AUTH_ERROR_TIMEOUT","message":"Auth not responding error"}""")
        }

        it (s"should return http status 503 when Auth HTTP 400") {
          // set up
          stubFor(post(urlEqualTo("/auth/authorise")).withId(uuid).willReturn(aResponse().withStatus(400).withBody("""{"reason" : "Not responding"}""")))
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