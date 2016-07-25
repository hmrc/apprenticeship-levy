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

import java.net.URLEncoder

import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import uk.gov.hmrc.apprenticeshiplevy.connectors.{AuthConnector, EpayeConnector}

class EmprefControllerTest extends WordSpecLike with Matchers with OptionValues {
  "prepareLinks" should {
    "correctly prepare HAL for an empref" in {
      val empref = "123/AB12345"
      val hal = testController.prepareLinks(empref)

      hal.links.links should have size 4
      hal.links.links.find(_.rel == "self").value.href shouldBe testController.emprefUrl(empref)
      hal.links.links.find(_.rel == "declarations").value.href shouldBe testController.declarationsUrl(empref)
      hal.links.links.find(_.rel == "fractions").value.href shouldBe testController.fractionsUrl(empref)
      hal.links.links.find(_.rel == "employment-check").value.href shouldBe testController.employmentCheckUrl(empref)
    }
  }

  val testController = new EmprefController {
    override def epayeConnector: EpayeConnector = ???

    override def emprefUrl(empref: String): String = s"""/epaye/${URLEncoder.encode(empref, "UTF-8")}"""

    override def declarationsUrl(empref: String): String = emprefUrl(empref) + "/declarations"

    override def fractionsUrl(empref: String): String = emprefUrl(empref) + "/fractions"

    override def employmentCheckUrl(empref: String): String = emprefUrl(empref) + "/employed/{nino}"
  }

}
