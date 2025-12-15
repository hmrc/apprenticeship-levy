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

import java.io.IOException
import com.codahale.metrics.MetricRegistry
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status.{FORBIDDEN, INTERNAL_SERVER_ERROR, NOT_FOUND, REQUEST_TIMEOUT, SERVICE_UNAVAILABLE, TOO_MANY_REQUESTS, UNAUTHORIZED}
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, BodyParser, ControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status, stubControllerComponents}
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.apprenticeshiplevy.connectors.DesConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.{AuthAction, FakePrivilegedAuthAction}
import uk.gov.hmrc.apprenticeshiplevy.data.des.FractionCalculationDate
import uk.gov.hmrc.apprenticeshiplevy.utils.MockAppContext.mock
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{BadRequestException, GatewayTimeoutException, JsValidationException, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.{ExecutionContext, Future}

class DesControllerSpec extends AnyWordSpecLike with Matchers with OptionValues {
  val stubComponents: ControllerComponents = stubControllerComponents()
  val mockAppContext: AppContext = mock[AppContext]
  val mockHttp = mock[HttpClientV2]
  val mockRequestBuilder = mock[RequestBuilder]

  val controller: FractionsCalculationDateController = new FractionsCalculationDateController with DesController {
    def desConnector: DesConnector = new DesConnector {
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

  "DesController" should {
    "return a internal server error when upstream returns JsValidationException" in {
      when(mockHttp.get(any())(using any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[FractionCalculationDate](using any(), any())).thenReturn(Future.failed(new JsValidationException("GET", "/test", Int.getClass,"validation")))

      val response: Future[Result] = controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
        "Authorization"->"Bearer dsfda9080"))

      status(response) shouldBe INTERNAL_SERVER_ERROR
      contentAsJson(response) shouldBe Json.parse("""{"code":"DES_ERROR_JSON_FAILURE","message":"DES and/or BACKEND server returned bad json."}""")
    }
    "return a internal server error when upstream returns IllegalArgumentException" in {
      when(mockHttp.get(any())(using any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[FractionCalculationDate](using any(), any())).thenReturn(Future.failed(new IllegalArgumentException))

      val response: Future[Result] = controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
        "Authorization"->"Bearer dsfda9080"))

      status(response) shouldBe INTERNAL_SERVER_ERROR
      contentAsJson(response) shouldBe Json.parse("""{"code":"DES_ERROR_JSON_FAILURE","message":"DES and/or BACKEND server returned bad json."}""")
    }
    "return an internal server error when upstream returns BadRequestException" in {
      when(mockHttp.get(any())(using any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[FractionCalculationDate](using any(), any())).thenReturn(Future.failed(new BadRequestException("badRequest")))

      val response: Future[Result] = controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
        "Authorization"->"Bearer dsfda9080"))

      status(response) shouldBe 400
      contentAsJson(response) shouldBe Json.parse("""{"code":"DES_ERROR_BAD_REQUEST","message":"Bad request error"}""")
    }
    "return a service unavailable error when upstream returns IOException" in {
      when(mockHttp.get(any())(using any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[FractionCalculationDate](using any(), any())).thenReturn(Future.failed(new IOException("io exception")))

      val response: Future[Result] = controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
        "Authorization"->"Bearer dsfda9080"))

      status(response) shouldBe SERVICE_UNAVAILABLE
      contentAsJson(response) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")
    }
    "return a request timeout error when upstream returns GatewayTimeoutException" in {
      when(mockHttp.get(any())(using any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[FractionCalculationDate](using any(), any())).thenReturn(Future.failed(new GatewayTimeoutException("gateway timeout exception")))

      val response: Future[Result] = controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
        "Authorization"->"Bearer dsfda9080"))

      status(response) shouldBe REQUEST_TIMEOUT
      contentAsJson(response) shouldBe Json.parse("""{"code":"DES_ERROR_GATEWAY_TIMEOUT","message":"DES not responding error"}""")
    }
    "return a not found error when upstream returns NotFoundException" in {
      when(mockHttp.get(any())(using any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[FractionCalculationDate](using any(), any())).thenReturn(Future.failed(new NotFoundException("gateway timeout exception")))

      val response: Future[Result] = controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
        "Authorization"->"Bearer dsfda9080"))

      status(response) shouldBe NOT_FOUND
      contentAsJson(response) shouldBe Json.parse("""{"code":"DES_ERROR_NOT_FOUND","message":"DES endpoint or EmpRef not found"}""")
    }
    "return an internal server error when upstream returns a 412" in {
      when(mockHttp.get(any())(using any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[FractionCalculationDate](using any(), any())).thenReturn(Future.failed(UpstreamErrorResponse.apply("DES 4xx error: uk.gov.hmrc.play.http.Upstream4xxResponse: GET of 'http://localhost:8080/fraction-calculation-date' returned 412. Response body: '{\"reason\" : \"Backend systems not working\"}'",412)))

      val response: Future[Result] = controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
        "Authorization"->"Bearer dsfda9080"))

      status(response) shouldBe INTERNAL_SERVER_ERROR
      contentAsJson(response) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES backend error"}""")
    }
    "return a forbidden error when upstream returns a 403" in {
      when(mockHttp.get(any())(using any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[FractionCalculationDate](using any(), any())).thenReturn(Future.failed(UpstreamErrorResponse.apply("DES 4xx error: uk.gov.hmrc.play.http.Upstream4xxResponse: GET of 'http://localhost:8080/fraction-calculation-date' returned 403. Response body: '{\"reason\" : \"Backend systems not working\"}'",403)))

      val response: Future[Result] = controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
        "Authorization"->"Bearer dsfda9080"))

      status(response) shouldBe FORBIDDEN
      contentAsJson(response) shouldBe Json.parse("""{"code":"DES_ERROR_FORBIDDEN","message":"DES forbidden error"}""")
    }
    "return an unauthorised error when upstream returns a 401" in {
      when(mockHttp.get(any())(using any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[FractionCalculationDate](using any(), any())).thenReturn(Future.failed(UpstreamErrorResponse.apply("DES 4xx error: uk.gov.hmrc.play.http.Upstream4xxResponse: GET of 'http://localhost:8080/fraction-calculation-date' returned 401. Response body: '{\"reason\" : \"Backend systems not working\"}'",401)))

      val response: Future[Result] = controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
        "Authorization"->"Bearer dsfda9080"))

      status(response) shouldBe UNAUTHORIZED
      contentAsJson(response) shouldBe Json.parse("""{"code":"DES_ERROR_UNAUTHORIZED","message":"DES unauthorised error"}""")
    }
    "return a too many requests error when upstream returns a 429" in {
      when(mockHttp.get(any())(using any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[FractionCalculationDate](using any(), any())).thenReturn(Future.failed(UpstreamErrorResponse.apply("DES 4xx error: uk.gov.hmrc.play.http.Upstream4xxResponse: GET of 'http://localhost:8080/fraction-calculation-date' returned 429. Response body: '{\"reason\" : \"Backend systems not working\"}'",429)))

      val response: Future[Result] = controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
        "Authorization"->"Bearer dsfda9080"))

      status(response) shouldBe TOO_MANY_REQUESTS
      contentAsJson(response) shouldBe Json.parse("""{"code":"DES_ERROR_TOO_MANY_REQUESTS","message":"DES too many requests"}""")
    }
    "return a request timeout error when upstream returns a 408" in {
      when(mockHttp.get(any())(using any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[FractionCalculationDate](using any(), any())).thenReturn(Future.failed(UpstreamErrorResponse.apply("DES 4xx error: uk.gov.hmrc.play.http.Upstream4xxResponse: GET of 'http://localhost:8080/fraction-calculation-date' returned 408. Response body: '{\"reason\" : \"Backend systems not working\"}'",408)))

      val response: Future[Result] = controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
        "Authorization"->"Bearer dsfda9080"))

      status(response) shouldBe REQUEST_TIMEOUT
      contentAsJson(response) shouldBe Json.parse("""{"code":"DES_ERROR_TIMEOUT","message":"DES not responding error"}""")
    }
    "return an other DES Error when upstream returns an upstream 4xx error" in {
      when(mockHttp.get(any())(using any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[FractionCalculationDate](using any(), any())).thenReturn(Future.failed(UpstreamErrorResponse.apply("DES 4xx error: uk.gov.hmrc.play.http.Upstream4xxResponse: GET of 'http://localhost:8080/fraction-calculation-date' returned 400. Response body: '{\"reason\" : \"Backend systems not working\"}'",400)))

      val response: Future[Result] = controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
        "Authorization"->"Bearer dsfda9080"))

      status(response) shouldBe SERVICE_UNAVAILABLE
      contentAsJson(response) shouldBe Json.parse("""{"code":"DES_ERROR_OTHER","message":"DES 4xx error"}""")
    }
  }
}
