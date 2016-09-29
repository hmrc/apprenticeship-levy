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
import uk.gov.hmrc.apprenticeshiplevy.connectors.EpayeConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox.SandboxEmprefController
import uk.gov.hmrc.apprenticeshiplevy.data.epaye.{DesignatoryDetails, DesignatoryDetailsData, HodName}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, NotFoundException}
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatest.DoNotDiscover
import scala.concurrent.{ExecutionContext, Future}

@DoNotDiscover
class SandboxEmprefControllerISpec extends UnitSpec with ScalaFutures with IntegrationPatience {


  "getting the empref details" should {
    "return the declarations and fractions link for each empref" in {
      val result = testController.empref("123/AB12345")(FakeRequest().withHeaders("Accept" -> "application/vnd.hmrc.1.0+json")).futureValue
      result.header.status shouldBe OK
      val json = contentAsJson(result)

      (json \ "_links" \ "self" \ "href").as[String] shouldBe "/epaye/123%2FAB12345"
      (json \ "_links" \ "fractions" \ "href").as[String] shouldBe "/epaye/123%2FAB12345/fractions"
      (json \ "_links" \ "declarations" \ "href").as[String] shouldBe "/epaye/123%2FAB12345/declarations"
    }

    "return NOT FOUND for a non known empref" in {
      val result = testController.empref("UNKNOWN")(FakeRequest().withHeaders("Accept" -> "application/vnd.hmrc.1.0+json")).futureValue

      result.header.status shouldBe NOT_FOUND
    }
  }

  lazy val dummyEpayeConnector = new EpayeConnector {
    override def epayeBaseUrl: String = ???

    override def http: HttpGet = ???

    override def designatoryDetails(empref: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DesignatoryDetails] =
      if (empref == "123/AB12345") Future.successful(DesignatoryDetails(Some(empref), Some(DesignatoryDetailsData(Some(HodName(Some("Foo Bar Ltd."), None)), None, None)), None))
      else Future.failed(new NotFoundException(empref))
  }

  lazy val testController = new SandboxEmprefController {
    override def env: String = "Test"

    override def epayeConnector: EpayeConnector = dummyEpayeConnector
  }

}
