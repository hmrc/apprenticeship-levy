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

package uk.gov.hmrc.apprenticeshiplevy.controllers.auth

import java.io.IOException

import com.google.inject.{ImplementedBy, Inject}
import org.slf4j.MDC
import play.api.Mode.Mode
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import play.api.{Configuration, Logger, Play}
import uk.gov.hmrc.apprenticeshiplevy.config.WSHttp
import uk.gov.hmrc.apprenticeshiplevy.controllers.AuthError
import uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{PAClientId, ~}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.http.{Request => _, _}
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AuthActionImpl @Inject()(val authConnector: AuthConnector)(implicit executionContext: ExecutionContext)
  extends AuthAction with AuthorisedFunctions {

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, None)

    authorised().retrieve(Retrievals.allEnrolments) {
      case Enrolments(enrolments) =>
        val payeRef: Option[EmpRef] = EnrolmentHelper.getEmpRef(enrolments)
        Future.successful(Right(AuthenticatedRequest(request, payeRef)))
    }.recover { case e: Throwable => Left(ErrorHandler.authErrorHandler(e)) }

  }
}

class AllProviderAuthActionImpl @Inject()(val authConnector: AuthConnector)(implicit executionContext: ExecutionContext)
  extends AuthorisedFunctions {

  def apply(empRef: EmploymentReference): AuthAction = new AuthAction {
    override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, None)
      implicit val ec: ExecutionContext = executionContext
      authorised(EnrolmentHelper.enrolmentPredicate or AuthProviders(PrivilegedApplication)).retrieve(Retrievals.allEnrolments and Retrievals.authProviderId) {
        case _ ~ PAClientId(_) =>
          Future.successful(Right(AuthenticatedRequest(request, None)))
        case Enrolments(enrolments) ~ _ =>
          val payeRef: Option[EmpRef] = EnrolmentHelper.getEmpRef(enrolments)
          val isCorrectEmpRef: Boolean = payeRef.exists(_.value == empRef.empref)
          if(isCorrectEmpRef) {
            Future.successful(Right(AuthenticatedRequest(request, payeRef)))
          } else {
            Logger.warn(s"Unauthorized request of ${empRef.empref} from $payeRef")
            Future.successful(Left(Unauthorized(
              Json.toJson(AuthError(UNAUTHORIZED, "UNAUTHORIZED", s"Unauthorized request of ${empRef.empref}."))
            )))
          }
      }.recover { case e: Throwable => Left(ErrorHandler.authErrorHandler(e)) }
    }
  }
}

class PrivilegedAuthActionImpl @Inject()(val authConnector: AuthConnector)(implicit executionContext: ExecutionContext)
  extends AuthAction with AuthorisedFunctions {

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, None)

    authorised(AuthProviders(PrivilegedApplication)) {
      Future.successful(Right(AuthenticatedRequest(request, None)))
    }.recover { case e: Throwable => Left(ErrorHandler.authErrorHandler(e)) }

  }
}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction extends ActionBuilder[AuthenticatedRequest] with ActionRefiner[Request, AuthenticatedRequest]

private object EnrolmentHelper {
  val enrolmentKey: String = "IR-PAYE"
  val enrolmentPredicate: Enrolment = Enrolment(enrolmentKey)
  def getEmpRef(enrolments: Set[Enrolment]): Option[EmpRef] = enrolments.find(_.key == enrolmentKey)
    .flatMap { enrolment =>
      val taxOfficeNumber = enrolment.identifiers.find(id => id.key == "TaxOfficeNumber").map(_.value)
      val taxOfficeReference = enrolment.identifiers.find(id => id.key == "TaxOfficeReference").map(_.value)

      (taxOfficeNumber, taxOfficeReference) match {
        case (Some(number), Some(reference)) => Some(EmpRef(number, reference))
        case _ => None
      }
    }
}

private object ErrorHandler {
  private def extractReason(msg: String): String =
    Try(if (msg.contains("Response body")) {
      val str1 = msg.reverse.substring(1).reverse.substring(msg.indexOf("Response body") + 14).trim
      val m = if (str1.startsWith("{")) str1 else str1.substring(str1.indexOf("{"))
      Try((Json.parse(m) \ "reason").as[String]) getOrElse (Json.parse(m) \ "Reason").as[String]
    } else {
      msg
    }) getOrElse msg

  private def logWarningAboutException(e: Throwable, code: Int, description: String): Unit = {
    val message = s"Client ${
      MDC.get("X-Client-ID")
    } API error: ${
      e.getMessage
    }, API returning $description ${
      code
    }"
    Logger.warn(message)
  }

  def authErrorHandler(exc: Throwable): Result = {
    exc match {
      case e: SessionRecordNotFound =>
        logWarningAboutException(e, UNAUTHORIZED, "Unauthorized with code")
        Unauthorized(Json.toJson(AuthError(UNAUTHORIZED, "UNAUTHORIZED", s"No active session error: ${
          extractReason(e.getMessage)
        }")))
      case e: AuthorisationException =>
        logWarningAboutException(e, UNAUTHORIZED, "Unauthorized with code")
        Unauthorized(Json.toJson(AuthError(UNAUTHORIZED, "UNAUTHORIZED", s"${
          extractReason(e.getMessage)
        }")))
      case e: BadRequestException =>
        logWarningAboutException(e, SERVICE_UNAVAILABLE, "BadRequest with code")
        BadRequest(Json.toJson(AuthError(SERVICE_UNAVAILABLE, "BAD_REQUEST", s"Bad request error: ${
          extractReason(e.getMessage)
        }")))
      case e: IOException =>
        logWarningAboutException(e, SERVICE_UNAVAILABLE, "ServiceUnavailable with code")
        ServiceUnavailable(Json.toJson(AuthError(SERVICE_UNAVAILABLE, "IO", s"Auth connection error: ${
          extractReason(e.getMessage)
        }")))
      case e: GatewayTimeoutException =>
        val message = s"Client ${
          MDC.get("X-Client-ID")
        } API error: ${
          e.getMessage
        }, API returning RequestTimeout with code ${
          GATEWAY_TIMEOUT
        }"
        Logger.error(message, e)
        RequestTimeout(Json.toJson(AuthError(REQUEST_TIMEOUT, "GATEWAY_TIMEOUT", s"Auth not responding error: ${
          extractReason(e.getMessage)
        }")))
      case e: NotFoundException =>
        logWarningAboutException(e, NOT_FOUND, "NotFound with code")
        NotFound(Json.toJson(AuthError(NOT_FOUND, "NOT_FOUND", s"Auth endpoint not found: ${
          extractReason(e.getMessage)
        }")))
      case e: Upstream5xxResponse =>
        val message = s"Client ${
          MDC.get("X-Client-ID")
        } API error: ${
          e.getMessage
        }, API returning ServiceUnavailable with code ${
          e.reportAs
        }"
        Logger.error(message, e)
        ServiceUnavailable(Json.toJson(AuthError(e.reportAs, "BACKEND_FAILURE", s"Auth 5xx error: ${
          extractReason(e.getMessage)
        }")))
      case e: Upstream4xxResponse =>
        val message = s"Client ${
          MDC.get("X-Client-ID")
        } API error: ${
          e.getMessage
        } with ${
          e.upstreamResponseCode
        }, API returning code ${
          e.reportAs
        }"
        Logger.warn(message)
        e.upstreamResponseCode match {
          case FORBIDDEN => Forbidden(Json.toJson(AuthError(e.reportAs, "FORBIDDEN", s"Auth forbidden error: ${
            extractReason(e.getMessage)
          }")))
          case TOO_MANY_REQUESTS => TooManyRequests(Json.toJson(AuthError(TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", s"Auth too many requests: ${
            extractReason(e.getMessage)
          }")))
          case REQUEST_TIMEOUT => RequestTimeout(Json.toJson(AuthError(REQUEST_TIMEOUT, "TIMEOUT", s"Auth not responding error: ${
            extractReason(e.getMessage)
          }")))
          case _ => ServiceUnavailable(Json.toJson(AuthError(e.reportAs, "OTHER", s"Auth 4xx error: ${
            extractReason(e.getMessage)
          }")))
        }
      case e: Throwable =>
        val message = s"Client ${
          MDC.get("X-Client-ID")
        } API error: ${
          e.getMessage
        }, API returning code ${
          INTERNAL_SERVER_ERROR
        }"
        Logger.error(message, e)
        InternalServerError(Json.toJson(AuthError(INTERNAL_SERVER_ERROR, "API", s"API or Auth internal server error: ${
          extractReason(e.getMessage)
        }")))
    }
  }
}

class AuthConnector extends PlayAuthConnector with ServicesConfig {
  override lazy val serviceUrl: String = baseUrl("auth")

  override def http: CorePost = WSHttp

  override protected def mode: Mode = Play.current.mode

  override protected def runModeConfiguration: Configuration = Play.current.configuration
}
