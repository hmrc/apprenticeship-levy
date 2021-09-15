/*
 * Copyright 2021 HM Revenue & Customs
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
import org.scalatest.OptionValues
import play.api.mvc.{AnyContent, BodyParser, ControllerComponents}
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.apprenticeshiplevy.connectors.DesConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.{AuthAction, FakePrivilegedAuthAction}
import uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference
import uk.gov.hmrc.apprenticeshiplevy.utils.AppLevyUnitSpec

import scala.concurrent.ExecutionContext

class EmprefControllerSpec extends AppLevyUnitSpec with OptionValues {

  val stubComponents: ControllerComponents = stubControllerComponents()
  val mockAppContext: AppContext = mock[AppContext]

  "prepareLinks" should {
    "correctly prepare HAL for an empref" in {
      val empref = "123/AB12345"
      val hal = testController.prepareLinks(EmploymentReference(empref))

      hal.links.links should have size 4
      hal.links.links.find(_.rel == "self").value shouldBe testController.emprefUrl(EmploymentReference(empref))
      hal.links.links.find(_.rel == "declarations").value shouldBe testController.declarationsUrl(EmploymentReference(empref))
      hal.links.links.find(_.rel == "fractions").value shouldBe testController.fractionsUrl(EmploymentReference(empref))
      hal.links.links.find(_.rel == "employment-check").value shouldBe testController.employmentCheckUrl(EmploymentReference(empref))
    }
  }

  val testController = new EmprefController {

    override val appContext: AppContext = mockAppContext

    override def controllerComponents: ControllerComponents = stubComponents

    override def executionContext: ExecutionContext = controllerComponents.executionContext

    override def parser: BodyParser[AnyContent] = controllerComponents.parsers.default

    override def desConnector: DesConnector = ???

    override def emprefUrl(ref: EmploymentReference): String = s"""/epaye/${URLEncoder.encode(ref.empref, "UTF-8")}"""

    override def declarationsUrl(ref: EmploymentReference): String = emprefUrl(ref) + "/declarations"

    override def fractionsUrl(ref: EmploymentReference): String = emprefUrl(ref) + "/fractions"

    override def employmentCheckUrl(ref: EmploymentReference): String = emprefUrl(ref) + "/employed/{nino}"

    override val authAction: EmploymentReference => AuthAction = _ => new FakePrivilegedAuthAction
  }

}
