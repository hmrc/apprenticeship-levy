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

package uk.gov.hmrc.apprenticeshiplevy.controllers

import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec

class LevyDeclarationControllerSpec extends UnitSpec with ScalaFutures {

  "getting the levy declarations" should {
    "return a Not Acceptable response if the Accept header is not correctly set" in {
      val response = LevyDeclarationController.declarations("empref", None)(FakeRequest()).futureValue
      response.header.status shouldBe NOT_ACCEPTABLE
    }

    "return an HTTP Not Implemented response" in {
      val response = LevyDeclarationController.declarations("empref", None)(FakeRequest().withHeaders("Accept" -> "application/vnd.hmrc.1.0+json")).futureValue
      response.header.status shouldBe NOT_IMPLEMENTED
    }
  }
}


