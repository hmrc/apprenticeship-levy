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
import uk.gov.hmrc.play.audit.model.DataEvent

class DesConnectorSpec extends UnitSpec with MockitoSugar {
  "DES Connector" should {
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

    "for Fraction Date endpoint" must {
      "when EDH not failing return local date instance of date" in {
        // set up
        val stubHttpGet = mock[HttpGet]
        when(stubHttpGet.GET[FractionCalculationDate](anyString())(any(), any())).thenReturn(Future.successful(FractionCalculationDate(new LocalDate(2016,11,3))))
        val connector = new DesConnector() {
          def baseUrl: String = "http://a.guide.to.nowhere/"
          def httpGet: HttpGet = stubHttpGet
          protected def auditConnector: Option[AuditConnector] = None
        }

        // test
        val futureResult = connector.fractionCalculationDate(HeaderCarrier(), defaultContext)

        // check
        await[LocalDate](futureResult) shouldBe new LocalDate(2016,11,3)
      }
    }
    "for Fractions endpoint" must {
      "when EDH not failing return fractions" in {
        // set up
        val stubHttpGet = mock[HttpGet]
        val expected = Fractions("123/AB12345", List(FractionCalculation(new LocalDate(2016,4,22), List(Fraction("England", BigDecimal(0.83))))))
        when(stubHttpGet.GET[Fractions](anyString())(any(), any()))
           .thenReturn(Future.successful(expected))
        val connector = new DesConnector() {
          def baseUrl: String = "http://a.guide.to.nowhere/"
          def httpGet: HttpGet = stubHttpGet
          protected def auditConnector: Option[AuditConnector] = None
        }

        // test
        val futureResult = connector.fractions("123/AB12345", OpenDateRange)(HeaderCarrier(),defaultContext)

        // check
        await[Fractions](futureResult) shouldBe expected
      }
    }
  }
}
