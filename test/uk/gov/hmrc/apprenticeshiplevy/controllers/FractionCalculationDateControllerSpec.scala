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
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, BodyParser, ControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.apprenticeshiplevy.connectors.DesConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.{AuthAction, FakePrivilegedAuthAction}
import uk.gov.hmrc.apprenticeshiplevy.data.des.FractionCalculationDate
import uk.gov.hmrc.apprenticeshiplevy.utils.AppLevyUnitSpec
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.net.URL
import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class FractionCalculationDateControllerSpec extends AppLevyUnitSpec with BeforeAndAfterEach {

  val stubComponents: ControllerComponents = stubControllerComponents()
  val mockAppContext: AppContext = mock[AppContext]
  val mockHttp: HttpClientV2 = mock[HttpClientV2]
  val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]

  def controller : FractionsCalculationDateController = new FractionsCalculationDateController with DesController {
    def desConnector: DesConnector = new DesConnector() {
      override val appContext: AppContext = mockAppContext
      override protected def auditConnector: Option[AuditConnector] = None
      override def registry: Option[MetricRegistry] = None
      def baseUrl: String = "http://a.guide.to.nowhere/"
      def httpClient: HttpClientV2 = mockHttp
      override def desAuthorization: String = "localBearer"

      override def desEnvironment: String = "localEnv"
    }

    override protected def defaultDESEnvironment: String = "clone"

    override protected def defaultDESToken: String = "ABC"

    override val authAction: AuthAction = new FakePrivilegedAuthAction

    override val appContext: AppContext = mockAppContext

    override def controllerComponents: ControllerComponents = stubComponents

    override def executionContext: ExecutionContext = controllerComponents.executionContext

    override def parser: BodyParser[AnyContent] = controllerComponents.parsers.default
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAppContext)
    reset(mockHttp)
  }

  "getting fraction calculation date" should {
    "propagate environment but not authorization headers on to connector" in {
      // set up

      val headerCarrierCaptor: ArgumentCaptor[HeaderCarrier] = ArgumentCaptor.forClass(classOf[HeaderCarrier])
      when(mockHttp.get(any())(using any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[Either[UpstreamErrorResponse, FractionCalculationDate]](using any(), any())).thenReturn(Future.successful(Right(FractionCalculationDate(LocalDate.of(2016,11,3)))))

      val response = await(controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                                                                    "Authorization"->"Bearer dsfda9080",
                                                                                                    "Environment"->"clone")))

      verify(mockHttp).get(any())(using headerCarrierCaptor.capture())

      // check
      val actualHeaderCarrier = headerCarrierCaptor.getValue
      val expectedHeaderCarrier = HeaderCarrier(Some(Authorization("Bearer ABC")))
      actualHeaderCarrier.authorization shouldBe expectedHeaderCarrier.authorization
      actualHeaderCarrier.extraHeaders shouldBe List(("X-Client-ID","Unknown caller"),("X-Client-Authorization-Token","Unknown caller"),("Environment","clone"))
      status(response) shouldBe OK
    }

    "not fail if environment header not supplied" in {
      // set up
      val headerCarrierCaptor: ArgumentCaptor[HeaderCarrier] = ArgumentCaptor.forClass(classOf[HeaderCarrier])
      val urlCaptor: ArgumentCaptor[URL] = ArgumentCaptor.forClass(classOf[URL])

      when(mockHttp.get(any())(using any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[FractionCalculationDate](using any(), any())).thenReturn(Future.successful(FractionCalculationDate(LocalDate.of(2016,11,3))))

      // test
      await(controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                                                                    "Authorization"->"Bearer dsfda9080")))
      verify(mockHttp).get(urlCaptor.capture())(using headerCarrierCaptor.capture())

      // check
      val actualHeaderCarrier = headerCarrierCaptor.getValue
      val expectedHeaderCarrier = HeaderCarrier(Some(Authorization("Bearer ABC")))
      actualHeaderCarrier.authorization shouldBe expectedHeaderCarrier.authorization
      actualHeaderCarrier.extraHeaders shouldBe List(("X-Client-ID","Unknown caller"),("X-Client-Authorization-Token","Unknown caller"),("Environment","clone"))
    }

    "recover from exceptions" in {
      // set up
      when(mockHttp.get(any())(using any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[FractionCalculationDate](using any(), any())).thenReturn(Future.failed(UpstreamErrorResponse.apply(
        """DES 5xx error: uk.gov.hmrc.play.http.Upstream5xxResponse: GET of 'http://localhost:8080/fraction-calculation-date' returned 503.
          | Response body: '{"reason" : "Backend systems not working"}'""".stripMargin,500)))

      // test
      val response: Future[Result] = controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                                                                    "Authorization"->"Bearer dsfda9080"))

      // check
      status(response) shouldBe SERVICE_UNAVAILABLE
      contentAsJson(response) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")
    }
  }
}
