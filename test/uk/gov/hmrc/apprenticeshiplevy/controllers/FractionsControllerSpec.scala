/*
 * Copyright 2023 HM Revenue & Customs
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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.ControllerComponents
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.{FakePrivilegedAuthAction, PrivilegedAuthActionImpl}
import uk.gov.hmrc.apprenticeshiplevy.controllers.live.LiveFractionsController
import uk.gov.hmrc.apprenticeshiplevy.data.api._
import uk.gov.hmrc.apprenticeshiplevy.utils.{AppLevyUnitSpec, MockAppContext}
import uk.gov.hmrc.http.{HttpClient, UpstreamErrorResponse}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import java.time.LocalDate

import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import uk.gov.hmrc.apprenticeshiplevy.data.des.Fractions

import scala.concurrent.Future

class FractionsControllerSpec extends AppLevyUnitSpec with ScalaFutures with GuiceOneAppPerSuite with Injecting {

  val stubComponents: ControllerComponents = stubControllerComponents()
  val mockHttp: HttpClient = mock[HttpClient]
  val mockAuditConnector: AuditConnector = mock[AuditConnector]

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .overrides(
      bind[ControllerComponents].toInstance(stubComponents),
      bind[AppContext].toInstance(MockAppContext.mocked),
      bind[HttpClient].toInstance(mockHttp),
      bind[AuditConnector].toInstance(mockAuditConnector),
      bind[PrivilegedAuthActionImpl].to[FakePrivilegedAuthAction]
    ).build()


  lazy val liveFractionsController: LiveFractionsController = inject[LiveFractionsController]

  "getting the fractions" should {
    "return a success if the submitted request is valid" in {
      val fromDate: LocalDate = LocalDate.parse("2020-07-22")
      val toDate: LocalDate = LocalDate.parse("2021-07-22")

      when(mockHttp.GET[Either[UpstreamErrorResponse, Fractions]](anyString(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Right(Fractions("empref", List()))))

      val response = liveFractionsController.fractions(EmploymentReference("empref"), Some(fromDate), Some(toDate))(FakeRequest().withHeaders(
        "ACCEPT"->"application/vnd.hmrc.1.0+json",
        "Authorization"->"Bearer dsfda9080",
        "Environment"->"clone"))

      response.header.status shouldBe OK
    }

    "return a Not Acceptable response if the Accept header is not correctly set" in {
      val response = liveFractionsController.fractions(EmploymentReference("empref"), None, None)(FakeRequest()).futureValue
      response.header.status shouldBe NOT_ACCEPTABLE
    }
    "return a fromDateAfterToDate error if the submitted fromDate is after the toDate" in {
      val fromDate: LocalDate = LocalDate.parse("2021-07-22")
      val toDate: LocalDate = LocalDate.parse("2020-07-22")

      val response = liveFractionsController.fractions(EmploymentReference("empref"), Some(fromDate), Some(toDate))(FakeRequest().withHeaders(
        "ACCEPT"->"application/vnd.hmrc.1.0+json",
        "Authorization"->"Bearer dsfda9080",
        "Environment"->"clone")).futureValue

      response.header.status shouldBe BAD_REQUEST
    }
  }

  "validating fromDate" should {
    "should use default value if fromDate is omitted" in {
      liveFractionsController.validateFromDate(None) shouldBe LocalDate.now().minusMonths(liveFractionsController.defaultPriorMonthsForFromDate)
    }
    "use date if supplied" in {
      val date: LocalDate = LocalDate.parse("2013-07-22")
      liveFractionsController.validateToDate(Some(date)) shouldBe date
    }
  }

  "validating toDate" should {
    "should use default value if toDate is omitted" in {
      liveFractionsController.validateToDate(None) shouldBe LocalDate.now()
    }
    "use date if supplied" in {
      val date: LocalDate = LocalDate.parse("2010-08-03")
      liveFractionsController.validateToDate(Some(date)) shouldBe date
    }
  }
}
