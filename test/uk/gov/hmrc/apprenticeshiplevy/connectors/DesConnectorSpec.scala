/*
 * Copyright 2021 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock.{ok, _}
import org.joda.time.{LocalDate, LocalDateTime}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{when, reset => mockReset}
import org.mockito._
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.apprenticeshiplevy.audit.Auditor
import uk.gov.hmrc.apprenticeshiplevy.data.audit.ALAEvent
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import uk.gov.hmrc.apprenticeshiplevy.utils._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.EventKeys._
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent
import views.html.helper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DesConnectorSpec
  extends BaseSpec
    with GuiceOneAppPerSuite
    with MockitoSugar
    with ScalaFutures
    with WireMockHelper {

  val baseUrl = "/sandbox/data"
  val mockAuditor: Auditor = mock[Auditor]
  val mockHeaderCarrier: HeaderCarrier = HeaderCarrier(
    extraHeaders = Seq(
      "X-Client-ID" -> "ClientId",
      "Authorization" -> "Bearer ABC",
      "Environment" -> "Test"
  ))

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .configure(
      "microservice.epsOrigPathEnabled" -> true,
      "microservice.services.auth.host" -> "127.0.0.1",
      "microservice.services.auth.port" -> server.port(),
      "microservice.services.des.host" -> "127.0.0.1",
      "microservice.services.des.port" -> server.port(),
      "microservice.services.stub-des.host" -> "127.0.0.1",
      "microservice.services.stub-des.port" -> server.port(),
      "microservice.services.stub-auth.host" -> "127.0.0.1",
      "microservice.services.stub-auth.port" -> server.port(),
      "metrics.enabled" -> false
    )
    .overrides(
      bind[Auditor].toInstance(mockAuditor)
    )
    .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    mockReset(mockAuditor)
  }

  lazy val desConnector = app.injector.instanceOf[LiveDesConnector]

  "DES Connector" should {
    "send audit events" in {
        val stubAuditConnector= mock[AuditConnector]
        val eventCaptor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
        when(stubAuditConnector.sendEvent(eventCaptor.capture())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
        val event = ALAEvent("readEmprefDetails", "123AB12345")

        stubAuditConnector.sendEvent(event.toDataEvent(200)(mockHeaderCarrier))

        val auditEvent = eventCaptor.getValue
        auditEvent.auditType shouldBe ("ServiceReceivedRequest")
        auditEvent.tags(TransactionName) shouldBe ("readEmprefDetails")
        auditEvent.detail("empref") shouldBe ("123AB12345")
    }

    "for Fraction Date endpoint" must {
      "when EDH not failing return local date instance of date" in {

        val fractionsCalculationDateUrl = s"$baseUrl/apprenticeship-levy/fraction-calculation-date"
        val fractionCalculationDate = FractionCalculationDate(new LocalDate(2016,11,3))
        val json = Json.toJson[FractionCalculationDate](fractionCalculationDate)
        val stubResponse = ok(json.toString)
        val expectedResponse = new LocalDate(2016,11,3)

        stubGetServer(stubResponse, fractionsCalculationDateUrl)

        val response = await(desConnector.fractionCalculationDate(mockHeaderCarrier))

        response shouldBe expectedResponse

        server.verify(
          getRequestedFor(urlEqualTo(fractionsCalculationDateUrl))
            .withHeader("Authorization", equalTo(s"Bearer ABC"))
            .withHeader("X-Client-ID", equalTo("ClientId"))
            .withHeader("Environment", equalTo("Test"))
        )
      }
    }

    "for Fractions endpoint" must {
      "when EDH not failing return fractions" in {

        val localDate = new LocalDate(2016,4,22)
        val dateRange = OpenEarlyDateRange(localDate)
        val dateRangeParams = dateRange.toParams
        val empRef = "123/AB12345"
        val employersUrl = s"$baseUrl/apprenticeship-levy/employers/${helper.urlEncode(empRef)}/fractions?$dateRangeParams"
        val expectedResponse = Fractions("123/AB12345", List(FractionCalculation(localDate, List(Fraction("England", BigDecimal(0.83))))))
        val json = Json.toJson[Fractions](expectedResponse)
        val stubResponse = ok(json.toString)

        stubGetServer(stubResponse, employersUrl)

        val response = await(desConnector.fractions(empRef, dateRange)(mockHeaderCarrier))

        response shouldBe expectedResponse

        server.verify(
          getRequestedFor(urlEqualTo(employersUrl))
            .withHeader("Authorization", equalTo(s"Bearer ABC"))
            .withHeader("X-Client-ID", equalTo("ClientId"))
            .withHeader("Environment", equalTo("Test"))
        )
      }
    }
  }

  "have Levy Declarations endpoint and" should {
    "support original endpoint url" in {

      val empRef = "123AB12345"
      val empRefWithSlash = "123/AB12345"
      val localDate = new LocalDate(2016,11,3)
      val dateRange = OpenEarlyDateRange(localDate)
      val dateRangeParams = dateRange.toParams
      val expectedResponse = EmployerPaymentsSummary(empRefWithSlash, List[EmployerPaymentSummary]())
      val employerPaymentsSummaryUrl = s"$baseUrl/rti/employers/${helper.urlEncode(empRef)}/employer-payment-summary?$dateRangeParams"
      val json = Json.toJson[EmployerPaymentsSummary](expectedResponse)
      val stubResponse = ok(json.toString)

      stubGetServer(stubResponse, employerPaymentsSummaryUrl)

      val response = await(desConnector.eps(empRef, dateRange)(headerCarrier))

      response shouldBe expectedResponse
    }

    "supply default dates when not specified" in {

      val empRef = "123AB12345"
      val empRefWithSlash = "123/AB12345"
      val localDate = new LocalDate(2016,4,22)
      val dateRange = OpenEarlyDateRange(localDate)
      val employerPaymentsSummaryUrl = s"$baseUrl/rti/employers/${helper.urlEncode(empRef)}/employer-payment-summary?toDate=${localDate.toString()}"
      val expectedResponse = EmployerPaymentsSummary(empRefWithSlash, List[EmployerPaymentSummary]())
      val json = Json.toJson[EmployerPaymentsSummary](expectedResponse)
      val stubResponse = ok(json.toString)

      stubGetServer(stubResponse, employerPaymentsSummaryUrl)

      val response = await(desConnector.eps(empRef, dateRange)(headerCarrier))

      response shouldBe expectedResponse
    }

    "with valid and invalid json" must {
      import play.api.libs.json._

      "convert invalid empty json to valid response" in {

        val empRef = "123AB12345"
        val empRefWithSlash = "123/AB12345"
        val localDate = new LocalDate(2016,4,22)
        val dateRange = OpenEarlyDateRange(localDate)
        val employerPaymentsSummaryUrl = s"$baseUrl/rti/employers/${helper.urlEncode(empRef)}/employer-payment-summary?toDate=${localDate.toString()}"
        val expectedResponse = EmployerPaymentsSummary(empRefWithSlash, List[EmployerPaymentSummary]())
        val json = Json.parse("""{"empref":"123AB12345"}""")
        val stubResponse = ok(json.toString)

        stubGetServer(stubResponse, employerPaymentsSummaryUrl)

        val response = await(desConnector.eps(empRef, dateRange)(headerCarrier))

        response shouldBe expectedResponse
      }

      "convert invalid bad date-time json values to valid date times" in {

        val empRef = "123AB12345"
        val empRefWithSlash = "123/AB12345"
        val dateFrom = new LocalDate(2016,7,1)
        val dateTo = new LocalDate(2016,7,15)
        val dateRange = ClosedDateRange(dateFrom, dateTo)
        val dateRangeParams = dateRange.toParams
        val employerPaymentsSummaryUrl = s"$baseUrl/rti/employers/${helper.urlEncode(empRef)}/employer-payment-summary?$dateRangeParams"

        val expectedResponse = EmployerPaymentsSummary(
          empRefWithSlash,
          List(
            EmployerPaymentSummary(
              12345678L,
              new LocalDateTime("2016-07-14T16:05:44.000"),
              new LocalDateTime("2016-07-14T16:05:23.000"),"16-17",
              apprenticeshipLevy = Some(ApprenticeshipLevy(BigDecimal(600.00), BigDecimal(15000),"11")))
          )
        )

        val json = Json.parse(
        """{
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
        }"""
        )

        val stubResponse = ok(json.toString)

        stubGetServer(stubResponse, employerPaymentsSummaryUrl)

        val response = await(desConnector.eps(empRef, dateRange)(headerCarrier))

        response shouldBe expectedResponse
      }

//      "convert valid json values to valid objects" in {
//        val url = "http://a.guide.to.nowhere/rti/employers/123AB12345/employer-payment-summary"
//        val expected = EmployerPaymentsSummary("123/AB12345",
//                        List(EmployerPaymentSummary(12345678L,new LocalDateTime("2016-07-14T16:05:23.000"),new LocalDateTime("2016-07-14T16:05:23.000"),"16-17",apprenticeshipLevy=Some(ApprenticeshipLevy(BigDecimal(600.00),BigDecimal(15000),"11"))),
//                             EmployerPaymentSummary(12345679L,new LocalDateTime("2015-04-07T16:05:23.000"),new LocalDateTime("2015-04-07T16:05:23.000"),"15-16",Some(ClosedDateRange(new LocalDate("2016-12-13"),new LocalDate("2017-03-22")))),
//                             EmployerPaymentSummary(12345680L,new LocalDateTime("2016-05-07T16:05:23.000"),new LocalDateTime("2016-05-07T16:05:23.000"),"16-17",apprenticeshipLevy=Some(ApprenticeshipLevy(BigDecimal(500.00),BigDecimal(15000),"1"))),
//                             EmployerPaymentSummary(12345681L,new LocalDateTime("2016-06-07T16:05:23.000"),new LocalDateTime("2016-06-07T16:05:23.000"),"16-17",apprenticeshipLevy=Some(ApprenticeshipLevy(BigDecimal(1000.00),BigDecimal(15000),"2"))),
//                             EmployerPaymentSummary(12345682L,new LocalDateTime("2016-06-15T16:20:23.000"),new LocalDateTime("2016-06-15T16:20:23.000"),"16-17",apprenticeshipLevy=Some(ApprenticeshipLevy(BigDecimal(200.00),BigDecimal(15000),"2"))),
//                             EmployerPaymentSummary(12345683L,new LocalDateTime("2016-07-15T16:05:23.000"),new LocalDateTime("2016-07-15T16:05:23.000"),"16-17",inactivePeriod=Some(ClosedDateRange(new LocalDate("2016-06-06"),new LocalDate("2016-09-05")))),
//                             EmployerPaymentSummary(12345684L,new LocalDateTime("2016-10-15T16:05:23.000"),new LocalDateTime("2016-10-15T16:05:23.000"),"16-17",finalSubmission=Some(SchemeCeased(true,new LocalDate("2016-09-05"),None)))))
//        when(mockAppContext.epsOrigPathEnabled).thenReturn(true)
//        when(mockHttp.GET[HttpResponse](startsWith(s"${url}?fromDate=20"), any(), any())(any(), any(), any()))
//                        .thenReturn(Future.successful{
//                          HttpResponse(200, Some(
//                          Json.parse("""{
//                          "empref": "123/AB12345",
//                          "eps": [
//                            {
//                              "submissionId": 12345678,
//                              "hmrcSubmissionTime": "2016-07-14T16:05:23",
//                              "rtiSubmissionTime": "2016-07-14T16:05:23",
//                              "taxYear": "16-17",
//                              "apprenticeshipLevy": {
//                                "amountDue": 600.00,
//                                "taxMonth": "11",
//                                "amountAllowance": 15000
//                              }
//                            },
//                            {
//                              "submissionId": 12345679,
//                              "hmrcSubmissionTime": "2015-04-07T16:05:23",
//                              "rtiSubmissionTime": "2015-04-07T16:05:23",
//                              "taxYear": "15-16",
//                              "noPaymentPeriod": {
//                                "from": "2016-12-13",
//                                "to": "2017-03-22"
//                              }
//                            },
//                            {
//                              "submissionId": 12345680,
//                              "hmrcSubmissionTime": "2016-05-07T16:05:23",
//                              "rtiSubmissionTime": "2016-05-07T16:05:23",
//                              "taxYear": "16-17",
//                              "apprenticeshipLevy": {
//                                "amountDue": 500.00,
//                                "taxMonth": "1",
//                                "amountAllowance": 15000
//                              }
//                            },
//                            {
//                              "submissionId": 12345681,
//                              "hmrcSubmissionTime": "2016-06-07T16:05:23",
//                              "rtiSubmissionTime": "2016-06-07T16:05:23",
//                              "taxYear": "16-17",
//                              "apprenticeshipLevy": {
//                                "amountDue": 1000.00,
//                                "taxMonth": "2",
//                                "amountAllowance": 15000
//                              }
//                            },
//                            {
//                              "submissionId": 12345682,
//                              "hmrcSubmissionTime": "2016-06-15T16:20:23",
//                              "rtiSubmissionTime": "2016-06-15T16:20:23",
//                              "taxYear": "16-17",
//                              "apprenticeshipLevy": {
//                                "amountDue": 200.00,
//                                "taxMonth": "2",
//                                "amountAllowance": 15000
//                              }
//                            },
//                            {
//                              "submissionId": 12345683,
//                              "hmrcSubmissionTime": "2016-07-15T16:05:23",
//                              "rtiSubmissionTime": "2016-07-15T16:05:23",
//                              "taxYear": "16-17",
//                              "inactivePeriod": {
//                                "from": "2016-06-06",
//                                "to": "2016-09-05"
//                              }
//                            },
//                            {
//                              "submissionId": 12345684,
//                              "hmrcSubmissionTime": "2016-10-15T16:05:23",
//                              "rtiSubmissionTime": "2016-10-15T16:05:23",
//                              "taxYear": "16-17",
//                              "finalSubmission": {
//                                "schemeCeased": true,
//                                "schemeCeasedDate": "2016-09-05"
//                              }
//                            }
//                          ]
//                        }""")))
//                        })
//
//        // test
//        val futureResult = connector.eps("123AB12345", ClosedDateRange(new LocalDate(2016,7,1), new LocalDate(2016,7,15)))(hc)
//
//        // check
//        await[EmployerPaymentsSummary](futureResult) shouldBe expected
//      }
    }
  }
}
