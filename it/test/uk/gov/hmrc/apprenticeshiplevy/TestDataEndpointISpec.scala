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

import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.apprenticeshiplevy.util.StubbingData._
import util.WireMockHelper

class TestDataEndpointISpec
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

  "Sandbox Test Data Endpoint" when {
    "authorise/read" should {
      "return Ok" in {
        // set up
        val request = FakeRequest(GET, "/sandbox/data/authorise/read")
          .withHeaders(standardDesHeaders()*)

        // test
        val result = route(app, request).get

        // check
        status(result) shouldBe OK
      }
    }

    "/sandbox/data/..." should {
      "return Ok where json file exists" in {
        // set up
        val request = FakeRequest(GET, "/sandbox/data/paye/employer/840/MODES17/designatory-details")
          .withHeaders(standardDesHeaders()*)

        // test
        val result = route(app, request).get

        // check
        status(result) shouldBe OK
        contentAsString(result) should include ("/paye/employer/840/MODES17/designatory-details/employer")
      }

      "return NotFound where json file does not exists" in {
        // set up
        val request = FakeRequest(GET, "/sandbox/data/zxy")
          .withHeaders(standardDesHeaders()*)

        // test
        val result = route(app, request).get

        // check
        status(result) shouldBe NOT_FOUND
      }

      "return Ok where json file does not have json body" in {
        // set up
        val request = FakeRequest(GET, "/sandbox/data/empty")
          .withHeaders(standardDesHeaders()*)

        // test
        val result = route(app, request).get

        // check
        status(result) shouldBe OK
      }

      "return support OVERRIDE_EMPREF" should {
        "return json file where it exists" in {
          // set up
          val headers = standardDesHeaders() :+ "OVERRIDE_EMPREF"->"840/MODES17"
          val request = FakeRequest(GET, "/sandbox/data/paye/employer/000/ABC/designatory-details")
            .withHeaders(headers*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe OK
          contentAsString(result) should include ("/paye/employer/840/MODES17/designatory-details/employer")
        }

        "return NotFound where json file does not exist" in {
          // set up
          val headers = standardDesHeaders() :+ "OVERRIDE_EMPREF"->"ZZZ%2FJKLJLJL"
          val request = FakeRequest(GET, "/sandbox/data/paye/employer/840/MODES17/designatory-details")
            .withHeaders(headers*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe NOT_FOUND
        }
      }
    }
  }
}
