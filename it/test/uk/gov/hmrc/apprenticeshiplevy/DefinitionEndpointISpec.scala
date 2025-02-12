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
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsJson, contentType, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.apprenticeshiplevy.util.StubbingData._
import uk.gov.hmrc.apprenticeshiplevy.util.WireMockHelper

class DefinitionEndpointISpec
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
      .configure(wireMockConfiguration(server.port()) ++ Map[String, Any]("microservice.private-mode" -> "true"))
      .build()

  "API Definition Endpoint (Private Mode)" when {
    "calling /api/definition" +
    "\nand definition file exists and private-mode is set to true" should {
      "return OK" in {
        // set up
        val request = FakeRequest(GET, "/api/definition")

        // test
        val result = route(app, request).get

        // check
        status(result) shouldBe OK
      }

      "return expected JSON API definition" in {
        // set up
        val request = FakeRequest(GET, "/api/definition")

        // test
        val result = route(app, request).get

        // check
        contentType(result) shouldBe Some("application/json")
        contentAsJson(result) shouldBe Json.parse(asString("definition.json"))
      }

      "return definition configuration as PRIVATE" in {
        // set up
        val request = FakeRequest(GET, "/api/definition")

        // test
        val result = route(app, request).get

        // check
        contentType(result) shouldBe Some("application/json")
        val json = contentAsJson(result)
        val version1 = (json \ "api" \ "versions")(0)
        (version1 \ "access" \ "type").as[String] shouldBe "PRIVATE"
      }
    }
  }
}
