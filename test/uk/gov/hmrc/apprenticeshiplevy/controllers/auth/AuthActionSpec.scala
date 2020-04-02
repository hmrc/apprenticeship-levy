/*
 * Copyright 2020 HM Revenue & Customs
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

import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{OK, UNAUTHORIZED}
import play.api.mvc.{Action, AnyContent, Controller}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference
import uk.gov.hmrc.apprenticeshiplevy.utils.RetrievalOps
import uk.gov.hmrc.auth.core.retrieve.{Credentials, GGCredId, LegacyCredentials, PAClientId, Retrieval, ~}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthActionSpec extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar with RetrievalOps {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  class Harness(authAction: AuthAction) extends Controller {
    def onPageLoad(): Action[AnyContent] = authAction { request => Ok(s"PAYE: ${request.empRef.getOrElse("None found")}") }
  }

  "A user with no active session" should {
    "return UNAUTHORIZED" in {
      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(new SessionRecordNotFound))
      val authAction = new AuthActionImpl(mockAuthConnector)
      val controller = new Harness(authAction)
      val result = controller.onPageLoad()(FakeRequest("", ""))
      status(result) shouldBe UNAUTHORIZED
    }
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

      when(mockAuthConnector.authorise[Enrolments](any(),any())(any(), any()))
        .thenReturn(retrievalResult)

      val authAction = new AuthActionImpl(mockAuthConnector)
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

      when(mockAuthConnector.authorise[Enrolments](any(),any())(any(), any()))
        .thenReturn(retrievalResult)

      val authAction = new AuthActionImpl(mockAuthConnector)
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

      when(mockAuthConnector.authorise[Enrolments](any(),any())(any(), any()))
        .thenReturn(retrievalResult)

      val authAction = new AuthActionImpl(mockAuthConnector)
      val controller = new Harness(authAction)

      val result = controller.onPageLoad()(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include("None found")
    }
  }

  "A user that is logged in using PrivilegedApplication" must {
    "be allowed access" in {

      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(())

      val authAction = new PrivilegedAuthActionImpl(mockAuthConnector)
      val controller = new Harness(authAction)

      val result = controller.onPageLoad()(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include("None found")
    }
  }

  "AllProviderAuthAction" must {
    "return an instance of auth action" in {
      new AllProviderAuthActionImpl(mockAuthConnector).apply(EmploymentReference(""))
        .isInstanceOf[AuthAction] shouldBe true
    }

    "authenticate a privileged application" in {
      when(mockAuthConnector.authorise[Enrolments ~ LegacyCredentials](any(), any())(any(), any()))
        .thenReturn(paRetrieval)

      val authAction = new AllProviderAuthActionImpl(mockAuthConnector).apply(EmploymentReference(""))
      val controller = new Harness(authAction)

      val result = controller.onPageLoad()(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include("None found")
    }

    "authenticate an IR-PAYE enrolled user" when {
      "the request emp ref matches their emp ref" in {
        when(mockAuthConnector.authorise[Enrolments ~ LegacyCredentials](any(), any())(any(), any()))
          .thenReturn(ggRetrieval)

        val authAction = new AllProviderAuthActionImpl(mockAuthConnector).apply(EmploymentReference("123/ABCDEF"))
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest())
        status(result) shouldBe OK
        contentAsString(result) should include("123/ABCDEF")
      }
    }

    "return unauthorized for an IR-PAYE enrolled user" when {
      "the request emp ref does not match their emp ref" in {
        when(mockAuthConnector.authorise[Enrolments ~ LegacyCredentials](any(), any())(any(), any()))
          .thenReturn(ggRetrieval)

        val authAction = new AllProviderAuthActionImpl(mockAuthConnector).apply(EmploymentReference("123%2FABCDE"))
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest())
        status(result) shouldBe UNAUTHORIZED
      }

      "their is no emp ref returned" in {
        when(mockAuthConnector.authorise[Enrolments ~ LegacyCredentials](any(), any())(any(), any()))
          .thenReturn(emptyGGRetrieval)

        val authAction = new AllProviderAuthActionImpl(mockAuthConnector).apply(EmploymentReference("123/ABCDEF"))
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(FakeRequest())
        status(result) shouldBe UNAUTHORIZED
      }
    }
  }

  val enrolments: Enrolments = Enrolments(Set(
    Enrolment("IR-PAYE", Seq(
      EnrolmentIdentifier("TaxOfficeNumber", "123"),
      EnrolmentIdentifier("TaxOfficeReference", "ABCDEF")),
      "")
  ))
  val paRetrieval: Enrolments ~ LegacyCredentials =
    new ~(Enrolments(Set()), PAClientId("app-id"))

  val ggRetrieval: Enrolments ~ LegacyCredentials =
    new ~(enrolments, GGCredId(""))

  val emptyGGRetrieval: Enrolments ~ LegacyCredentials =
    new ~(Enrolments(Set(Enrolment("IR-PAYE", Seq(), ""))), GGCredId(""))


}
