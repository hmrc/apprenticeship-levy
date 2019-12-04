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

package uk.gov.hmrc.apprenticeshiplevy.controllers.auth

import java.io.IOException

import com.google.inject.{ImplementedBy, Inject}
import org.slf4j.MDC
import play.api.Mode.Mode
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Results.{BadRequest, Forbidden, InternalServerError, NotFound, RequestTimeout, ServiceUnavailable, TooManyRequests, Unauthorized}
import play.api.mvc._
import play.api.{Configuration, Logger, Play}
import uk.gov.hmrc.apprenticeshiplevy.config.WSHttp
import uk.gov.hmrc.apprenticeshiplevy.controllers.AuthError
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.http.{Request => _, _}
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AuthActionImpl @Inject()(val authConnector: AuthConnector)(implicit executionContext: ExecutionContext)
  extends AuthAction with AuthorisedFunctions {

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    authorised(AuthProviders(PrivilegedApplication)).retrieve(Retrievals.allEnrolments) {
      case Enrolments(enrolments) =>
        val payeRef: Option[EmpRef] = enrolments.find(_.key == "IR-PAYE")
          .flatMap { enrolment =>
            val taxOfficeNumber = enrolment.identifiers.find(id => id.key == "TaxOfficeNumber").map(_.value)
            val taxOfficeReference = enrolment.identifiers.find(id => id.key == "TaxOfficeReference").map(_.value)

            (taxOfficeNumber, taxOfficeReference) match {
              case (Some(number), Some(reference)) => Some(EmpRef(number, reference))
              case _ => None
            }
          }
        Future.successful(Right(AuthenticatedRequest(request, payeRef)))
      case _ =>
        Future.successful(Right(AuthenticatedRequest(request, None)))

    }.recover { case e: Throwable => Left(authErrorHandler(e)) }

  }

  private def extractReason(msg: String): String =
    Try(if (msg.contains("Response body")) {
      val str1 = msg.reverse.substring(1).reverse.substring(msg.indexOf("Response body") + 14).trim
      val m = if (str1.startsWith("{")) str1 else str1.substring(str1.indexOf("{"))
      Try((Json.parse(m) \ "reason").as[String]) getOrElse ((Json.parse(m) \ "Reason").as[String])
    } else {msg}) getOrElse(msg)

  private def authErrorHandler(exc: Throwable): Result = {
    println("============== Exception =================: " + exc.getMessage + " " + exc.getClass)
    exc match {
      case e: SessionRecordNotFound =>
        Logger.warn(s"Client ${
          MDC.get("X-Client-ID")
        } API error: ${
          e.getMessage
        }, API returning Unauthorized with code ${
          UNAUTHORIZED
        }")
        Unauthorized(Json.toJson(AuthError(UNAUTHORIZED, "UNAUTHORIZED", s"No active session error: ${
          extractReason(e.getMessage)
        }")))
      case e: BadRequestException =>
        Logger.warn(s"Client ${
          MDC.get("X-Client-ID")
        } API error: ${
          e.getMessage
        }, API returning BadRequest with code ${
          SERVICE_UNAVAILABLE
        }")
        BadRequest(Json.toJson(AuthError(SERVICE_UNAVAILABLE, "BAD_REQUEST", s"Bad request error: ${
          extractReason(e.getMessage)
        }")))
      case e: IOException =>
        Logger.error(s"Client ${
          MDC.get("X-Client-ID")
        } API error: ${
          e.getMessage
        }, API returning ServiceUnavailable with code ${
          SERVICE_UNAVAILABLE
        }", e)
        ServiceUnavailable(Json.toJson(AuthError(SERVICE_UNAVAILABLE, "IO", s"Auth connection error: ${
          extractReason(e.getMessage)
        }")))
      case e: GatewayTimeoutException =>
        Logger.error(s"Client ${
          MDC.get("X-Client-ID")
        } API error: ${
          e.getMessage
        }, API returning RequestTimeout with code ${
          GATEWAY_TIMEOUT
        }", e)
        RequestTimeout(Json.toJson(AuthError(REQUEST_TIMEOUT, "GATEWAY_TIMEOUT", s"Auth not responding error: ${
          extractReason(e.getMessage)
        }")))
      case e: NotFoundException =>
        Logger.warn(s"Client ${
          MDC.get("X-Client-ID")
        } API error: ${
          e.getMessage
        }, API returning NotFound with code ${
          NOT_FOUND
        }")
        NotFound(Json.toJson(AuthError(NOT_FOUND, "NOT_FOUND", s"Auth endpoint not found: ${
          extractReason(e.getMessage)
        }")))
      case e: Upstream5xxResponse =>
        Logger.error(s"Client ${
          MDC.get("X-Client-ID")
        } API error: ${
          e.getMessage
        }, API returning ServiceUnavailable with code ${
          e.reportAs
        }", e)
        ServiceUnavailable(Json.toJson(AuthError(e.reportAs, "BACKEND_FAILURE", s"Auth 5xx error: ${
          extractReason(e.getMessage)
        }")))
      case e: Upstream4xxResponse =>
        Logger.warn(s"Client ${
          MDC.get("X-Client-ID")
        } API error: ${
          e.getMessage
        } with ${
          e.upstreamResponseCode
        }, API returning code ${
          e.reportAs
        }")
        e.upstreamResponseCode match {
          case FORBIDDEN => Forbidden(Json.toJson(AuthError(e.reportAs, "FORBIDDEN", s"Auth forbidden error: ${
            extractReason(e.getMessage)
          }")))
          case UNAUTHORIZED => Unauthorized(Json.toJson(AuthError(e.reportAs, "UNAUTHORIZED", s"Auth unauthorised error: ${
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
      case e: _root_.uk.gov.hmrc.http.JsValidationException =>
        Logger.error(s"Client ${
          MDC.get("X-Client-ID")
        } API error: ${
          e.getMessage
        }, API returning Unauthorized 498, WRONG_TOKEN", e)
        Unauthorized(Json.toJson(AuthError(498, "WRONG_TOKEN", s"Auth unauthorised error: OAUTH 2 User Token Required not TOTP")))
      case e: Throwable =>
        Logger.error(s"Client ${
          MDC.get("X-Client-ID")
        } API error: ${
          e.getMessage
        }, API returning code ${
          INTERNAL_SERVER_ERROR
        }", e)
        InternalServerError(Json.toJson(AuthError(INTERNAL_SERVER_ERROR, "API", s"API or Auth internal server error: ${
          extractReason(e.getMessage)
        }")))
    }
  }

}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction extends ActionBuilder[AuthenticatedRequest] with ActionRefiner[Request, AuthenticatedRequest]

class AuthConnector extends PlayAuthConnector with ServicesConfig {
  override lazy val serviceUrl: String = baseUrl("auth")

  override def http: CorePost = WSHttp

  override protected def mode: Mode = Play.current.mode

  override protected def runModeConfiguration: Configuration = Play.current.configuration
}