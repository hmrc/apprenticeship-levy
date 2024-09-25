/*
 * Copyright 2023 HM Revenue & Customs
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

import org.scalatest.matchers.should.Matchers._
import org.scalatest._
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import test.uk.gov.hmrc.apprenticeshiplevy.config.IntegrationTestConfig
import test.uk.gov.hmrc.apprenticeshiplevy.util.AppLevyItUnitSpec

import scala.io.{BufferedSource, Source}
import scala.util.Using

@DoNotDiscover
class DefinitionEndpointISpec extends WiremockFunSpec with ConfiguredServer  {
  def asString(filename: String): String = {
    val fileBuffer: BufferedSource = Source.fromFile(s"$resourcePath/data/expected/$filename")

    Using(fileBuffer) {
      file => file.getLines().mkString("\n")
    }.get
  }

  describe (s"API Definition Endpoint (Private Mode)") {
    describe (s"should when calling $localMicroserviceUrl/api/definition") {
      describe (s"when definition file exists and private-mode is set to true") {
        it (s"return OK") {
          // set up
          val request = FakeRequest(GET, "/api/definition")

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe OK
        }

        it (s"return expected JSON API definition") {
          // set up
          val request = FakeRequest(GET, "/api/definition")

          // test
          val result = route(app, request).get

          // check
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse(asString("definition.json"))
        }

        it (s"return definition with allowlisted applications (still called whitelist in api)") {
          // set up
          val request = FakeRequest(GET, "/api/definition")

          // test
          val result = route(app, request).get

          // check
          contentType(result) shouldBe Some("application/json")
          val json = contentAsJson(result)
          val version1 = (json \ "api" \ "versions")(0)
          (version1 \ "access" \ "type").as[String] shouldBe "PRIVATE"
          (version1 \ "access" \ "whitelistedApplicationIds")(0).as[String] shouldBe "myappid1"
        }
      }
    }
  }
}

@DoNotDiscover
class PublicDefinitionEndpointISpec extends AppLevyItUnitSpec with IntegrationTestConfig with ConfiguredServer {
  def asString(filename: String): String = {
    val fileBuffer: BufferedSource = Source.fromFile(s"$resourcePath/data/expected/$filename")

    Using(fileBuffer) {
      file => file.getLines().mkString("\n")
    }.get
  }
}
