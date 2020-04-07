/*
 * Copyright 2020 HM Revenue & Customs
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

import org.joda.time.LocalDate
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apprenticeshiplevy.connectors._
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.{AuthAction, FakeAuthAction, FakePrivilegedAuthAction}
import uk.gov.hmrc.apprenticeshiplevy.data.des.FractionCalculationDate
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, Upstream5xxResponse}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class FractionCalculationDateControllerSpec extends UnitSpec with MockitoSugar {
  "getting fraction calculation date" should {
    "propogate environment but not authorization headers on to connector" in {
      // set up
      val stubHttpGet = mock[HttpGet]
      val headerCarrierCaptor = ArgumentCaptor.forClass(classOf[HeaderCarrier])
      when(stubHttpGet.GET[FractionCalculationDate](anyString())(any(), any(), any()))
           .thenReturn(Future.successful(FractionCalculationDate(new LocalDate(2016,11,3))))

      val controller = new FractionsCalculationDateController() with DesController {
        val desConnector: DesConnector = new DesConnector() {
          val baseUrl: String = "http://a.guide.to.nowhere/"
          val httpGet: HttpGet = stubHttpGet
          protected val auditConnector: Option[AuditConnector] = None
        }
        override protected val defaultDESEnvironment: String = "clone"

        override protected val defaultDESToken: String = "ABC"

        override val authAction: AuthAction = FakeAuthAction
      }

      // test
      await(controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                                                                    "Authorization"->"Bearer dsfda9080",
                                                                                                    "Environment"->"clone")))

      verify(stubHttpGet).GET[FractionCalculationDate](anyString())(any(), headerCarrierCaptor.capture(), any())

      // check
      val actualHeaderCarrier = headerCarrierCaptor.getValue
      val expectedHeaderCarrier = HeaderCarrier(Some(Authorization("Bearer ABC")))
      actualHeaderCarrier.authorization shouldBe expectedHeaderCarrier.authorization
      actualHeaderCarrier.extraHeaders shouldBe List(("X-Client-ID","Unknown caller"),("X-Client-Authorization-Token","Unknown caller"),("Environment","clone"))
    }

    "not fail if environment header not supplied" in {
      // set up
      val stubHttpGet = mock[HttpGet]
      val headerCarrierCaptor = ArgumentCaptor.forClass(classOf[HeaderCarrier])
      when(stubHttpGet.GET[FractionCalculationDate](anyString())(any(), any(), any()))
           .thenReturn(Future.successful(FractionCalculationDate(new LocalDate(2016,11,3))))
      val controller = new FractionsCalculationDateController() with DesController {
        def desConnector: DesConnector = new DesConnector() {
          def baseUrl: String = "http://a.guide.to.nowhere/"
          def httpGet: HttpGet = stubHttpGet
          protected def auditConnector: Option[AuditConnector] = None
        }
        override protected def defaultDESEnvironment: String = "clone"

        override protected def defaultDESToken: String = "ABC"

        override val authAction: AuthAction = FakePrivilegedAuthAction
      }

      // test
      await(controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                                                                    "Authorization"->"Bearer dsfda9080")))

      verify(stubHttpGet).GET[FractionCalculationDate](anyString())(any(), headerCarrierCaptor.capture(), any())

      // check
      val actualHeaderCarrier = headerCarrierCaptor.getValue
      val expectedHeaderCarrier = HeaderCarrier(Some(Authorization("Bearer ABC")))
      actualHeaderCarrier.authorization shouldBe expectedHeaderCarrier.authorization
      actualHeaderCarrier.extraHeaders shouldBe List(("X-Client-ID","Unknown caller"),("X-Client-Authorization-Token","Unknown caller"),("Environment","clone"))
    }

    "recover from exceptions" in {
      // set up
      val stubHttpGet = mock[HttpGet]
      when(stubHttpGet.GET[FractionCalculationDate](anyString())(any(), any(), any()))
           .thenReturn(Future.failed(new Upstream5xxResponse("DES 5xx error: uk.gov.hmrc.play.http.Upstream5xxResponse: GET of 'http://localhost:8080/fraction-calculation-date' returned 503. Response body: '{\"reason\" : \"Backend systems not working\"}'", 1, 2)))
      val controller = new FractionsCalculationDateController() with DesController {
        def desConnector: DesConnector = new DesConnector() {
          def baseUrl: String = "http://a.guide.to.nowhere/"
          def httpGet: HttpGet = stubHttpGet
          protected def auditConnector: Option[AuditConnector] = None
        }
        override protected def defaultDESEnvironment: String = "clone"

        override protected def defaultDESToken: String = "ABC"

        override val authAction: AuthAction = FakePrivilegedAuthAction
      }

      // test
      val response: Future[Result] = controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                                                                    "Authorization"->"Bearer dsfda9080"))

      // check
      status(response) shouldBe 503
      contentAsJson(response) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error: Backend systems not working"}""")
    }
  }
}
