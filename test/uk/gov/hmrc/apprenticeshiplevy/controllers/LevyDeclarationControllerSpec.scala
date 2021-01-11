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

import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apprenticeshiplevy.connectors._
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.{AuthAction, FakePrivilegedAuthAction}
import uk.gov.hmrc.apprenticeshiplevy.controllers.live.LiveLevyDeclarationController
import uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference
import uk.gov.hmrc.apprenticeshiplevy.utils.DateRange
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.test.UnitSpec

class LevyDeclarationControllerSpec extends UnitSpec with ScalaFutures with MockitoSugar {
  val liveFractionsController = new LiveLevyDeclarationController(new LiveDesConnector(mock[HttpGet]), FakePrivilegedAuthAction)

  "getting the levy declarations" should {
    "return a Not Acceptable response if the Accept header is not correctly set" in {
      val response = liveFractionsController.declarations(EmploymentReference("empref"), None, None)(FakeRequest()).futureValue
      response.header.status shouldBe NOT_ACCEPTABLE
    }
  }
}

object TestDesConnector extends DesConnector {
  override def baseUrl: String = ???

  override def httpGet: HttpGet = ???

  override def eps(empref: String, dateRange: DateRange)(implicit hc: HeaderCarrier, ec: scala.concurrent.ExecutionContext) = ???

  protected def auditConnector: Option[AuditConnector] = None
}

object TestLevyDeclarationController extends LevyDeclarationController with DesController {
  override def desConnector: DesConnector = TestDesConnector
  override val authAction: AuthAction = FakePrivilegedAuthAction
}
