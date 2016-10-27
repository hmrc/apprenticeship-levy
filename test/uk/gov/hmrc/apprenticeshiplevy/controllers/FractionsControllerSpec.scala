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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apprenticeshiplevy.controllers.live.LiveFractionsController
import uk.gov.hmrc.play.test.UnitSpec

class FractionsControllerSpec extends UnitSpec with ScalaFutures {
  "getting the fractions" should {

    "return a Not Acceptable response if the Accept header is not correctly set" in {
      val response = LiveFractionsController.fractions("empref", None, None)(FakeRequest()).futureValue
      response.header.status shouldBe NOT_ACCEPTABLE
    }
  }
}
