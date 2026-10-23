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

import org.scalatest.wordspec.AnyWordSpec
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import play.api.http.Status.{NOT_FOUND, OK}
import util.StubbingData.*
import util.ISpecBase

class TestDataEndpointISpec
  extends ISpecBase {
  
  "Sandbox Test Data Endpoint" when {
    "authorise/read" must {
      "return Ok" in {
        val request = FakeRequest(GET, "/sandbox/data/authorise/read")
          .withHeaders(standardDesHeaders()*)

        val result = route(app, request).get

        status(result) mustBe OK
      }
    }

    "/sandbox/data/..." must {
      "return Ok where json file exists" in {
        val request = FakeRequest(GET, "/sandbox/data/paye/employer/840/MODES17/designatory-details")
          .withHeaders(standardDesHeaders()*)

        val result = route(app, request).get

        status(result) mustBe OK
        contentAsString(result) must include ("/paye/employer/840/MODES17/designatory-details/employer")
      }

      "return NotFound where json file does not exists" in {
        val request = FakeRequest(GET, "/sandbox/data/zxy")
          .withHeaders(standardDesHeaders()*)

        val result = route(app, request).get

        status(result) mustBe NOT_FOUND
      }

      "return Ok where json file does not have json body" in {
        val request = FakeRequest(GET, "/sandbox/data/empty")
          .withHeaders(standardDesHeaders()*)

        val result = route(app, request).get

        status(result) mustBe OK
      }

      "return support OVERRIDE_EMPREF" must {
        "return json file where it exists" in {
          val headers = standardDesHeaders() :+ "OVERRIDE_EMPREF"->"840/MODES17"
          val request = FakeRequest(GET, "/sandbox/data/paye/employer/000/ABC/designatory-details")
            .withHeaders(headers*)

          val result = route(app, request).get

          status(result) mustBe OK
          contentAsString(result) must include ("/paye/employer/840/MODES17/designatory-details/employer")
        }

        "return NotFound where json file does not exist" in {
          val headers = standardDesHeaders() :+ "OVERRIDE_EMPREF"->"ZZZ%2FJKLJLJL"
          val request = FakeRequest(GET, "/sandbox/data/paye/employer/840/MODES17/designatory-details")
            .withHeaders(headers*)

          val result = route(app, request).get

          status(result) mustBe NOT_FOUND
        }
      }
    }
  }
}
