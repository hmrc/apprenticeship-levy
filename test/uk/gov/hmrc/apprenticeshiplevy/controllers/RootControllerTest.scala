/*
 * Copyright 2017 HM Revenue & Customs
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
import uk.gov.hmrc.apprenticeshiplevy.connectors.AuthConnector
import uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference

class RootControllerTest extends WordSpecLike with Matchers with OptionValues {
  "transformEmprefs" should {
    "correctly generate HAL for emprefs" in {
      val hal = testController.transformEmpRefs(Seq("123/AB12345", "321/XY54321"))

      hal.links.links.length shouldBe 3
      hal.links.links.find(_.rel == "self").value.href shouldBe "/"
      hal.links.links.find(_.rel == "123/AB12345").value.href shouldBe "/epaye/123%2FAB12345"
      hal.links.links.find(_.rel == "321/XY54321").value.href shouldBe "/epaye/321%2FXY54321"
    }
  }


  val testController = new RootController {
    override def rootUrl: String = "/"

    override def authConnector: AuthConnector = ???

    override def emprefUrl(ref: EmploymentReference): String = s"""/epaye/${URLEncoder.encode(ref.empref, "UTF-8")}"""
  }

}
