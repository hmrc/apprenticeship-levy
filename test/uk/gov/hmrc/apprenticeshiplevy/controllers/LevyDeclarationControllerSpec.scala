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
import uk.gov.hmrc.apprenticeshiplevy.connectors.{ETMPConnector, ETMPLevyDeclarations, TaxYear}
import uk.gov.hmrc.apprenticeshiplevy.controllers.live.LiveLevyDeclarationController
import uk.gov.hmrc.apprenticeshiplevy.data.charges.{Charge, Charges}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class LevyDeclarationControllerSpec extends UnitSpec with ScalaFutures {
  "getting the levy declarations" should {
    "return a Not Acceptable response if the Accept header is not correctly set" in {
      val response = LiveLevyDeclarationController.declarations("empref", None)(FakeRequest()).futureValue
      response.header.status shouldBe NOT_ACCEPTABLE
    }

    "return an HTTP Not Implemented response" in {
      val response = LiveLevyDeclarationController.declarations("empref", None)(FakeRequest().withHeaders("Accept" -> "application/vnd.hmrc.1.0+json")).futureValue
      response.header.status shouldBe NOT_IMPLEMENTED
    }
  }

  "isLevyCharge" should {
    "return true if chargeType contains 'APPRENTICESHIP LEVY'" in {
      val charge = Charge("RTI Spec Charge-APPRENTICESHIP LEVY", "FPS", Seq())

      TestLevyDeclarationController.isLevyCharge(charge) shouldBe true
    }
    "return false if chargeType does not contain 'APPRENTICESHIP LEVY'" in {
      val charge = Charge("RTI Spec Charge-TAX", "FPS", Seq())

      TestLevyDeclarationController.isLevyCharge(charge) shouldBe false
    }
  }

  "originalOrAmended" should {
    "return 'original'" in {
      val charge = Charge("", "FPS", Seq())
      TestLevyDeclarationController.originalOrAmended(charge) shouldBe "original"
    }

    "return 'amended'" in {
      val charge = Charge("", "EYU", Seq())
      TestLevyDeclarationController.originalOrAmended(charge) shouldBe "amended"
    }
  }
}

object TestETMPConnector extends ETMPConnector {
  override def etmpBaseUrl: String = ???

  override def httpGet: HttpGet = ???

  override def charges(empref: String, taxYear: TaxYear)(implicit hc: HeaderCarrier): Future[Charges] = ???
}

object TestLevyDeclarationController extends LevyDeclarationController with ApiController {
  override def etmpConnector: ETMPConnector = ???
}
