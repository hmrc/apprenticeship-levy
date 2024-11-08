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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.apprenticeshiplevy.util.StubbingData._
import uk.gov.hmrc.apprenticeshiplevy.util.WireMockHelper

class PrivateDefinitionEndpointISpec
  extends AnyWordSpec
    with GuiceOneAppPerSuite
    with WireMockHelper
    with ScalaCheckPropertyChecks {

  def stubbedConfigPath: String = sys.props.getOrElse("WIREMOCK_MAPPINGS", "./it/no-mappings")

  override protected val server: WireMockServer = new WireMockServer(wireMockConfig.usingFilesUnderDirectory(stubbedConfigPath).dynamicPort())

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(wireMockConfiguration(server.port())
        - "microservice.private-mode"
        - "microservice.allowlisted-applications"
        ++ Map[String, Any](
        "microservice.private-mode" -> "true",
        "microservice.allowlisted-applications" -> "none")
      )
      .build()

  "API Definition Endpoint (Public Mode)" when {
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
    }
  }
}
