/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.apprenticeshiplevy.sandbox

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox.SandboxEmprefRoutesController
import uk.gov.hmrc.play.test.UnitSpec

class SandboxEmprefRoutesControllerISpec extends UnitSpec with ScalaFutures with IntegrationPatience {
  val testController = new SandboxEmprefRoutesController {
    override def env: String = "Test"
  }

  "getting the routes for a given empref" should {

    "return links for a known empref" in {
      val result = testController.routes("123/AB12345")(FakeRequest().withHeaders("Accept" -> "application/vnd.hmrc.1.0+json")).futureValue

      result.header.status shouldBe OK
      val json = contentAsJson(result)

      (json \ "_links" \ "self" \ "href").as[String] shouldBe "/epaye/123%2FAB12345"
      (json \ "_links" \ "fractions" \ "href").as[String] shouldBe "/epaye/123%2FAB12345/fractions"
      (json \ "_links" \ "declarations" \ "href").as[String] shouldBe "/epaye/123%2FAB12345/declarations"
    }


    "return NOT FOUND for a non known empref" in {
      val result = testController.routes("UNKNOWN")(FakeRequest().withHeaders("Accept" -> "application/vnd.hmrc.1.0+json")).futureValue

      result.header.status shouldBe NOT_FOUND
    }
  }
}
