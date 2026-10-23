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
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsJson, contentType, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import util.StubbingData.*
import util.ISpecBase

class DefinitionEndpointISpec
    extends ISpecBase {

  stubGetServerWithId(aResponse().withStatus(OK), validReadURL1, auuid1)
  stubGetServerWithId(aResponse().withStatus(OK), validReadURL2, auuid2)
  stubGetServerWithId(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK), faultURL1, auuid3)
  stubGetServerWithId(aResponse().withStatus(OK), invalidReadURL1, auuid4)
  stubGetServerWithId(aResponse().withStatus(OK), validRead, auuid5)

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(wireMockConfiguration(server.port()) ++ Map[String, Any]("microservice.private-mode" -> "true"))
      .build()

  "API Definition Endpoint (Private Mode)" when {
    "calling /api/definition" +
    "\nand definition file exists and private-mode is set to true" must {
      "return OK" in {
        val request = FakeRequest(GET, "/api/definition")

        val result = route(app, request).get

        status(result) mustBe OK
      }

      "return expected JSON API definition" in {
        val request = FakeRequest(GET, "/api/definition")

        val result = route(app, request).get

        contentType(result) mustBe Some("application/json")
        contentAsJson(result) mustBe Json.parse(asString("definition.json"))
      }

      "return definition configuration as PRIVATE" in {
        val request = FakeRequest(GET, "/api/definition")

        val result = route(app, request).get

        contentType(result) mustBe Some("application/json")
        val json = contentAsJson(result)
        val version1 = (json \ "api" \ "versions")(0)
        (version1 \ "access" ).as[String] mustBe "INTERNAL"
      }
    }
  }
}
