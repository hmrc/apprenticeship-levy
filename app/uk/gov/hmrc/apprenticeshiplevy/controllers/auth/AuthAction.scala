/*
 * Copyright 2023 HM Revenue & Customs
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

import com.google.inject.{ImplementedBy, Inject}
import org.slf4j.MDC
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.apprenticeshiplevy.controllers.ErrorResponses.AuthError
import uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference
import uk.gov.hmrc.apprenticeshiplevy.utils.ErrorResponseUtils
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{PAClientId, ~}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.http.{Request => _, _}
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.io.IOException
import scala.concurrent.{ExecutionContext, Future}

class AuthActionImpl @Inject()(val authConnector: AuthConnector, val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
  extends AuthAction with AuthorisedFunctions {

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    authorised().retrieve(Retrievals.allEnrolments) {
      case Enrolments(enrolments) =>
        val payeRef: Option[EmpRef] = EnrolmentHelper.getEmpRef(enrolments)
        Future.successful(Right(AuthenticatedRequest(request, payeRef)))
    }.recover { case e: Throwable => Left(ErrorHandler.authErrorHandler(e)) }

  }
}

class AllProviderAuthActionImpl @Inject()(val authConnector: AuthConnector, bodyParser: BodyParsers.Default)(implicit ec: ExecutionContext)
  extends AuthorisedFunctions with Logging {

  def apply(empRef: EmploymentReference): AuthAction = new AuthAction {

    override def parser: BodyParsers.Default = bodyParser

    override def executionContext: ExecutionContext = ec

    override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
      authorised(
        EnrolmentHelper.enrolmentPredicate or AuthProviders(PrivilegedApplication)
      ).retrieve(
        // warning silenced in build.sbt as credentials does support PrivilegedAccess sessions (PAClientId)
        // https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?spaceKey=GG&title=Retrievals+Reference#RetrievalsReference-credentials
        Retrievals.allEnrolments and Retrievals.authProviderId
      ) {
        case _ ~ PAClientId(_) =>
          Future.successful(Right(AuthenticatedRequest(request, None)))
        case Enrolments(enrolments) ~ _ =>
          val payeRef: Option[EmpRef] = EnrolmentHelper.getEmpRef(enrolments)
          val isCorrectEmpRef: Boolean = payeRef.exists(_.value == empRef.empref)
          if (isCorrectEmpRef) {
            Future.successful(Right(AuthenticatedRequest(request, payeRef)))
          } else {
            logger.warn(s"Unauthorized request of ${empRef.empref} from $payeRef")
            Future.successful(Left(Unauthorized(
              ErrorResponseUtils.convertToJson(AuthError(UNAUTHORIZED, "UNAUTHORIZED", s"Unauthorized request of ${empRef.empref}."))
            )))
          }
      }.recover { case e: Throwable =>
        Left(ErrorHandler.authErrorHandler(e))
      }
    }
  }
}

class PrivilegedAuthActionImpl @Inject()(val authConnector: AuthConnector, val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
  extends AuthAction with AuthorisedFunctions {

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    authorised(AuthProviders(PrivilegedApplication)) {
      Future.successful(Right(AuthenticatedRequest(request, None)))
    }.recover { case e: Throwable => Left(ErrorHandler.authErrorHandler(e)) }

  }
}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction extends ActionBuilder[AuthenticatedRequest, AnyContent] with ActionRefiner[Request, AuthenticatedRequest]

private object EnrolmentHelper {
  private val enrolmentKey: String = "IR-PAYE"
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

private object ErrorHandler extends Logging {
  private def logWarningAboutException(e: Throwable, code: Int, description: String): Unit = {
    val message = s"Client ${
      MDC.get("X-Client-ID")
    } API error: ${
      e.getMessage
    }, API returning $description $code"
    logger.warn(message)
  }

  def authErrorHandler(exc: Throwable): Result = {
    exc match {
      case e: SessionRecordNotFound =>
        logWarningAboutException(e, UNAUTHORIZED, "Unauthorized with code")
        Unauthorized(ErrorResponseUtils.convertToJson(AuthError(UNAUTHORIZED, "UNAUTHORIZED", "No active session error")))
      case e: AuthorisationException =>
        logWarningAboutException(e, UNAUTHORIZED, "Unauthorized with code")
        Unauthorized(ErrorResponseUtils.convertToJson(AuthError(UNAUTHORIZED, "UNAUTHORIZED", "The Authorization token provided wasn't valid")))
      case e: BadRequestException =>
        logWarningAboutException(e, SERVICE_UNAVAILABLE, "BadRequest with code")
        BadRequest(ErrorResponseUtils.convertToJson(AuthError(SERVICE_UNAVAILABLE, "BAD_REQUEST", "Bad request error")))
      case e: IOException =>
        logWarningAboutException(e, SERVICE_UNAVAILABLE, "ServiceUnavailable with code")
        ServiceUnavailable(ErrorResponseUtils.convertToJson(AuthError(SERVICE_UNAVAILABLE, "IO", s"Auth connection error")))
      case e: GatewayTimeoutException =>
        val message = s"Client ${
          MDC.get("X-Client-ID")
        } API error: ${
          e.getMessage
        }, API returning RequestTimeout with code $GATEWAY_TIMEOUT"
        logger.error(message, e)
        RequestTimeout(ErrorResponseUtils.convertToJson(AuthError(REQUEST_TIMEOUT, "GATEWAY_TIMEOUT", "Auth not responding error")))
      case e: NotFoundException =>
        logWarningAboutException(e, NOT_FOUND, "NotFound with code")
        NotFound(ErrorResponseUtils.convertToJson(AuthError(NOT_FOUND, "NOT_FOUND", "Auth endpoint not found")))
      case e: UpstreamErrorResponse =>
        val apiMessage = if (e.statusCode >= 400 && e.statusCode < 500) "API returning code" else "API returning ServiceUnavailable with code"
        val message = s"Client ${
          MDC.get("X-Client-ID")
        } API error: ${
          e.getMessage
        } with ${
          e.statusCode
        }, $apiMessage ${
          e.reportAs
        }"
        logger.warn(message)
        e.statusCode match {
          case FORBIDDEN => Forbidden(ErrorResponseUtils.convertToJson(AuthError(e.reportAs, "FORBIDDEN", s"Auth forbidden error")))
          case TOO_MANY_REQUESTS => TooManyRequests(ErrorResponseUtils.convertToJson(AuthError(TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", s"Auth too many requests")))
          case REQUEST_TIMEOUT => RequestTimeout(ErrorResponseUtils.convertToJson(AuthError(REQUEST_TIMEOUT, "TIMEOUT", s"Auth not responding error")))
          case _ =>
            if (e.statusCode >= 400 && e.statusCode < 500) {
              ServiceUnavailable(ErrorResponseUtils.convertToJson(AuthError(e.reportAs, "OTHER", s"Auth 4xx error")))
            } else {
              ServiceUnavailable(ErrorResponseUtils.convertToJson(AuthError(e.reportAs, "BACKEND_FAILURE", s"Auth 5xx error")))
            }
        }

      case e: Throwable =>
        val message = s"Client ${
          MDC.get("X-Client-ID")
        } API error: ${
          e.getMessage
        }, API returning code $INTERNAL_SERVER_ERROR"
        logger.error(message, e)
        InternalServerError(ErrorResponseUtils.convertToJson(AuthError(INTERNAL_SERVER_ERROR, "API", s"API or Auth internal server error")))
    }
  }
}