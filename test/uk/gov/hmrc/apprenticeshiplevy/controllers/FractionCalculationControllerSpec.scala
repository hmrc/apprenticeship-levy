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

import com.codahale.metrics.MetricRegistry
import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, verify, when}
import org.mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.{AnyContent, BodyParser, ControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.apprenticeshiplevy.connectors._
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.{AuthAction, FakePrivilegedAuthAction}
import uk.gov.hmrc.apprenticeshiplevy.data.api._
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import uk.gov.hmrc.apprenticeshiplevy.utils.AppLevyUnitSpec
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.http.Authorization
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.{ExecutionContext, Future}

class FractionCalculationControllerSpec extends AppLevyUnitSpec with BeforeAndAfterEach {

  val stubComponents: ControllerComponents = stubControllerComponents()
  val mockAppContext: AppContext = mock[AppContext]
  val mockHttp = mock[HttpClient]

  def controller : FractionsController = new FractionsController with DesController {
    def desConnector: DesConnector = new DesConnector() {
      override val appContext: AppContext = mockAppContext
      override protected def auditConnector: Option[AuditConnector] = None
      override def registry: Option[MetricRegistry] = None
      def baseUrl: String = "http://a.guide.to.nowhere/"
      def httpClient: HttpClient = mockHttp

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
    reset(mockAppContext, mockHttp)
  }

  "getting fraction calculations" should {
    "propogate environment but not authorization headers on to connector" in {
      // set up
      val headerCarrierCaptor: ArgumentCaptor[HeaderCarrier] = ArgumentCaptor.forClass(classOf[HeaderCarrier])
      when(mockHttp.GET[Fractions](anyString(), any(), any())(any(), any(), any()))
           .thenReturn(Future.successful(Fractions("123AB12345", List(FractionCalculation(new LocalDate(2016,4,22), List(Fraction("England", BigDecimal(0.83))))))))

      // test
      await(controller.fractions(EmploymentReference("123AB12345"), None, None)(FakeRequest()
                                                                       .withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                                                    "Authorization"->"Bearer dsfda9080",
                                                                                    "Environment"->"clone")))

      verify(mockHttp).GET[Fractions](anyString(), any(), any())(any(), headerCarrierCaptor.capture(), any())

      // check
      val actualHeaderCarrier = headerCarrierCaptor.getValue
      val expectedHeaderCarrier = HeaderCarrier(Some(Authorization("Bearer ABC")))
      actualHeaderCarrier.authorization shouldBe expectedHeaderCarrier.authorization
      actualHeaderCarrier.extraHeaders shouldBe List(("X-Client-ID","Unknown caller"),("X-Client-Authorization-Token","Unknown caller"),("Environment","clone"))
    }

    "not fail if environment header not supplied" in {
      // set up
      val headerCarrierCaptor: ArgumentCaptor[HeaderCarrier] = ArgumentCaptor.forClass(classOf[HeaderCarrier])

      when(mockHttp.GET[Fractions](anyString(), any(), any())(any(), any(), any()))
           .thenReturn(Future.successful(Fractions("123AB12345",
                                                   List(FractionCalculation(new LocalDate(2016,4,22), List(Fraction("England", BigDecimal(0.83))))))))

      // test
      await(controller.fractions(EmploymentReference("123AB12345"), None, None)(FakeRequest()
                                                                      .withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                                                    "Authorization"->"Bearer dsfda9080")))

      verify(mockHttp).GET[Fractions](anyString(), any(), any())(any(), headerCarrierCaptor.capture(), any())

      // check
      val actualHeaderCarrier = headerCarrierCaptor.getValue
      val expectedHeaderCarrier = HeaderCarrier(Some(Authorization("Bearer ABC")))
      actualHeaderCarrier.authorization shouldBe expectedHeaderCarrier.authorization
      actualHeaderCarrier.extraHeaders shouldBe List(("X-Client-ID","Unknown caller"),("X-Client-Authorization-Token","Unknown caller"),("Environment","clone"))
    }
  }
}
