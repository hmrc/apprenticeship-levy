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

package uk.gov.hmrc.apprenticeshiplevy.connectors

import org.scalatest._
import org.scalatest.mock.MockitoSugar
import org.mockito._
import org.mockito.Mockito._
import org.mockito.Matchers._
import uk.gov.hmrc.play.test.UnitSpec
import org.joda.time.LocalDate
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import uk.gov.hmrc.apprenticeshiplevy.utils._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.apprenticeshiplevy.data.audit.ALAEvent
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.audit.EventKeys._
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
import uk.gov.hmrc.domain._
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.http.HeaderCarrier

class AuthConnectorSpec extends UnitSpec with MockitoSugar {
  "Auth Connector" should {
    "send audit events" in {
        // set up
        val stubAuditConnector= mock[AuditConnector]
        val eventCaptor = ArgumentCaptor.forClass(classOf[DataEvent])
        when(stubAuditConnector.sendEvent(eventCaptor.capture())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
        val event = ALAEvent("readEmprefDetails", "123AB12345")
        implicit val hc = HeaderCarrier()
        implicit val ec = defaultContext

        // test
        stubAuditConnector.sendEvent(event.toDataEvent(200))(hc,ec)

        // check
        val auditEvent = eventCaptor.getValue
        auditEvent.auditType shouldBe ("ServiceReceivedRequest")
        auditEvent.tags(TransactionName) shouldBe ("readEmprefDetails")
        auditEvent.detail("empref") shouldBe ("123AB12345")
    }
  }
}
