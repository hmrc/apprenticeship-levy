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

import java.time.LocalDate

import com.codahale.metrics.MetricRegistry
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import play.api.mvc.{AnyContent, BodyParser, ControllerComponents}
import play.api.test.Helpers.stubControllerComponents
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.apprenticeshiplevy.connectors.DesConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.{AuthAction, FakePrivilegedAuthAction}
import uk.gov.hmrc.apprenticeshiplevy.data.api.{EmploymentReference, Nino}
import uk.gov.hmrc.apprenticeshiplevy.data.des.{EmploymentCheckStatus, Unknown}
import uk.gov.hmrc.apprenticeshiplevy.utils.AppLevyUnitSpec
import uk.gov.hmrc.http.{HttpClient, UpstreamErrorResponse}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.{ExecutionContext, Future}

class EmploymentCheckControllerSpec extends AppLevyUnitSpec with ScalaFutures with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach {

  val stubComponents: ControllerComponents = stubControllerComponents()
  val mockHttp: HttpClient = mock[HttpClient]
  val mockAppContext: AppContext = mock[AppContext]

  val testController: EmploymentCheckController = new EmploymentCheckController {
    override implicit val executionContext: ExecutionContext = controllerComponents.executionContext

    def desConnector: DesConnector = new DesConnector() {
      override val appContext: AppContext = mockAppContext
      override protected def auditConnector: Option[AuditConnector] = None
      override def registry: Option[MetricRegistry] = None
      def baseUrl: String = "http://a.guide.to.nowhere/"
      def httpClient: HttpClient = mockHttp
      override def desAuthorization: String = "localBearer"

      override def desEnvironment: String = "localEnv"
    }

    override val authAction: AuthAction = new FakePrivilegedAuthAction
    override val appContext: AppContext = mockAppContext

    override def parser: BodyParser[AnyContent] = controllerComponents.parsers.default

    override protected def controllerComponents: ControllerComponents = stubComponents
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAppContext)
    reset(mockHttp)
  }

  "checking employment" should {
    "return a success response" when {
      "the user is employed" in {
        val fromDate: LocalDate = LocalDate.parse("2020-07-22")
        val toDate: LocalDate = LocalDate.parse("2021-07-22")
        val nino = Nino("AA12345A")

        when(mockHttp.GET[Either[UpstreamErrorResponse, EmploymentCheckStatus]](anyString(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(Right(EmploymentCheckStatus(true))))

        val response = await(testController.check(EmploymentReference("empref"), nino, fromDate, toDate)(FakeRequest().withHeaders(
          "ACCEPT"->"application/vnd.hmrc.1.0+json",
          "Authorization"->"Bearer dsfda9080",
          "Environment"->"clone")))

        status(response) shouldBe OK
      }
      "the user is not employed" in {
        val fromDate: LocalDate = LocalDate.parse("2020-07-22")
        val toDate: LocalDate = LocalDate.parse("2021-07-22")
        val nino = Nino("AA12345A")

        when(mockHttp.GET[Either[UpstreamErrorResponse, EmploymentCheckStatus]](anyString(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(Right(EmploymentCheckStatus(false))))

        val response = await(testController.check(EmploymentReference("empref"), nino, fromDate, toDate)(FakeRequest().withHeaders(
          "ACCEPT"->"application/vnd.hmrc.1.0+json",
          "Authorization"->"Bearer dsfda9080",
          "Environment"->"clone")))

        status(response) shouldBe OK
      }
    }
    "return an error" when {
      "the user's employment is unknown" in {
        val fromDate: LocalDate = LocalDate.parse("2020-07-22")
        val toDate: LocalDate = LocalDate.parse("2021-07-22")
        val nino = Nino("AA12345A")

        when(mockHttp.GET[Either[UpstreamErrorResponse, EmploymentCheckStatus]](anyString(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(Right(Unknown)))

        val response = await(testController.check(EmploymentReference("empref"), nino, fromDate, toDate)(FakeRequest().withHeaders(
          "ACCEPT"->"application/vnd.hmrc.1.0+json",
          "Authorization"->"Bearer dsfda9080",
          "Environment"->"clone")))

        status(response) shouldBe NOT_FOUND

      }
      "the supplied fromDate is after the toDate" in {

        val fromDate: LocalDate = LocalDate.parse("2021-07-22")
        val toDate: LocalDate = LocalDate.parse("2020-07-22")
        val nino = Nino("AA12345A")

        when(mockHttp.GET[Either[UpstreamErrorResponse, EmploymentCheckStatus]](anyString(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(Right(EmploymentCheckStatus(true))))

        val response = await(testController.check(EmploymentReference("empref"), nino, fromDate, toDate)(FakeRequest().withHeaders(
          "ACCEPT"->"application/vnd.hmrc.1.0+json",
          "Authorization"->"Bearer dsfda9080",
          "Environment"->"clone")))

        status(response) shouldBe BAD_REQUEST

      }
    }
  }
}
