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
import org.scalacheck.Gen
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import util.StubbingData.*
import util.ISpecBase

import java.io.File
import scala.io.{BufferedSource, Source}
import scala.util.Using

class DocumentationEndpointISpec
  extends ISpecBase {  

  stubGetServerWithId(aResponse().withStatus(OK), validReadURL1, auuid1)
  stubGetServerWithId(aResponse().withStatus(OK), validReadURL2, auuid2)
  stubGetServerWithId(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK), faultURL1, auuid3)
  stubGetServerWithId(aResponse().withStatus(OK), invalidReadURL1, auuid4)
  stubGetServerWithId(aResponse().withStatus(OK), validRead, auuid5)

  "API Documentation Endpoint" must {
    "provide YAML documentation" when {
      Seq("1.0").foreach { version =>
        s"/api/conf/$version/application.yaml is defined" in {
          val request = FakeRequest(GET, s"/api/conf/$version/application.yaml")

          val result = route(app, request).get

          status(result) mustBe OK
          contentAsString(result).startsWith("openapi: 3.0.3") mustBe true
        }

        val definitionFile = new File(s"./public/api/conf/$version/application.yaml")
        val fileBuffer: BufferedSource = Source.fromFile(definitionFile)

        val includes = Using(fileBuffer) {
          file => file.getLines().filter(_.contains("!include")).map(line=>line.substring(line.indexOf("!include "))).toList.filterNot(_.matches(".*(errors|versioning).md")).toSet
        }.get

        includes.zipWithIndex.foreach { case (include, _) =>
          s"and serve file for $include" in {
            val file = include.substring(9)
            val request = FakeRequest(GET, s"/api/conf/$version/$file")

            val result = route(app, request).get

            status(result) mustBe OK
          }
        }
      }
    }

    "should when calling /api/definition" must {
      "have correct definitions configured" in {
        val request = FakeRequest(GET, s"/api/definition")

        val result = route(app, request).get

        status(result) mustBe OK
      }
    }

    "with invalid parameters" must {
      "return 404 when documentation version doesn't exist" in {
        val urls = for {version <- Gen.choose(Int.MinValue, Int.MaxValue)} yield s"/api/documentation/$version/empref"

        forAll(urls) { (url: String) =>
          val request = FakeRequest(GET, url)

          val documentationResult = route(app, request).get
          val httpStatus = status(documentationResult)

          httpStatus mustBe NOT_FOUND
        }
      }

      "return 404 when documentation endpoint doesn't exist" in {
        val endpoints = for { endpoint <- Gen.alphaStr } yield endpoint

        forAll(endpoints) { (endpoint: String) =>
          whenever (endpoint.nonEmpty && endpoint != "/") {
            val url = s"/api/documentation/1.0/$endpoint"
            val request = FakeRequest(GET, url)

            val documentationResult = route(app, request).get
            val httpStatus = status(documentationResult)

            httpStatus mustBe NOT_FOUND
          }
        }
      }
    }
  }
}
