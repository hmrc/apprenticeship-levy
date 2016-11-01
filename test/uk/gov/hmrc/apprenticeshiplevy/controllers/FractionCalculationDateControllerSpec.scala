/*
 * Copyright 2016 HM Revenue & Customs
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
import uk.gov.hmrc.play.http.HttpGet
import org.joda.time.LocalDate
import uk.gov.hmrc.apprenticeshiplevy.data.des.FractionCalculationDate
import scala.concurrent.Future
import uk.gov.hmrc.play.http.{HeaderCarrier,HttpReads,HttpResponse}
import uk.gov.hmrc.play.http.logging._
import uk.gov.hmrc.play.http.hooks.HttpHook
import play.api.libs.concurrent.Execution.Implicits._
import uk.gov.hmrc.apprenticeshiplevy.connectors._
import play.api.mvc.{ActionBuilder, Request, Result, Results}

class FractionCalculationDateControllerSpec extends UnitSpec with MockitoSugar {
  "getting fraction calculation date" should {
    "popogate authorization and environment headers on to connector" in {
      // set up
      val stubHttpGet = mock[HttpGet]
      val headerCarrierCaptor = ArgumentCaptor.forClass(classOf[HeaderCarrier])
      when(stubHttpGet.GET[FractionCalculationDate](anyString())(any(), headerCarrierCaptor.capture()))
           .thenReturn(Future.successful(FractionCalculationDate(new LocalDate(2016,11,3))))
      val controller = new FractionsCalculationController() with ApiController {
        def edhConnector: EDHConnector = new EDHConnector() {
          def edhBaseUrl: String = "http://a.guide.to.nowhere/"
          def httpGet: HttpGet = stubHttpGet
        }
      }

      // test
      val response: Future[Result] = controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                                                                    "Authorization"->"Bearer dsfda9080",
                                                                                                    "Environment"->"clone"))

      // check
      val actualHeaderCarrier = headerCarrierCaptor.getValue
      val expectedHeaderCarrier = HeaderCarrier(Some(Authorization("Bearer dsfda9080")))
      actualHeaderCarrier.authorization shouldBe expectedHeaderCarrier.authorization
      actualHeaderCarrier.extraHeaders.head shouldBe (("Environment","clone"))
    }

    "not fail if environment header not supplied" in {
      // set up
      val stubHttpGet = mock[HttpGet]
      val headerCarrierCaptor = ArgumentCaptor.forClass(classOf[HeaderCarrier])
      when(stubHttpGet.GET[FractionCalculationDate](anyString())(any(), headerCarrierCaptor.capture()))
           .thenReturn(Future.successful(FractionCalculationDate(new LocalDate(2016,11,3))))
      val controller = new FractionsCalculationController() with ApiController {
        def edhConnector: EDHConnector = new EDHConnector() {
          def edhBaseUrl: String = "http://a.guide.to.nowhere/"
          def httpGet: HttpGet = stubHttpGet
        }
      }

      // test
      val response: Future[Result] = controller.fractionCalculationDate()(FakeRequest().withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                                                                    "Authorization"->"Bearer dsfda9080"))

      // check
      val actualHeaderCarrier = headerCarrierCaptor.getValue
      val expectedHeaderCarrier = HeaderCarrier(Some(Authorization("Bearer dsfda9080")))
      actualHeaderCarrier.authorization shouldBe expectedHeaderCarrier.authorization
      actualHeaderCarrier.extraHeaders.head shouldBe (("Environment","clone"))
    }
  }
}
