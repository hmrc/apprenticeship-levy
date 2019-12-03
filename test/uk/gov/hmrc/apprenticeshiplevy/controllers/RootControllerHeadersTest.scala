/*
 * Copyright 2019 HM Revenue & Customs
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

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.apprenticeshiplevy.connectors.AuthConnector
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, Authority, ConfidenceLevel, CredentialStrength, EpayeAccount}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

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
