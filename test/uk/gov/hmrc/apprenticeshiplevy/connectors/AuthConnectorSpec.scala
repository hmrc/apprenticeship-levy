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

package uk.gov.hmrc.apprenticeshiplevy.connectors

import org.scalatest._
import org.scalatest.mock.MockitoSugar
import org.mockito._
import org.mockito.Mockito._
import org.mockito.Matchers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.play.http.HttpGet
import org.joda.time.LocalDate
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import scala.concurrent.Future
import uk.gov.hmrc.play.http.{HeaderCarrier,HttpReads,HttpResponse}
import uk.gov.hmrc.play.http.hooks.HttpHook
import play.api.libs.concurrent.Execution.Implicits._
import uk.gov.hmrc.apprenticeshiplevy.utils._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.apprenticeshiplevy.data.audit.ALAEvent
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.audit.EventKeys._
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
import uk.gov.hmrc.domain._

class AuthConnectorSpec extends UnitSpec with MockitoSugar {
  "Auth Connector" should {
    "send audit events" in {
        // set up
        val stubAuditConnector= mock[AuditConnector]
        val eventCaptor = ArgumentCaptor.forClass(classOf[ALAEvent])
        when(stubAuditConnector.sendEvent(eventCaptor.capture())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

        val accounts = Accounts(None, None, None, None, Some(new EpayeAccount("", EmpRef("123", "AB12345"))))
        val authority = Authority("", accounts, None, None, CredentialStrength.Weak, ConfidenceLevel.L50, None, None)
        val stubHttpGet = mock[HttpGet]
        when(stubHttpGet.GET[Authority](anyString())(any(), any())).thenReturn(Future.successful(authority))
        val connector = new AuthConnector() {
          def authBaseUrl: String = "http://a.guide.to.nowhere/"
          def http: HttpGet = stubHttpGet
          protected def auditConnector: Option[AuditConnector] = Some(stubAuditConnector)
        }
        val event = new ALAEvent("readEmprefDetails", "123AB12345")(HeaderCarrier())

        // test
        connector.sendEvent(event)(defaultContext)

        // check
        val auditEvent = eventCaptor.getValue
        auditEvent.auditType shouldBe ("ServiceReceivedRequest")
        auditEvent.tags(TransactionName) shouldBe ("readEmprefDetails")
        auditEvent.detail("empref") shouldBe ("123AB12345")
    }
  }
}
