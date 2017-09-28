/*
 * Copyright 2017 HM Revenue & Customs
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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.scalatest._
import org.scalatest.mock.MockitoSugar
import org.mockito._
import org.mockito.Mockito._
import org.mockito.Matchers._
import uk.gov.hmrc.play.test.UnitSpec
import org.joda.time.LocalDate
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import uk.gov.hmrc.apprenticeshiplevy.data.api._
import scala.concurrent.Future
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging._
import play.api.libs.concurrent.Execution.Implicits._
import uk.gov.hmrc.apprenticeshiplevy.connectors._
import play.api.mvc.{ActionBuilder, Request, Result, Results}
import play.api.libs.json.Json
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.http.{ HeaderCarrier, HttpGet }
import uk.gov.hmrc.http.logging.Authorization

class FractionCalculationControllerSpec extends UnitSpec with MockitoSugar {
  "getting fraction calculations" should {
    "propogate environment but not authorization headers on to connector" in {
      // set up
      val stubHttpGet = mock[HttpGet]
      val headerCarrierCaptor = ArgumentCaptor.forClass(classOf[HeaderCarrier])
      when(stubHttpGet.GET[Fractions](anyString())(any(), headerCarrierCaptor.capture()))
           .thenReturn(Future.successful(Fractions("123AB12345", List(FractionCalculation(new LocalDate(2016,4,22), List(Fraction("England", BigDecimal(0.83))))))))
      val controller = new FractionsController() with DesController {
        def desConnector: DesConnector = new DesConnector() {
          def baseUrl: String = "http://a.guide.to.nowhere/"
          def httpGet: HttpGet = stubHttpGet
          protected def auditConnector: Option[AuditConnector] = None
        }
        override protected def defaultDESEnvironment: String = "clone"

        override protected def defaultDESToken: String = "ABC"
      }

      // test
      val response: Future[Result] = controller.fractions(EmploymentReference("123AB12345"), None, None)(FakeRequest()
                                                                       .withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                                                    "Authorization"->"Bearer dsfda9080",
                                                                                    "Environment"->"clone"))

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
      when(stubHttpGet.GET[Fractions](anyString())(any(), headerCarrierCaptor.capture()))
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
      }

      // test
      val response: Future[Result] = controller.fractions(EmploymentReference("123AB12345"), None, None)(FakeRequest()
                                                                      .withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                                                    "Authorization"->"Bearer dsfda9080"))

      // check
      val actualHeaderCarrier = headerCarrierCaptor.getValue
      val expectedHeaderCarrier = HeaderCarrier(Some(Authorization("Bearer ABC")))
      actualHeaderCarrier.authorization shouldBe expectedHeaderCarrier.authorization
      actualHeaderCarrier.extraHeaders shouldBe List(("X-Client-ID","Unknown caller"),("X-Client-Authorization-Token","Unknown caller"),("Environment","clone"))
    }

    /*"recover from exceptions" in {
      // set up
      val stubHttpGet = mock[HttpGet]
      when(stubHttpGet.GET[FractionCalculationDate](anyString())(any(), any()))
           .thenReturn(Future.failed(new Upstream5xxResponse("DES 5xx error: uk.gov.hmrc.play.http.Upstream5xxResponse: GET of 'http://localhost:8080/fraction-calculation-date' returned 503. Response body: '{\"reason\" : \"Backend systems not working\"}'", 1, 2)))
      val controller = new FractionsController() with ApiController {
        def desConnector: DesConnector = new DesConnector() {
          def baseUrl: String = "http://a.guide.to.nowhere/"
          def httpGet: HttpGet = stubHttpGet
        }
      }

      // test
      val response: Future[Result] = controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                                                                    "Authorization"->"Bearer dsfda9080"))

      // check
      status(response) shouldBe 503
      contentAsJson(response) shouldBe Json.parse("""{"code":"DES_ERROR","message":"DES 5xx error: Backend systems not working"}""")
    }*/
  }
}
