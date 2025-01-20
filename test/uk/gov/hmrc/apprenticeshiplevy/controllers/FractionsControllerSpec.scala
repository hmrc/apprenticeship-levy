/*
 * Copyright 2025 HM Revenue & Customs
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

import com.codahale.metrics.MetricRegistry
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{AnyContent, BodyParser, ControllerComponents}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.apprenticeshiplevy.connectors.DesConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.{AuthAction, FakePrivilegedAuthAction}
import uk.gov.hmrc.apprenticeshiplevy.data.api._
import uk.gov.hmrc.apprenticeshiplevy.data.des.Fractions
import uk.gov.hmrc.apprenticeshiplevy.utils.AppLevyUnitSpec
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class FractionsControllerSpec extends AppLevyUnitSpec with ScalaFutures with GuiceOneAppPerSuite with Injecting {

  val stubComponents: ControllerComponents = stubControllerComponents()
  val mockHttp: HttpClientV2 = mock[HttpClientV2]
  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val mockRequestBuilder = mock[RequestBuilder]
  val mockAppContext: AppContext = mock[AppContext]

  def liveFractionsController: FractionsController = new FractionsController with DesController {

    override def desConnector: DesConnector = new DesConnector() {
      def httpClient: HttpClientV2 = mockHttp
      def baseUrl: String = "http://a.guide.to.nowhere/"
      override def desAuthorization: String = "localBearer"
      override def desEnvironment: String = "localEnv"
      override protected def auditConnector: Option[AuditConnector] = None
      override val appContext: AppContext = mockAppContext
      override def registry: Option[MetricRegistry] = None
    }

    override val authAction: AuthAction = new FakePrivilegedAuthAction

    override val appContext: AppContext = mockAppContext

    override protected def controllerComponents: ControllerComponents = stubComponents

    override def parser: BodyParser[AnyContent] = controllerComponents.parsers.default

    override protected def executionContext: ExecutionContext = controllerComponents.executionContext

  }

  "getting the fractions" should {
    "return a success if the submitted request is valid" in {
      val fromDate: LocalDate = LocalDate.parse("2020-07-22")
      val toDate: LocalDate = LocalDate.parse("2021-07-22")

      when(mockHttp.get(any())(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[Either[UpstreamErrorResponse, Fractions]](using any(), any())).thenReturn(Future.successful(Right(Fractions("empref", List()))))

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
