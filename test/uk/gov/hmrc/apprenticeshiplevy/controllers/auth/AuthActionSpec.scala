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

package uk.gov.hmrc.apprenticeshiplevy.controllers.auth

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.BodyParsers.Default
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference
import uk.gov.hmrc.apprenticeshiplevy.utils.{AppLevyUnitSpec, RetrievalOps}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{GGCredId, LegacyCredentials, PAClientId, ~}
import uk.gov.hmrc.http.{BadRequestException, GatewayTimeoutException, MethodNotAllowedException, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendBaseController

import java.io.IOException
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthActionSpec
  extends AppLevyUnitSpec
    with GuiceOneAppPerSuite
    with RetrievalOps
    with BeforeAndAfterEach {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val stubComponents: ControllerComponents = stubControllerComponents()
  val defaultParser = new Default(stubComponents.parsers)

  class Harness(authAction: AuthAction) extends BackendBaseController {
    def onPageLoad(): Action[AnyContent] = authAction {
      request =>
        Ok(s"PAYE: ${request.empRef.getOrElse("None found")}")
    }

    override def controllerComponents: ControllerComponents = stubComponents
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector)
  }

  def enrolments(identifiers: Seq[EnrolmentIdentifier] = Seq()): Enrolments =
    Enrolments(
      Set(
        Enrolment(
          key = "IR-PAYE",
          identifiers = identifiers,
          state = ""
        )
      )
    )

  val paRetrieval: Enrolments ~ LegacyCredentials =
    Enrolments(Set()) ~ PAClientId("app-id")

  val ggRetrieval: Enrolments ~ LegacyCredentials =
    enrolments(
      Seq(
        EnrolmentIdentifier("TaxOfficeNumber", "123"),
        EnrolmentIdentifier("TaxOfficeReference", "ABCDEF")
      )
    ) ~ GGCredId("")

  val emptyGGRetrieval: Enrolments ~ LegacyCredentials =
    enrolments() ~ GGCredId("")

  "A user with no active session" should {
    "return UNAUTHORIZED" in {
      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(new SessionRecordNotFound))
      val authAction = new AuthActionImpl(mockAuthConnector, new Default(stubComponents.parsers))
      val controller = new Harness(authAction)
      val result = controller.onPageLoad()(FakeRequest("", ""))
      status(result) shouldBe UNAUTHORIZED
    }
  }

  "An unauthorized user" should {
    def authError(testTitle: String, error: Exception, statusCode: Int): Unit =
      s"return $testTitle" in {
        when(mockAuthConnector.authorise(any(), any())(any(), any()))
          .thenReturn(Future.failed(error))
        val authAction = new AuthActionImpl(mockAuthConnector, new Default(stubComponents.parsers))
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest("", ""))
        status(result) shouldBe statusCode
      }

    val input = Seq(
      ("BadRequestException", new BadRequestException("Bad Request"), BAD_REQUEST),
      ("IOException", new IOException, SERVICE_UNAVAILABLE),
      ("GatewayTimeoutException", new GatewayTimeoutException("timeout"), REQUEST_TIMEOUT),
      ("NotFoundException", new NotFoundException("not found"), NOT_FOUND),
      ("UpstreamErrorResponse with SERVICE_UNAVAILABLE", UpstreamErrorResponse("", SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE), SERVICE_UNAVAILABLE),
      ("UpstreamErrorResponse with FORBIDDEN", UpstreamErrorResponse("", FORBIDDEN, FORBIDDEN), FORBIDDEN),
      ("UpstreamErrorResponse with TOO_MANY_REQUESTS", UpstreamErrorResponse("", TOO_MANY_REQUESTS, TOO_MANY_REQUESTS), TOO_MANY_REQUESTS),
      ("UpstreamErrorResponse with TIMEOUT", UpstreamErrorResponse("", REQUEST_TIMEOUT, REQUEST_TIMEOUT), REQUEST_TIMEOUT),
      ("UpstreamErrorResponse with SERVICE_UNAVAILABLE due to different 4xx error", UpstreamErrorResponse("", BAD_REQUEST, BAD_REQUEST), SERVICE_UNAVAILABLE),
      ("An unknown exception", new MethodNotAllowedException("Method Not Allowed"), INTERNAL_SERVER_ERROR)
    )

    input.foreach(args => authError(args._1, args._2, args._3))
  }

  "A user that is logged in with a PAYE enrolment" must {
    "be allowed access" in {

      val enrolments = Enrolments(Set(
        Enrolment("IR-PAYE", Seq(
          EnrolmentIdentifier("TaxOfficeNumber", "someOffice"),
          EnrolmentIdentifier("TaxOfficeReference", "someRef")),
          "")))

      val retrievalResult: Future[Enrolments] =
        Future.successful(enrolments)

      when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
        .thenReturn(retrievalResult)

      val authAction = new AuthActionImpl(mockAuthConnector, defaultParser)
      val controller = new Harness(authAction)

      val result = controller.onPageLoad()(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include("someOffice/someRef")
    }
  }

  "A user that is logged in without a PAYE enrolment" must {
    "be allowed access" in {

      val enrolments = Enrolments(Set(
        Enrolment("IR-SA", Seq(
          EnrolmentIdentifier("Utr", "someUtr")),
          "")))

      val retrievalResult: Future[Enrolments] =
        Future.successful(enrolments)

      when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
        .thenReturn(retrievalResult)

      val authAction = new AuthActionImpl(mockAuthConnector, defaultParser)
      val controller = new Harness(authAction)

      val result = controller.onPageLoad()(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include("None found")
    }
  }

  "A user that is logged in with an empty PAYE enrolment" must {
    "be allowed access" in {
      val enrolments = Enrolments(Set(Enrolment("IR-PAYE", Seq.empty, "")))
      val retrievalResult: Future[Enrolments] =
        Future.successful(enrolments)

      when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
        .thenReturn(retrievalResult)

      val authAction = new AuthActionImpl(mockAuthConnector, defaultParser)
      val controller = new Harness(authAction)

      val result = controller.onPageLoad()(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include("None found")
    }
  }

  "A user that is logged in using PrivilegedApplication" must {
    "be allowed access" in {

      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val authAction = new PrivilegedAuthActionImpl(mockAuthConnector, defaultParser)
      val controller = new Harness(authAction)

      val result = controller.onPageLoad()(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include("None found")
    }
  }

  "AllProviderAuthAction" must {
    "return an instance of auth action" in {
      new AllProviderAuthActionImpl(mockAuthConnector, defaultParser).apply(EmploymentReference(""))
        .isInstanceOf[AuthAction] shouldBe true
    }

    "authenticate a privileged application" in {
      when(mockAuthConnector.authorise[Enrolments ~ LegacyCredentials](any(), any())(any(), any()))
        .thenReturn(Future.successful(paRetrieval))

      val authAction = new AllProviderAuthActionImpl(mockAuthConnector, defaultParser).apply(EmploymentReference(""))
      val controller = new Harness(authAction)

      val result = controller.onPageLoad()(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include("None found")
    }

    "authenticate an IR-PAYE enrolled user" when {
      "the request emp ref matches their emp ref" in {
        when(mockAuthConnector.authorise[Enrolments ~ LegacyCredentials](any(), any())(any(), any()))
          .thenReturn(Future.successful(ggRetrieval))

        val authAction = new AllProviderAuthActionImpl(mockAuthConnector, defaultParser).apply(EmploymentReference("123/ABCDEF"))
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest())
        status(result) shouldBe OK
        contentAsString(result) should include("123/ABCDEF")
      }
    }

    "return unauthorized for an IR-PAYE enrolled user" when {
      "the request emp ref does not match their emp ref" in {
        when(mockAuthConnector.authorise[Enrolments ~ LegacyCredentials](any(), any())(any(), any()))
          .thenReturn(Future.successful(ggRetrieval))

        val authAction = new AllProviderAuthActionImpl(mockAuthConnector, defaultParser).apply(EmploymentReference("123%2FABCDE"))
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest())
        status(result) shouldBe UNAUTHORIZED
      }

      "their is no emp ref returned" in {
        when(mockAuthConnector.authorise[Enrolments ~ LegacyCredentials](any(), any())(any(), any()))
          .thenReturn(Future.successful(emptyGGRetrieval))

        val authAction = new AllProviderAuthActionImpl(mockAuthConnector, defaultParser).apply(EmploymentReference("123/ABCDEF"))
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest())
        status(result) shouldBe UNAUTHORIZED
      }
    }
  }
}
