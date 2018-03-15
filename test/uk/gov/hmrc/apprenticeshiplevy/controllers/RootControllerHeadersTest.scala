/*
 * Copyright 2018 HM Revenue & Customs
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

import java.net.URLEncoder

import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.apprenticeshiplevy.connectors.{AuthConnector, LiveAuthConnector}
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
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

class RootControllerHeadersTest extends UnitSpec with MockitoSugar {
  "propogate authorization headers on to connector" in {
    // set up
    val stubHttpGet = mock[HttpGet]
    val headerCarrierCaptor = ArgumentCaptor.forClass(classOf[HeaderCarrier])
    val authority = Authority("hi",
                     Accounts(epaye=Some(EpayeAccount("/abc", uk.gov.hmrc.domain.EmpRef("123","AB12345")))),
                     None,
                     None,
                     CredentialStrength.Weak,
                     ConfidenceLevel.L50,
                     None,
                     None,
                     None,
                     "")
    when(stubHttpGet.GET[Authority](anyString())(any(), headerCarrierCaptor.capture(), any()))
         .thenReturn(Future.successful(authority))
    val controller = new RootController() {
      def authConnector: AuthConnector = new AuthConnector() {
        def authBaseUrl: String = "http://a.guide.to.nowhere/"
        def http: HttpGet = stubHttpGet
        protected def auditConnector: Option[AuditConnector] = None
      }
      def emprefUrl(empref: uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference): String = ""
      def rootUrl: String = ""
    }

    // test
    val response: Future[Result] = controller.root()(FakeRequest()
                                                     .withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                                  "Authorization"->"Bearer dsfda9080",
                                                                  "Environment"->"clone"))

    // check
    val actualHeaderCarrier = headerCarrierCaptor.getValue
    val expectedHeaderCarrier = HeaderCarrier(Some(Authorization("Bearer dsfda9080")))
    actualHeaderCarrier.authorization shouldBe expectedHeaderCarrier.authorization
  }
}
