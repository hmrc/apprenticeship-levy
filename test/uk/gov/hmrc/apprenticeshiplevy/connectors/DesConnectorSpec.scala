/*
 * Copyright 2025 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{when, reset => mockReset}
import org.mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import uk.gov.hmrc.apprenticeshiplevy.audit.Auditor
import uk.gov.hmrc.apprenticeshiplevy.data.audit.ALAEvent
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import uk.gov.hmrc.apprenticeshiplevy.utils._
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.play.audit.EventKeys._
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent
import views.html.helper

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DesConnectorSpec
  extends AppLevyUnitSpec
    with GuiceOneAppPerSuite
    with ScalaFutures
    with WireMockHelper {

  val baseUrl = "/sandbox/data"
  val mockAuditor: Auditor = mock[Auditor]
  val mockHeaderCarrier: HeaderCarrier = HeaderCarrier(
    otherHeaders = Seq(
      "X-Client-ID" -> "ClientId",
      "Authorization" -> "Bearer ABC",
      "Environment" -> "Test"
    ))

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .configure(
      "microservice.epsOrigPathEnabled" -> true,
      "microservice.services.auth.host" -> "localhost",
      "microservice.services.auth.port" -> server.port(),
      "microservice.services.des.host" -> "localhost",
      "microservice.services.des.port" -> server.port(),
      "microservice.services.stub-des.host" -> "localhost",
      "microservice.services.stub-des.port" -> server.port(),
      "microservice.services.stub-auth.host" -> "localhost",
      "microservice.services.stub-auth.port" -> server.port(),
      "microservice.services.des.env" -> "Test",
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

  lazy val desConnector: LiveDesConnector = app.injector.instanceOf[LiveDesConnector]

  "DES Connector" should {
    "send audit events" in {
      val stubAuditConnector = mock[AuditConnector]
      val eventCaptor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
      when(stubAuditConnector.sendEvent(eventCaptor.capture())(any(), any())).thenReturn(Future.successful(AuditResult.Success))
      val event = ALAEvent("readEmprefDetails", "123AB12345")

      stubAuditConnector.sendEvent(event.toDataEvent(200)(mockHeaderCarrier))

      val auditEvent = eventCaptor.getValue
      auditEvent.auditType shouldBe "ServiceReceivedRequest"
      auditEvent.tags(TransactionName) shouldBe "readEmprefDetails"
      auditEvent.detail("empref") shouldBe "123AB12345"
    }

    "for Fraction Date endpoint" must {
      "when EDH not failing return local date instance of date" in {

        val fractionsCalculationDateUrl = s"$baseUrl/apprenticeship-levy/fraction-calculation-date"
        val fractionCalculationDate = FractionCalculationDate(LocalDate.of(2016, 11, 3))
        val json = Json.toJson[FractionCalculationDate](fractionCalculationDate)
        val stubResponse = ok(json.toString)
        val expectedResponse = LocalDate.of(2016, 11, 3)

        stubGetServer(stubResponse, fractionsCalculationDateUrl)

        val response = await(desConnector.fractionCalculationDate(mockHeaderCarrier, global))

        response shouldBe expectedResponse

        server.verify(
          getRequestedFor(urlEqualTo(fractionsCalculationDateUrl))
            .withHeader("Authorization", equalTo(s"Bearer ABC"))
            .withHeader("X-Client-ID", equalTo("ClientId"))
            .withHeader("Environment", equalTo("Test"))
        )
      }

      "throw an error when upstream returns a 404" in {

        val fractionsCalculationDateUrl = s"$baseUrl/apprenticeship-levy/fraction-calculation-date"
        val stubResponse = notFound()

        stubGetServer(stubResponse, fractionsCalculationDateUrl)

        assertThrows[NotFoundException] {
          await(desConnector.fractionCalculationDate(mockHeaderCarrier, global))
        }

        server.verify(
          getRequestedFor(urlEqualTo(fractionsCalculationDateUrl))
            .withHeader("Authorization", equalTo(s"Bearer ABC"))
            .withHeader("X-Client-ID", equalTo("ClientId"))
            .withHeader("Environment", equalTo("Test"))
        )
      }

      "throw an error when upstream returns a 400" in {

        val fractionsCalculationDateUrl = s"$baseUrl/apprenticeship-levy/fraction-calculation-date"
        val stubResponse = badRequest()

        stubGetServer(stubResponse, fractionsCalculationDateUrl)

        assertThrows[BadRequestException] {
          await(desConnector.fractionCalculationDate(mockHeaderCarrier, global))
        }

        server.verify(
          getRequestedFor(urlEqualTo(fractionsCalculationDateUrl))
            .withHeader("Authorization", equalTo(s"Bearer ABC"))
            .withHeader("X-Client-ID", equalTo("ClientId"))
            .withHeader("Environment", equalTo("Test"))
        )
      }

      "throw an error when upstream returns neither a 404 nor 400" in {

        val fractionsCalculationDateUrl = s"$baseUrl/apprenticeship-levy/fraction-calculation-date"
        val stubResponse = forbidden()

        stubGetServer(stubResponse, fractionsCalculationDateUrl)

        assertThrows[UpstreamErrorResponse] {
          await(desConnector.fractionCalculationDate(mockHeaderCarrier, global))
        }

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

        val localDate = LocalDate.of(2016, 4, 22)
        val dateRange = OpenEarlyDateRange(localDate)
        val dateRangeParams = dateRange.toParams
        val empRef = "123/AB12345"
        val employersUrl = s"$baseUrl/apprenticeship-levy/employers/${helper.urlEncode(empRef)}/fractions?$dateRangeParams"
        val expectedResponse = Fractions("123/AB12345", List(FractionCalculation(localDate, List(Fraction("England", BigDecimal(0.83))))))
        val json = Json.toJson[Fractions](expectedResponse)
        val stubResponse = ok(json.toString)

        stubGetServer(stubResponse, employersUrl)

        val response = await(desConnector.fractions(empRef, dateRange)(mockHeaderCarrier, global))

        response shouldBe expectedResponse

        server.verify(
          getRequestedFor(urlEqualTo(employersUrl))
            .withHeader("Authorization", equalTo(s"Bearer ABC"))
            .withHeader("X-Client-ID", equalTo("ClientId"))
            .withHeader("Environment", equalTo("Test"))
        )
      }

      "throw an exception when upstream returns a 404" in {

        val localDate = LocalDate.of(2016, 4, 22)
        val dateRange = OpenEarlyDateRange(localDate)
        val dateRangeParams = dateRange.toParams
        val empRef = "123/AB12345"
        val employersUrl = s"$baseUrl/apprenticeship-levy/employers/${helper.urlEncode(empRef)}/fractions?$dateRangeParams"
        val stubResponse = notFound()

        stubGetServer(stubResponse, employersUrl)

        assertThrows[NotFoundException] {
          await(desConnector.fractions(empRef, dateRange)(mockHeaderCarrier, global))
        }

        server.verify(
          getRequestedFor(urlEqualTo(employersUrl))
            .withHeader("Authorization", equalTo(s"Bearer ABC"))
            .withHeader("X-Client-ID", equalTo("ClientId"))
            .withHeader("Environment", equalTo("Test"))
        )
      }
      "throw an exception when upstream returns a 400" in {

        val localDate = LocalDate.of(2016, 4, 22)
        val dateRange = OpenEarlyDateRange(localDate)
        val dateRangeParams = dateRange.toParams
        val empRef = "123/AB12345"
        val employersUrl = s"$baseUrl/apprenticeship-levy/employers/${helper.urlEncode(empRef)}/fractions?$dateRangeParams"
        val stubResponse = badRequest()

        stubGetServer(stubResponse, employersUrl)

        assertThrows[BadRequestException] {
          await(desConnector.fractions(empRef, dateRange)(mockHeaderCarrier, global))
        }

        server.verify(
          getRequestedFor(urlEqualTo(employersUrl))
            .withHeader("Authorization", equalTo(s"Bearer ABC"))
            .withHeader("X-Client-ID", equalTo("ClientId"))
            .withHeader("Environment", equalTo("Test"))
        )
      }
      "throw an exception when upstream returns an error that is neither 404 or 400" in {

        val localDate = LocalDate.of(2016, 4, 22)
        val dateRange = OpenEarlyDateRange(localDate)
        val dateRangeParams = dateRange.toParams
        val empRef = "123/AB12345"
        val employersUrl = s"$baseUrl/apprenticeship-levy/employers/${helper.urlEncode(empRef)}/fractions?$dateRangeParams"
        val stubResponse = forbidden()

        stubGetServer(stubResponse, employersUrl)

        assertThrows[UpstreamErrorResponse] {
          await(desConnector.fractions(empRef, dateRange)(mockHeaderCarrier, global))
        }

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
    "support original endpoint url with an empty empref" in {

      val empRef = " "
      val empRefWithSlash = " "
      val localDate = LocalDate.of(2016, 11, 3)
      val dateRange = OpenEarlyDateRange(localDate)
      val dateRangeParams = dateRange.toParams
      val expectedResponse = EmployerPaymentsSummary(empRefWithSlash, List[EmployerPaymentSummary]())
      val employerPaymentsSummaryUrl = s"$baseUrl/rti/employers/${helper.urlEncode(empRef)}/employer-payment-summary?$dateRangeParams"
      val json = Json.toJson[EmployerPaymentsSummary](expectedResponse)
      val stubResponse = ok(json.toString)

      stubGetServer(stubResponse, employerPaymentsSummaryUrl)

      val response = await(desConnector.eps(empRef, dateRange)(mockHeaderCarrier, global))

      response shouldBe expectedResponse

      server.verify(
        getRequestedFor(urlEqualTo(employerPaymentsSummaryUrl))
          .withHeader("Authorization", equalTo(s"Bearer ABC"))
          .withHeader("X-Client-ID", equalTo("ClientId"))
          .withHeader("Environment", equalTo("Test"))
      )
    }
    "support original endpoint url" in {

      val empRef = "123AB12345"
      val empRefWithSlash = "123/AB12345"
      val localDate = LocalDate.of(2016, 11, 3)
      val dateRange = OpenEarlyDateRange(localDate)
      val dateRangeParams = dateRange.toParams
      val expectedResponse = EmployerPaymentsSummary(empRefWithSlash, List[EmployerPaymentSummary]())
      val employerPaymentsSummaryUrl = s"$baseUrl/rti/employers/${helper.urlEncode(empRef)}/employer-payment-summary?$dateRangeParams"
      val json = Json.toJson[EmployerPaymentsSummary](expectedResponse)
      val stubResponse = ok(json.toString)

      stubGetServer(stubResponse, employerPaymentsSummaryUrl)

      val response = await(desConnector.eps(empRef, dateRange)(mockHeaderCarrier, global))

      response shouldBe expectedResponse

      server.verify(
        getRequestedFor(urlEqualTo(employerPaymentsSummaryUrl))
          .withHeader("Authorization", equalTo(s"Bearer ABC"))
          .withHeader("X-Client-ID", equalTo("ClientId"))
          .withHeader("Environment", equalTo("Test"))
      )
    }

    "throw an IllegalArgumentException when an unexpected json is returned from upstream" in {

      val empRef = "123AB12345"
      val localDate = LocalDate.of(2016, 11, 3)
      val dateRange = OpenEarlyDateRange(localDate)
      val dateRangeParams = dateRange.toParams
      val employerPaymentsSummaryUrl = s"$baseUrl/rti/employers/${helper.urlEncode(empRef)}/employer-payment-summary?$dateRangeParams"
      val stubResponse = ok("test")

      stubGetServer(stubResponse, employerPaymentsSummaryUrl)

      assertThrows[IllegalArgumentException] {
        await(desConnector.eps(empRef, dateRange)(mockHeaderCarrier, global))
      }

      server.verify(
        getRequestedFor(urlEqualTo(employerPaymentsSummaryUrl))
          .withHeader("Authorization", equalTo(s"Bearer ABC"))
          .withHeader("X-Client-ID", equalTo("ClientId"))
          .withHeader("Environment", equalTo("Test"))
      )
    }

    "throw a bad request exception when the upstream returns a 400" in {

      val empRef = "123AB12345"
      val localDate = LocalDate.of(2016, 11, 3)
      val dateRange = OpenEarlyDateRange(localDate)
      val dateRangeParams = dateRange.toParams
      val employerPaymentsSummaryUrl = s"$baseUrl/rti/employers/${helper.urlEncode(empRef)}/employer-payment-summary?$dateRangeParams"
      val stubResponse = badRequest()

      stubGetServer(stubResponse, employerPaymentsSummaryUrl)

      assertThrows[BadRequestException] {
        await(desConnector.eps(empRef, dateRange)(mockHeaderCarrier, global))
      }

      server.verify(
        getRequestedFor(urlEqualTo(employerPaymentsSummaryUrl))
          .withHeader("Authorization", equalTo(s"Bearer ABC"))
          .withHeader("X-Client-ID", equalTo("ClientId"))
          .withHeader("Environment", equalTo("Test"))
      )
    }
    "throw a not found exception when the upstream returns a 404" in {

      val empRef = "123AB12345"
      val localDate = LocalDate.of(2016, 11, 3)
      val dateRange = OpenEarlyDateRange(localDate)
      val dateRangeParams = dateRange.toParams
      val employerPaymentsSummaryUrl = s"$baseUrl/rti/employers/${helper.urlEncode(empRef)}/employer-payment-summary?$dateRangeParams"
      val stubResponse = notFound()

      stubGetServer(stubResponse, employerPaymentsSummaryUrl)

      assertThrows[NotFoundException] {
        await(desConnector.eps(empRef, dateRange)(mockHeaderCarrier, global))
      }

      server.verify(
        getRequestedFor(urlEqualTo(employerPaymentsSummaryUrl))
          .withHeader("Authorization", equalTo(s"Bearer ABC"))
          .withHeader("X-Client-ID", equalTo("ClientId"))
          .withHeader("Environment", equalTo("Test"))
      )
    }
    "throw another type of exception when the response is neither a 400 or 404" in {

      val empRef = "123AB12345"
      val localDate = LocalDate.of(2016, 11, 3)
      val dateRange = OpenEarlyDateRange(localDate)
      val dateRangeParams = dateRange.toParams
      val employerPaymentsSummaryUrl = s"$baseUrl/rti/employers/${helper.urlEncode(empRef)}/employer-payment-summary?$dateRangeParams"
      val stubResponse = forbidden()

      stubGetServer(stubResponse, employerPaymentsSummaryUrl)

      assertThrows[UpstreamErrorResponse] {
        await(desConnector.eps(empRef, dateRange)(mockHeaderCarrier, global))
      }

      server.verify(
        getRequestedFor(urlEqualTo(employerPaymentsSummaryUrl))
          .withHeader("Authorization", equalTo(s"Bearer ABC"))
          .withHeader("X-Client-ID", equalTo("ClientId"))
          .withHeader("Environment", equalTo("Test"))
      )
    }

    "have Employer Details endpoint and" should {
      "support original endpoint url" in {

        val empRef = "123AB12345"
        val empRefWithSlash = "123/AB12345"
        val expectedResponse = DesignatoryDetails(Some(empRef), None, None)
        val employerDetailsUrl = s"$baseUrl/paye/employer/$empRefWithSlash/designatory-details"
        val json = Json.toJson[DesignatoryDetails](expectedResponse)
        val stubResponse = ok(json.toString)

        stubGetServer(stubResponse, employerDetailsUrl)

        val response = await(desConnector.designatoryDetails(empRef)(mockHeaderCarrier, global))

        response shouldBe expectedResponse

        server.verify(
          getRequestedFor(urlEqualTo(employerDetailsUrl))
            .withHeader("Authorization", equalTo(s"Bearer ABC"))
            .withHeader("X-Client-ID", equalTo("ClientId"))
            .withHeader("Environment", equalTo("Test"))
        )
      }

      "support original endpoint url with employer links" in {

        val empRef = "123AB12345"
        val empRefWithSlash = "123/AB12345"
        val expectedResponse = DesignatoryDetails(Some(empRef), Some(DesignatoryDetailsData(None, None, None)), None)
        val expectedStubResponse = HodDesignatoryDetailsLinks(Some(DesignatoryDetailsLinks(Some("/paye/employer/123/AB12345/designatory-details"), None)))
        val employerDetailsUrl = s"$baseUrl/paye/employer/$empRefWithSlash/designatory-details"
        val json = Json.toJson[HodDesignatoryDetailsLinks](expectedStubResponse)
        val stubResponse = ok(json.toString)

        stubGetServer(stubResponse, employerDetailsUrl)

        val response = await(desConnector.designatoryDetails(empRef)(mockHeaderCarrier, global))

        response shouldBe expectedResponse

        server.verify(
          getRequestedFor(urlEqualTo(employerDetailsUrl))
            .withHeader("Authorization", equalTo(s"Bearer ABC"))
            .withHeader("X-Client-ID", equalTo("ClientId"))
            .withHeader("Environment", equalTo("Test"))
        )
      }

      "support original endpoint url with communication links" in {

        val empRef = "123AB12345"
        val empRefWithSlash = "123/AB12345"
        val expectedResponse = DesignatoryDetails(Some(empRef), None, Some(DesignatoryDetailsData(None, None, None)))
        val expectedStubResponse = HodDesignatoryDetailsLinks(Some(DesignatoryDetailsLinks(None, Some("/paye/employer/123/AB12345/designatory-details"))))
        val employerDetailsUrl = s"$baseUrl/paye/employer/$empRefWithSlash/designatory-details"
        val json = Json.toJson[HodDesignatoryDetailsLinks](expectedStubResponse)
        val stubResponse = ok(json.toString)
        val getDetailsUrl = "/sandbox/datatest"
        val detailsStubResponse = Json.toJson[DesignatoryDetailsData](DesignatoryDetailsData(None, None, None))
        val detailsStubResponseJson = ok(detailsStubResponse.toString())

        stubGetServer(stubResponse, employerDetailsUrl)
        stubGetServer(detailsStubResponseJson, getDetailsUrl)

        val response = await(desConnector.designatoryDetails(empRef)(mockHeaderCarrier, global))

        response shouldBe expectedResponse

        server.verify(
          getRequestedFor(urlEqualTo(employerDetailsUrl))
            .withHeader("Authorization", equalTo(s"Bearer ABC"))
            .withHeader("X-Client-ID", equalTo("ClientId"))
            .withHeader("Environment", equalTo("Test"))
        )
      }

      "support original endpoint url when the getDetails call fails" in {

        val empRef = "123AB12345"
        val empRefWithSlash = "123/AB12345"
        val expectedResponse = DesignatoryDetails(Some(empRef), None, None)
        val expectedStubResponse = HodDesignatoryDetailsLinks(Some(DesignatoryDetailsLinks(None, Some("test"))))
        val employerDetailsUrl = s"$baseUrl/paye/employer/$empRefWithSlash/designatory-details"
        val json = Json.toJson[HodDesignatoryDetailsLinks](expectedStubResponse)
        val stubResponse = ok(json.toString)

        stubGetServer(stubResponse, employerDetailsUrl)

        val response = await(desConnector.designatoryDetails(empRef)(mockHeaderCarrier, global))

        response shouldBe expectedResponse

        server.verify(
          getRequestedFor(urlEqualTo(employerDetailsUrl))
            .withHeader("Authorization", equalTo(s"Bearer ABC"))
            .withHeader("X-Client-ID", equalTo("ClientId"))
            .withHeader("Environment", equalTo("Test"))
        )
      }
      "throw an error if the http call in getDetails returns a 400" in {

        val empRef = "123AB12345"
        val empRefWithSlash = "123/AB12345"
        val employerDetailsUrl = s"$baseUrl/paye/employer/$empRefWithSlash/designatory-details"
        val stubResponse = badRequest()

        stubGetServer(stubResponse, employerDetailsUrl)

        assertThrows[BadRequestException] {
          await(desConnector.designatoryDetails(empRef)(mockHeaderCarrier, global))
        }

        server.verify(
          getRequestedFor(urlEqualTo(employerDetailsUrl))
            .withHeader("Authorization", equalTo(s"Bearer ABC"))
            .withHeader("X-Client-ID", equalTo("ClientId"))
            .withHeader("Environment", equalTo("Test"))
        )
      }

      "throw an error when the upstream returns a 404" in {

        val empRef = "123AB12345"
        val empRefWithSlash = "123/AB12345"
        val employerDetailsUrl = s"$baseUrl/paye/employer/$empRefWithSlash/designatory-details"
        val stubResponse = notFound()

        stubGetServer(stubResponse, employerDetailsUrl)

        assertThrows[NotFoundException] {
          await(desConnector.designatoryDetails(empRef)(mockHeaderCarrier, global))
        }

        server.verify(
          getRequestedFor(urlEqualTo(employerDetailsUrl))
            .withHeader("Authorization", equalTo(s"Bearer ABC"))
            .withHeader("X-Client-ID", equalTo("ClientId"))
            .withHeader("Environment", equalTo("Test"))
        )
      }

      "throw an error when the upstream returns a 400" in {

        val empRef = "123AB12345"
        val empRefWithSlash = "123/AB12345"
        val employerDetailsUrl = s"$baseUrl/paye/employer/$empRefWithSlash/designatory-details"
        val stubResponse = badRequest()

        stubGetServer(stubResponse, employerDetailsUrl)

        assertThrows[BadRequestException] {
          await(desConnector.designatoryDetails(empRef)(mockHeaderCarrier, global))
        }

        server.verify(
          getRequestedFor(urlEqualTo(employerDetailsUrl))
            .withHeader("Authorization", equalTo(s"Bearer ABC"))
            .withHeader("X-Client-ID", equalTo("ClientId"))
            .withHeader("Environment", equalTo("Test"))
        )
      }
    }

    "throw an error when the upstream neither a 400 nor a 404" in {

      val empRef = "123AB12345"
      val empRefWithSlash = "123/AB12345"
      val employerDetailsUrl = s"$baseUrl/paye/employer/$empRefWithSlash/designatory-details"
      val stubResponse = forbidden()

      stubGetServer(stubResponse, employerDetailsUrl)

      assertThrows[UpstreamErrorResponse] {
        await(desConnector.designatoryDetails(empRef)(mockHeaderCarrier, global))
      }

      server.verify(
        getRequestedFor(urlEqualTo(employerDetailsUrl))
          .withHeader("Authorization", equalTo(s"Bearer ABC"))
          .withHeader("X-Client-ID", equalTo("ClientId"))
          .withHeader("Environment", equalTo("Test"))
      )
    }
  }

  "have Employment Check endpoint and" should {
    "throw a bad request error when a 400 is thrown" in {

      val empRef = "123AB12345"
      val nino = "AA122345A"
      val localDate = LocalDate.of(2016, 11, 3)
      val dateRange = ClosedDateRange(localDate, localDate)
      val dateRangeParams = dateRange.toParams
      val employerDetailsUrl = s"$baseUrl/apprenticeship-levy/employers/${helper.urlEncode(empRef)}/employed/$nino?$dateRangeParams"
      val stubResponse = badRequest()

      stubGetServer(stubResponse, employerDetailsUrl)

      assertThrows[BadRequestException] {
        await(desConnector.check(empRef, "AA122345A", dateRange)(mockHeaderCarrier, global))
      }

      server.verify(
        getRequestedFor(urlEqualTo(employerDetailsUrl))
          .withHeader("Authorization", equalTo(s"Bearer ABC"))
          .withHeader("X-Client-ID", equalTo("ClientId"))
          .withHeader("Environment", equalTo("Test"))
      )
    }

    "return Unknown when a 404 is thrown" in {

      val empRef = "123AB12345"
      val nino = "AA122345A"
      val localDate = LocalDate.of(2016, 11, 3)
      val dateRange = ClosedDateRange(localDate, localDate)
      val dateRangeParams = dateRange.toParams
      val employerDetailsUrl = s"$baseUrl/apprenticeship-levy/employers/${helper.urlEncode(empRef)}/employed/$nino?$dateRangeParams"
      val stubResponse = notFound()

      stubGetServer(stubResponse, employerDetailsUrl)

      val response = await(desConnector.check(empRef, "AA122345A", dateRange)(mockHeaderCarrier, global))

      response shouldBe Unknown

      server.verify(
        getRequestedFor(urlEqualTo(employerDetailsUrl))
          .withHeader("Authorization", equalTo(s"Bearer ABC"))
          .withHeader("X-Client-ID", equalTo("ClientId"))
          .withHeader("Environment", equalTo("Test"))
      )
    }

    "throw an error when neither 400 nor 404 is thrown" in {

      val empRef = "123AB12345"
      val nino = "AA122345A"
      val localDate = LocalDate.of(2016, 11, 3)
      val dateRange = ClosedDateRange(localDate, localDate)
      val dateRangeParams = dateRange.toParams
      val employerDetailsUrl = s"$baseUrl/apprenticeship-levy/employers/${helper.urlEncode(empRef)}/employed/$nino?$dateRangeParams"
      val stubResponse = forbidden()

      stubGetServer(stubResponse, employerDetailsUrl)

      assertThrows[UpstreamErrorResponse] {
        await(desConnector.check(empRef, "AA122345A", dateRange)(mockHeaderCarrier, global))
      }

      server.verify(
        getRequestedFor(urlEqualTo(employerDetailsUrl))
          .withHeader("Authorization", equalTo(s"Bearer ABC"))
          .withHeader("X-Client-ID", equalTo("ClientId"))
          .withHeader("Environment", equalTo("Test"))
      )
    }
  }

  "supply default dates when not specified" in {

    val empRef = "123AB12345"
    val empRefWithSlash = "123/AB12345"
    val localDate = LocalDate.of(2016, 4, 22)
    val dateRange = OpenEarlyDateRange(localDate)
    val employerPaymentsSummaryUrl = s"$baseUrl/rti/employers/${helper.urlEncode(empRef)}/employer-payment-summary?toDate=${localDate.toString}"
    val expectedResponse = EmployerPaymentsSummary(empRefWithSlash, List[EmployerPaymentSummary]())
    val json = Json.toJson[EmployerPaymentsSummary](expectedResponse)
    val stubResponse = ok(json.toString)

    stubGetServer(stubResponse, employerPaymentsSummaryUrl)

    val response = await(desConnector.eps(empRef, dateRange)(mockHeaderCarrier, global))

    response shouldBe expectedResponse

    server.verify(
      getRequestedFor(urlEqualTo(employerPaymentsSummaryUrl))
        .withHeader("Authorization", equalTo(s"Bearer ABC"))
        .withHeader("X-Client-ID", equalTo("ClientId"))
        .withHeader("Environment", equalTo("Test"))
    )
  }

  "with valid and invalid json" must {
    import play.api.libs.json._

    "convert invalid empty json to valid response" in {

      val empRef = "123AB12345"
      val empRefWithSlash = "123/AB12345"
      val localDate = LocalDate.of(2016, 4, 22)
      val dateRange = OpenEarlyDateRange(localDate)
      val employerPaymentsSummaryUrl = s"$baseUrl/rti/employers/${helper.urlEncode(empRef)}/employer-payment-summary?toDate=${localDate.toString}"
      val expectedResponse = EmployerPaymentsSummary(empRefWithSlash, List[EmployerPaymentSummary]())
      val json = Json.parse("""{"empref":"123AB12345"}""")
      val stubResponse = ok(json.toString)

      stubGetServer(stubResponse, employerPaymentsSummaryUrl)

      val response = await(desConnector.eps(empRef, dateRange)(mockHeaderCarrier, global))

      response shouldBe expectedResponse

      server.verify(
        getRequestedFor(urlEqualTo(employerPaymentsSummaryUrl))
          .withHeader("Authorization", equalTo(s"Bearer ABC"))
          .withHeader("X-Client-ID", equalTo("ClientId"))
          .withHeader("Environment", equalTo("Test"))
      )
    }

    "convert invalid bad date-time json values to valid date times" in {

      val empRef = "123AB12345"
      val empRefWithSlash = "123/AB12345"
      val dateFrom = LocalDate.of(2016, 7, 1)
      val dateTo = LocalDate.of(2016, 7, 15)
      val dateRange = ClosedDateRange(dateFrom, dateTo)
      val dateRangeParams = dateRange.toParams
      val employerPaymentsSummaryUrl = s"$baseUrl/rti/employers/${helper.urlEncode(empRef)}/employer-payment-summary?$dateRangeParams"

      val expectedResponse = EmployerPaymentsSummary(
        empRefWithSlash,
        List(
          EmployerPaymentSummary(
            12345678L,
            LocalDateTime.parse("2016-07-14T16:05:44.000"),
            LocalDateTime.parse("2016-07-14T16:05:23.000"), "16-17",
            apprenticeshipLevy = Some(ApprenticeshipLevy(BigDecimal(600.00), BigDecimal(15000), "11")))
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

      val response = await(desConnector.eps(empRef, dateRange)(mockHeaderCarrier, global))

      response shouldBe expectedResponse

      server.verify(
        getRequestedFor(urlEqualTo(employerPaymentsSummaryUrl))
          .withHeader("Authorization", equalTo(s"Bearer ABC"))
          .withHeader("X-Client-ID", equalTo("ClientId"))
          .withHeader("Environment", equalTo("Test"))
      )
    }

    "convert valid json values to valid objects" in {

      val empRef = "123AB12345"
      val dateFrom = LocalDate.of(2016, 7, 1)
      val dateTo = LocalDate.of(2016, 7, 15)
      val dateRange = ClosedDateRange(dateFrom, dateTo)
      val dateRangeParams = dateRange.toParams
      val employerPaymentsSummaryUrl = s"$baseUrl/rti/employers/${helper.urlEncode(empRef)}/employer-payment-summary?$dateRangeParams"

      val expectedResponse =
        EmployerPaymentsSummary("123/AB12345",
          List(
            EmployerPaymentSummary(12345678L, LocalDateTime.parse("2016-07-14T16:05:23.000"), LocalDateTime.parse("2016-07-14T16:05:23.000"), "16-17", apprenticeshipLevy = Some(ApprenticeshipLevy(BigDecimal(600.00), BigDecimal(15000), "11"))),
            EmployerPaymentSummary(12345679L, LocalDateTime.parse("2015-04-07T16:05:23.000"), LocalDateTime.parse("2015-04-07T16:05:23.000"), "15-16", Some(ClosedDateRange(LocalDate.parse("2016-12-13"), LocalDate.parse("2017-03-22")))),
            EmployerPaymentSummary(12345680L, LocalDateTime.parse("2016-05-07T16:05:23.000"), LocalDateTime.parse("2016-05-07T16:05:23.000"), "16-17", apprenticeshipLevy = Some(ApprenticeshipLevy(BigDecimal(500.00), BigDecimal(15000), "1"))),
            EmployerPaymentSummary(12345681L, LocalDateTime.parse("2016-06-07T16:05:23.000"), LocalDateTime.parse("2016-06-07T16:05:23.000"), "16-17", apprenticeshipLevy = Some(ApprenticeshipLevy(BigDecimal(1000.00), BigDecimal(15000), "2"))),
            EmployerPaymentSummary(12345682L, LocalDateTime.parse("2016-06-15T16:20:23.000"), LocalDateTime.parse("2016-06-15T16:20:23.000"), "16-17", apprenticeshipLevy = Some(ApprenticeshipLevy(BigDecimal(200.00), BigDecimal(15000), "2"))),
            EmployerPaymentSummary(12345683L, LocalDateTime.parse("2016-07-15T16:05:23.000"), LocalDateTime.parse("2016-07-15T16:05:23.000"), "16-17", inactivePeriod = Some(ClosedDateRange(LocalDate.parse("2016-06-06"), LocalDate.parse("2016-09-05")))),
            EmployerPaymentSummary(12345684L, LocalDateTime.parse("2016-10-15T16:05:23.000"), LocalDateTime.parse("2016-10-15T16:05:23.000"), "16-17", finalSubmission = Some(SchemeCeased(schemeCeased = true, LocalDate.parse("2016-09-05"), None)))
          )
        )

      val json =
        """{
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
                }"""

      val stubResponse = ok(json)

      stubGetServer(stubResponse, employerPaymentsSummaryUrl)

      val response = await(desConnector.eps(empRef, dateRange)(mockHeaderCarrier, global))

      response shouldBe expectedResponse

      server.verify(
        getRequestedFor(urlEqualTo(employerPaymentsSummaryUrl))
          .withHeader("Authorization", equalTo(s"Bearer ABC"))
          .withHeader("X-Client-ID", equalTo("ClientId"))
          .withHeader("Environment", equalTo("Test"))
      )
    }
  }
}
