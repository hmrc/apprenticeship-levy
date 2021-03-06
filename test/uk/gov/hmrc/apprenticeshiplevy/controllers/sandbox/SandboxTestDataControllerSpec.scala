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

package uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox

import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.ControllerComponents
import play.api.test.Helpers.stubControllerComponents
import play.api.{Configuration, Environment}
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox.data.SandboxTestDataController
import uk.gov.hmrc.apprenticeshiplevy.utils.DataTransformer
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class SandboxTestDataControllerSpec extends PlaySpec with MockitoSugar {

  def controller(dummyResponse: Boolean) = {
    val servicesConfig = mock[ServicesConfig]
    val configuration = Configuration("features.returnDummyResponse" -> dummyResponse)
    val environment = mock[Environment]
    val mockAppContext = new AppContext(servicesConfig, configuration, environment)
    val stubComponents: ControllerComponents = stubControllerComponents()

    new SandboxTestDataController(new DataTransformer(), mockAppContext, stubComponents)
  }

  "getFileName" must {
    "replace file EmpRef with 840/MODES17" when {
      "file matches EmpRef regex and returnDummyResponse is true" in {
        val inputEmpRef = "EMPREF"
        val file = "/sandbox/data/apprenticeship-levy/employers/%s/employed/SC111111A?fromDate=2017-01-01&toDate=2017-12-31"
        val result = controller(true).getFileName(file.format(inputEmpRef))
        result mustBe file.format("840MODES17")
      }

      "file matches EmpRef regex, EmpRef has / and returnDummyResponse is true" in {
        val (empRefStart, empRefEnd) = ("EMP", "REF")
        val file = "/sandbox/data/paye/employer/%s/%s/designatory-details/communication"
        val result = controller(true).getFileName(file.format(empRefStart, empRefEnd))
        result mustBe file.format("840", "MODES17")
      }
    }

    "not replace file EmpRef" when {
      "file does not match regex" in {
        val file = "/unmatched/file/840MODES17/EMPREF"
        controller(true).getFileName(file) mustBe file
      }

      "returnDummyResponse is false" in {
        val file = "/sandbox/data/paye/employer/EMP/REF/designatory-details/communication"
        controller(false).getFileName(file) mustBe file
      }
    }

    "replace nino file" when {
      "file matches nino regex" in {
        val inputNino = "AA000001A"
        val file = "/sandbox/data/apprenticeship-levy/employers/840MODES17/employed/%s?fromDate=2017-01-01&toDate=2017-12-31"
        val result = controller(true).getFileName(file.format(inputNino))
        result mustBe file.format("SC111111A")
      }
    }

    "replace both empref and nino" when {
      "file matches both regex and returnDummyResponse is true" in {
        val inputEmpRef = "EMPREF"
        val inputNino = "AA000001A"
        val file = "/sandbox/data/apprenticeship-levy/employers/%s/employed/%s?fromDate=2017-01-01&toDate=2017-12-31"
        val result = controller(true).getFileName(file.format(inputEmpRef, inputNino))
        result mustBe file.format("840MODES17", "SC111111A")
      }
    }
  }
}
