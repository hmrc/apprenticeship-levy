/*
 * Copyright 2022 HM Revenue & Customs
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

import org.scalatest.wordspec.AnyWordSpecLike

import java.net.URLEncoder
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues
import play.api.mvc.BodyParsers.Default
import play.api.mvc.{AnyContent, BodyParser, ControllerComponents}
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.{AuthAction, FakeAuthAction}
import uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference

import scala.concurrent.ExecutionContext

class RootControllerSpec extends AnyWordSpecLike with Matchers with OptionValues{

  val stubComponents: ControllerComponents = stubControllerComponents()

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

    override def controllerComponents: ControllerComponents = stubComponents

    override def executionContext: ExecutionContext = stubComponents.executionContext

    override def parser: BodyParser[AnyContent] = stubComponents.parsers.default

    override def emprefUrl(ref: EmploymentReference): String = s"""/epaye/${URLEncoder.encode(ref.empref, "UTF-8")}"""

    override val authAction: AuthAction = new FakeAuthAction(new Default(stubComponents.parsers), stubComponents.executionContext)
  }

}
