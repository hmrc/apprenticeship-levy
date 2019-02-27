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

package uk.gov.hmrc.apprenticeshiplevy.connectors

import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import org.mockito._
import org.mockito.Mockito._
import org.mockito.Matchers._
import uk.gov.hmrc.play.test.UnitSpec
import org.joda.time.{LocalDate, LocalDateTime}
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import uk.gov.hmrc.apprenticeshiplevy.utils._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.apprenticeshiplevy.data.audit.ALAEvent
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.audit.EventKeys._
import uk.gov.hmrc.play.audit.model.DataEvent
import play.api.libs.json.Json
import uk.gov.hmrc.http.{ HeaderCarrier, HttpGet, HttpResponse }

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
        when(stubHttpGet.GET[FractionCalculationDate](anyString())(any(), any(), any())).thenReturn(Future.successful(FractionCalculationDate(new LocalDate(2016,11,3))))
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
        when(stubHttpGet.GET[Fractions](anyString())(any(), any(), any()))
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

  "have Levy Declarations endpoint and" should {
    val hc = HeaderCarrier()
    val ec = defaultContext
    val stubHttpGet = mock[HttpGet]
    val connector = new DesConnector() {
      def baseUrl: String = "http://a.guide.to.nowhere"
      def httpGet: HttpGet = stubHttpGet
      protected def auditConnector: Option[AuditConnector] = None
    }

    "support original endpoint url" in {
      // set up
      val expected = EmployerPaymentsSummary("123/AB12345", List[EmployerPaymentSummary]())
      when(stubHttpGet.GET[HttpResponse](startsWith("http://a.guide.to.nowhere/rti/employers/123AB12345/employer-payment-summary"))(any(), any(), any()))
                      .thenReturn(Future.successful(
                        HttpResponse(200, Some(Json.parse("""{"empref":"123AB12345"}""")))
                      ))

      // test
      val futureResult = connector.eps("123AB12345", OpenEarlyDateRange(new LocalDate(2016,11,3)))(hc, ec)

      // check
      await[EmployerPaymentsSummary](futureResult) shouldBe expected
    }

    "support new endpoint url" in {
      // set up
      val expected = EmployerPaymentsSummary("123/AB12345", List[EmployerPaymentSummary]())
      when(stubHttpGet.GET[HttpResponse](startsWith("http://a.guide.to.nowhere/apprenticeship-levy/employers/123AB12345/declarations"))(any(), any(), any()))
                      .thenReturn(Future.successful(
                        HttpResponse(200, Some(Json.parse("""{"empref":"123AB12345"}""")))
                      ))
      val connector = new DesConnector() {
        def baseUrl: String = "http://a.guide.to.nowhere"
        def httpGet: HttpGet = stubHttpGet
        override protected[connectors] def isEpsOrigPathEnabled: Boolean = false
        protected def auditConnector: Option[AuditConnector] = None
      }

      // test
      val futureResult = connector.eps("123AB12345", OpenEarlyDateRange(new LocalDate(2016,11,3)))(hc, ec)

      // check
      await[EmployerPaymentsSummary](futureResult) shouldBe expected
    }

    "supply default dates when not specified" in {
      // set up
      val expected = EmployerPaymentsSummary("123/AB12345", List[EmployerPaymentSummary]())
      when(stubHttpGet.GET[HttpResponse](startsWith("http://a.guide.to.nowhere/rti/employers/123AB12345/employer-payment-summary?fromDate=20"))(any(), any(), any()))
                      .thenReturn(Future.successful(
                        HttpResponse(200, Some(Json.parse("""{"empref":"123AB12345"}""")))
                      ))

      // test
      val futureResult = connector.eps("123AB12345", OpenDateRange)(hc, ec)

      // check
      await[EmployerPaymentsSummary](futureResult) shouldBe expected
    }

    "with valid and invalid json" must {
      import play.api.libs.functional.syntax._
      import play.api.libs.json.Reads._
      import play.api.libs.json._

      "convert invalid empty json to valid response" in {
        val url = "http://a.guide.to.nowhere/rti/employers/123AB12345/employer-payment-summary"
        val expected = EmployerPaymentsSummary("123/AB12345", List[EmployerPaymentSummary]())
        when(stubHttpGet.GET[HttpResponse](startsWith(s"${url}?fromDate=20"))(any(), any(), any()))
                .thenReturn(Future.successful(
                  HttpResponse(200, Some(Json.parse("""{}""")))
                ))

        // test
        val futureResult = connector.eps("123AB12345", OpenDateRange)(hc, ec)

        // check
        await[EmployerPaymentsSummary](futureResult) shouldBe expected
      }

      "convert invalid bad date-time json values to valid date times" in {
        val url = "http://a.guide.to.nowhere/rti/employers/123AB12345/employer-payment-summary"
        val expected = EmployerPaymentsSummary("123/AB12345",
                        List(EmployerPaymentSummary(12345678L,new LocalDateTime("2016-07-14T16:05:44.000"),new LocalDateTime("2016-07-14T16:05:23.000"),"16-17",apprenticeshipLevy=Some(ApprenticeshipLevy(BigDecimal(600.00),BigDecimal(15000),"11")))))
        when(stubHttpGet.GET[HttpResponse](startsWith(s"${url}?fromDate=20"))(any(), any(), any()))
                        .thenReturn(Future.successful(
                          HttpResponse(200, Some(
                          Json.parse("""{
                          "empref": "123/AB12345",
                          "eps": [
                            {
                              "submissionId": 12345678,
                              "hmrcSubmissionTime": "2016-07-14T16:05:44Z",
                              "rtiSubmissionTime": "2016-07-14T16:05:23.123Z",
                              "taxYear": "16-17",
                              "apprenticeshipLevy": {
                                "amountDue": 600.00,
                                "taxMonth": "11",
                                "amountAllowance": 15000
                              }
                            }
                          ]
                        }""")))
                      ))

        // test
        val futureResult = connector.eps("123AB12345", OpenDateRange)(hc, ec)

        // check
        await[EmployerPaymentsSummary](futureResult) shouldBe expected
      }

      "convert valid json values to valid objects" in {
        val url = "http://a.guide.to.nowhere/rti/employers/123AB12345/employer-payment-summary"
        val expected = EmployerPaymentsSummary("123/AB12345",
                        List(EmployerPaymentSummary(12345678L,new LocalDateTime("2016-07-14T16:05:23.000"),new LocalDateTime("2016-07-14T16:05:23.000"),"16-17",apprenticeshipLevy=Some(ApprenticeshipLevy(BigDecimal(600.00),BigDecimal(15000),"11"))),
                             EmployerPaymentSummary(12345679L,new LocalDateTime("2015-04-07T16:05:23.000"),new LocalDateTime("2015-04-07T16:05:23.000"),"15-16",Some(ClosedDateRange(new LocalDate("2016-12-13"),new LocalDate("2017-03-22")))),
                             EmployerPaymentSummary(12345680L,new LocalDateTime("2016-05-07T16:05:23.000"),new LocalDateTime("2016-05-07T16:05:23.000"),"16-17",apprenticeshipLevy=Some(ApprenticeshipLevy(BigDecimal(500.00),BigDecimal(15000),"1"))),
                             EmployerPaymentSummary(12345681L,new LocalDateTime("2016-06-07T16:05:23.000"),new LocalDateTime("2016-06-07T16:05:23.000"),"16-17",apprenticeshipLevy=Some(ApprenticeshipLevy(BigDecimal(1000.00),BigDecimal(15000),"2"))),
                             EmployerPaymentSummary(12345682L,new LocalDateTime("2016-06-15T16:20:23.000"),new LocalDateTime("2016-06-15T16:20:23.000"),"16-17",apprenticeshipLevy=Some(ApprenticeshipLevy(BigDecimal(200.00),BigDecimal(15000),"2"))),
                             EmployerPaymentSummary(12345683L,new LocalDateTime("2016-07-15T16:05:23.000"),new LocalDateTime("2016-07-15T16:05:23.000"),"16-17",inactivePeriod=Some(ClosedDateRange(new LocalDate("2016-06-06"),new LocalDate("2016-09-05")))),
                             EmployerPaymentSummary(12345684L,new LocalDateTime("2016-10-15T16:05:23.000"),new LocalDateTime("2016-10-15T16:05:23.000"),"16-17",finalSubmission=Some(SchemeCeased(true,new LocalDate("2016-09-05"),None)))))
        when(stubHttpGet.GET[HttpResponse](startsWith(s"${url}?fromDate=20"))(any(), any(), any()))
                        .thenReturn(Future.successful{
                          HttpResponse(200, Some(
                          Json.parse("""{
                          "empref": "123/AB12345",
                          "eps": [
                            {
                              "submissionId": 12345678,
                              "hmrcSubmissionTime": "2016-07-14T16:05:23",
                              "rtiSubmissionTime": "2016-07-14T16:05:23",
                              "taxYear": "16-17",
                              "apprenticeshipLevy": {
                                "amountDue": 600.00,
                                "taxMonth": "11",
                                "amountAllowance": 15000
                              }
                            },
                            {
                              "submissionId": 12345679,
                              "hmrcSubmissionTime": "2015-04-07T16:05:23",
                              "rtiSubmissionTime": "2015-04-07T16:05:23",
                              "taxYear": "15-16",
                              "noPaymentPeriod": {
                                "from": "2016-12-13",
                                "to": "2017-03-22"
                              }
                            },
                            {
                              "submissionId": 12345680,
                              "hmrcSubmissionTime": "2016-05-07T16:05:23",
                              "rtiSubmissionTime": "2016-05-07T16:05:23",
                              "taxYear": "16-17",
                              "apprenticeshipLevy": {
                                "amountDue": 500.00,
                                "taxMonth": "1",
                                "amountAllowance": 15000
                              }
                            },
                            {
                              "submissionId": 12345681,
                              "hmrcSubmissionTime": "2016-06-07T16:05:23",
                              "rtiSubmissionTime": "2016-06-07T16:05:23",
                              "taxYear": "16-17",
                              "apprenticeshipLevy": {
                                "amountDue": 1000.00,
                                "taxMonth": "2",
                                "amountAllowance": 15000
                              }
                            },
                            {
                              "submissionId": 12345682,
                              "hmrcSubmissionTime": "2016-06-15T16:20:23",
                              "rtiSubmissionTime": "2016-06-15T16:20:23",
                              "taxYear": "16-17",
                              "apprenticeshipLevy": {
                                "amountDue": 200.00,
                                "taxMonth": "2",
                                "amountAllowance": 15000
                              }
                            },
                            {
                              "submissionId": 12345683,
                              "hmrcSubmissionTime": "2016-07-15T16:05:23",
                              "rtiSubmissionTime": "2016-07-15T16:05:23",
                              "taxYear": "16-17",
                              "inactivePeriod": {
                                "from": "2016-06-06",
                                "to": "2016-09-05"
                              }
                            },
                            {
                              "submissionId": 12345684,
                              "hmrcSubmissionTime": "2016-10-15T16:05:23",
                              "rtiSubmissionTime": "2016-10-15T16:05:23",
                              "taxYear": "16-17",
                              "finalSubmission": {
                                "schemeCeased": true,
                                "schemeCeasedDate": "2016-09-05"
                              }
                            }
                          ]
                        }""")))
                        })

        // test
        val futureResult = connector.eps("123AB12345", OpenDateRange)(hc, ec)

        // check
        await[EmployerPaymentsSummary](futureResult) shouldBe expected
      }
    }
  }
}
