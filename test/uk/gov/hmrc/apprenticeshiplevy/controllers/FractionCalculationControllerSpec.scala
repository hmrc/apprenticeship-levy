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
import org.scalatest.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.apprenticeshiplevy.connectors._
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.{AuthAction, FakePrivilegedAuthAction}
import uk.gov.hmrc.apprenticeshiplevy.data.api._
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class FractionCalculationControllerSpec extends UnitSpec with MockitoSugar {
  "getting fraction calculations" should {
    "propogate environment but not authorization headers on to connector" in {
      // set up
      val stubHttpGet = mock[HttpGet]
      val headerCarrierCaptor = ArgumentCaptor.forClass(classOf[HeaderCarrier])
      when(stubHttpGet.GET[Fractions](anyString())(any(), any(), any()))
           .thenReturn(Future.successful(Fractions("123AB12345", List(FractionCalculation(new LocalDate(2016,4,22), List(Fraction("England", BigDecimal(0.83))))))))
      val controller = new FractionsController() with DesController {
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
      await(controller.fractions(EmploymentReference("123AB12345"), None, None)(FakeRequest()
                                                                       .withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                                                    "Authorization"->"Bearer dsfda9080",
                                                                                    "Environment"->"clone")))

      verify(stubHttpGet).GET[Fractions](anyString())(any(), headerCarrierCaptor.capture(), any())

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

      when(stubHttpGet.GET[Fractions](anyString())(any(), any(), any()))
           .thenReturn(Future.successful(Fractions("123AB12345",
                                                   List(FractionCalculation(new LocalDate(2016,4,22), List(Fraction("England", BigDecimal(0.83))))))))

      val controller = new FractionsController() with DesController {
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
      await(controller.fractions(EmploymentReference("123AB12345"), None, None)(FakeRequest()
                                                                      .withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                                                    "Authorization"->"Bearer dsfda9080")))

      verify(stubHttpGet).GET[Fractions](anyString())(any(), headerCarrierCaptor.capture(), any())

      // check
      val actualHeaderCarrier = headerCarrierCaptor.getValue
      val expectedHeaderCarrier = HeaderCarrier(Some(Authorization("Bearer ABC")))
      actualHeaderCarrier.authorization shouldBe expectedHeaderCarrier.authorization
      actualHeaderCarrier.extraHeaders shouldBe List(("X-Client-ID","Unknown caller"),("X-Client-Authorization-Token","Unknown caller"),("Environment","clone"))
    }
  }
}
