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

import java.time.{LocalDate, LocalDateTime}

import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.apprenticeshiplevy.controllers.live.LiveLevyDeclarationController
import uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference
import play.api.inject.bind
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.apprenticeshiplevy.connectors.LiveDesConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.{FakePrivilegedAuthAction, PrivilegedAuthActionImpl}
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.apprenticeshiplevy.data.des.{ApprenticeshipLevy, EmployerPaymentSummary, EmployerPaymentsSummary}
import uk.gov.hmrc.apprenticeshiplevy.utils.{AppLevyUnitSpec, MockAppContext}
import uk.gov.hmrc.http.NotFoundException

import scala.concurrent.Future

class LevyDeclarationControllerSpec extends AppLevyUnitSpec with ScalaFutures with GuiceOneAppPerSuite
  with Injecting with BeforeAndAfterEach{

  val mockDesConnector = mock[LiveDesConnector]
  val stubComponents = stubControllerComponents()
  val mockAppContext = MockAppContext

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDesConnector)
    mockAppContext.reset()
  }

  override def fakeApplication: Application = GuiceApplicationBuilder()
    .overrides(
      bind[LiveDesConnector].toInstance(mockDesConnector),
      bind[PrivilegedAuthActionImpl].to[FakePrivilegedAuthAction],
      bind[ControllerComponents].toInstance(stubComponents),
      bind[AppContext].toInstance(mockAppContext.mocked)
    )
    .build()

  val liveFractionsController = inject[LiveLevyDeclarationController]

  "getting the levy declarations" should {
    "return a Not Acceptable response" when {
      "the accept header is not correct" in {
        val response = liveFractionsController.declarations(EmploymentReference("empref"), None, None)(FakeRequest()).futureValue
        response.header.status shouldBe NOT_ACCEPTABLE
      }
    }
    "return a fromDateAfterToDate error" when {
      "the supplied fromDate is after the toDate" in {
        val response = liveFractionsController.declarations(EmploymentReference("empref"), Some(LocalDate.parse("2021-01-01")), Some(LocalDate.parse("2020-01-01")))(FakeRequest().withHeaders(
          "ACCEPT"->"application/vnd.hmrc.1.0+json",
          "Authorization"->"Bearer dsfda9080",
          "Environment"->"clone")).futureValue

        response.header.status shouldBe BAD_REQUEST
      }
    }

    "return a success" when {
      "both the fromDate and toDate are supplied" in {
        val appLevy = ApprenticeshipLevy(10.10, 10.10, "4")

        when(mockDesConnector.eps(anyString(), any())(any()))
          .thenReturn(Future.successful(EmployerPaymentsSummary(
            "empref",
            List(EmployerPaymentSummary(
              submissionId = 123456,
              hmrcSubmissionTime = LocalDateTime.now(),
              rtiSubmissionTime = LocalDateTime.now(),
              taxYear = "20-21",
              apprenticeshipLevy = Some(appLevy))))))

        val response = liveFractionsController.declarations(EmploymentReference("empref"), None, None)(FakeRequest().withHeaders(
          "ACCEPT"->"application/vnd.hmrc.1.0+json",
          "Authorization"->"Bearer dsfda9080",
          "Environment"->"clone")).futureValue

        response.header.status shouldBe OK
      }

      "None of the dates are supplied" in {
        val appLevy = ApprenticeshipLevy(10.10, 10.10, "4")

        when(mockDesConnector.eps(anyString(), any())(any()))
          .thenReturn(Future.successful(EmployerPaymentsSummary(
            "empref",
            List(EmployerPaymentSummary(
              submissionId = 123456,
              hmrcSubmissionTime = LocalDateTime.now(),
              rtiSubmissionTime = LocalDateTime.now(),
              taxYear = "20-21",
              apprenticeshipLevy = Some(appLevy))))))

        val response = liveFractionsController.declarations(EmploymentReference("empref"), Some(LocalDate.parse("2020-01-01")), Some(LocalDate.parse("2021-01-01")))(FakeRequest().withHeaders(
          "ACCEPT"->"application/vnd.hmrc.1.0+json",
          "Authorization"->"Bearer dsfda9080",
          "Environment"->"clone")).futureValue

        response.header.status shouldBe OK
      }

      "only the fromDate is supplied" in {
        val appLevy = ApprenticeshipLevy(10.10, 10.10, "4")

        when(mockDesConnector.eps(anyString(), any())(any()))
          .thenReturn(Future.successful(EmployerPaymentsSummary(
            "empref",
            List(EmployerPaymentSummary(
              submissionId = 123456,
              hmrcSubmissionTime = LocalDateTime.now(),
              rtiSubmissionTime = LocalDateTime.now(),
              taxYear = "20-21",
              apprenticeshipLevy = Some(appLevy))))))

        val response = liveFractionsController.declarations(EmploymentReference("empref"), Some(LocalDate.parse("2021-01-01")), None)(FakeRequest().withHeaders(
          "ACCEPT"->"application/vnd.hmrc.1.0+json",
          "Authorization"->"Bearer dsfda9080",
          "Environment"->"clone")).futureValue

        response.header.status shouldBe OK
      }

      "only the toDate is supplied" in {
        val appLevy = ApprenticeshipLevy(10.10, 10.10, "4")

        when(mockDesConnector.eps(anyString(), any())(any()))
          .thenReturn(Future.successful(EmployerPaymentsSummary(
            "empref",
            List(EmployerPaymentSummary(
              submissionId = 123456,
              hmrcSubmissionTime = LocalDateTime.now(),
              rtiSubmissionTime = LocalDateTime.now(),
              taxYear = "20-21",
              apprenticeshipLevy = Some(appLevy))))))

        val response = liveFractionsController.declarations(EmploymentReference("empref"), None, Some(LocalDate.parse("2021-01-01")))(FakeRequest().withHeaders(
          "ACCEPT"->"application/vnd.hmrc.1.0+json",
          "Authorization"->"Bearer dsfda9080",
          "Environment"->"clone")).futureValue

        response.header.status shouldBe OK
      }
    }

    "return a notFound" when {
      "no data is found" in {

        when(mockDesConnector.eps(anyString(), any())(any()))
          .thenReturn(Future.successful(EmployerPaymentsSummary(
            "empref",
            List(EmployerPaymentSummary(
              submissionId = 123456,
              hmrcSubmissionTime = LocalDateTime.now(),
              rtiSubmissionTime = LocalDateTime.now(),
              taxYear = "20-21")))))

        val response = liveFractionsController.declarations(EmploymentReference("empref"), Some(LocalDate.parse("2020-01-01")), Some(LocalDate.parse("2021-01-01")))(FakeRequest().withHeaders(
          "ACCEPT"->"application/vnd.hmrc.1.0+json",
          "Authorization"->"Bearer dsfda9080",
          "Environment"->"clone")).futureValue

        response.header.status shouldBe NOT_FOUND
      }

      "the desConnector returns a 404" in {

        when(mockDesConnector.eps(anyString(), any())(any()))
          .thenReturn(Future.failed(new NotFoundException("Data Not Found")))

        val response = liveFractionsController.declarations(EmploymentReference("empref"), Some(LocalDate.parse("2020-01-01")), Some(LocalDate.parse("2021-01-01")))(FakeRequest().withHeaders(
          "ACCEPT"->"application/vnd.hmrc.1.0+json",
          "Authorization"->"Bearer dsfda9080",
          "Environment"->"clone")).futureValue

        response.header.status shouldBe NOT_FOUND
      }
    }
  }
}

