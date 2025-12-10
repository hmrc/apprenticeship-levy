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

package uk.gov.hmrc.apprenticeshiplevy.controllers

import org.slf4j.MDC
import play.api.Logging
import play.api.mvc.{RequestHeader, Result}
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.apprenticeshiplevy.controllers.ErrorResponses.DESError
import uk.gov.hmrc.apprenticeshiplevy.utils.ErrorResponseUtils
import uk.gov.hmrc.http.*

import java.io.IOException
import java.net.URLDecoder
import scala.util.matching.Regex

trait DesController extends ApiController with Logging {

  val appContext: AppContext

  override implicit def hc(implicit rh: RequestHeader): HeaderCarrier = {
    val hc = super.hc(using rh).withExtraHeaders(("Environment", rh.headers.toSimpleMap.getOrElse("Environment", defaultDESEnvironment)))
    hc.copy(authorization=Some(Authorization(s"Bearer ${defaultDESToken}")))
  }
  // $COVERAGE-OFF$
  protected def defaultDESEnvironment: String = appContext.desEnvironment
  // $COVERAGE-ON$

  protected def defaultDESToken: String = appContext.desToken

  protected def toDESFormat(empref: String): String = URLDecoder.decode(empref, "UTF-8") match {
    case emprefParts(part1, _, part2) => part1 + part2
    case _ => empref
  }

  protected lazy val emprefParts: Regex = "^(\\d{3})([^0-9A-Z]*)([0-9A-Z]{1,10})$".r

  protected val desErrorHandler: PartialFunction[Throwable, Result] = {
    case e: JsValidationException =>
      // $COVERAGE-OFF$
      logger.error(s"Client ${MDC.get("X-Client-ID")} DES returned bad json: ${e.getMessage()}, API returning  code $INTERNAL_SERVER_ERROR")
      // $COVERAGE-ON$
      InternalServerError(ErrorResponseUtils.convertToJson(DESError(INTERNAL_SERVER_ERROR, "JSON_FAILURE", "DES and/or BACKEND server returned bad json.")))
    case e: IllegalArgumentException =>
      // $COVERAGE-OFF$
      logger.error(s"Client ${MDC.get("X-Client-ID")} DES returned bad json: ${e.getMessage}, API returning  code $INTERNAL_SERVER_ERROR")
      // $COVERAGE-ON$
      InternalServerError(ErrorResponseUtils.convertToJson(DESError(INTERNAL_SERVER_ERROR, "JSON_FAILURE", "DES and/or BACKEND server returned bad json.")))
    case e: BadRequestException =>
      logger.warn(s"Client ${MDC.get("X-Client-ID")} DES error: ${e.getMessage}, API returning BadRequest with code $SERVICE_UNAVAILABLE")
      BadRequest(ErrorResponseUtils.convertToJson(DESError(SERVICE_UNAVAILABLE, "BAD_REQUEST", "Bad request error")))
    case e: IOException =>
      // $COVERAGE-OFF$
      logger.error(s"Client ${MDC.get("X-Client-ID")} DES error: ${e.getMessage}, API returning ServiceUnavailable with code $SERVICE_UNAVAILABLE", e)
      // $COVERAGE-ON$
      ServiceUnavailable(ErrorResponseUtils.convertToJson(DESError(SERVICE_UNAVAILABLE, "IO", "DES connection error")))
    case e: GatewayTimeoutException =>
      // $COVERAGE-OFF$
      logger.error(s"Client ${MDC.get("X-Client-ID")} DES error: ${e.getMessage}, API returning RequestTimeout with code $REQUEST_TIMEOUT", e)
      // $COVERAGE-ON$
      RequestTimeout(ErrorResponseUtils.convertToJson(DESError(REQUEST_TIMEOUT, "GATEWAY_TIMEOUT", s"DES not responding error")))
    case e: NotFoundException =>
      // $COVERAGE-OFF$
      logger.warn(s"Client ${MDC.get("X-Client-ID")} DES error: ${e.getMessage}, API returning NotFound with code $NOT_FOUND")
      // $COVERAGE-ON$
      NotFound(ErrorResponseUtils.convertToJson(DESError(NOT_FOUND, "NOT_FOUND", s"DES endpoint or EmpRef not found")))
    case e: UpstreamErrorResponse =>
      // $COVERAGE-OFF$
      logger.warn(s"Client ${MDC.get("X-Client-ID")} DES error: ${e.getMessage} with ${e.statusCode}, API returning code ${e.reportAs}", e)
      // $COVERAGE-ON$
      e.statusCode match {
        case PRECONDITION_FAILED =>
          InternalServerError(ErrorResponseUtils.convertToJson(DESError(420, "BACKEND_FAILURE", s"DES backend error")))
        case FORBIDDEN =>
          Forbidden(ErrorResponseUtils.convertToJson(DESError(e.reportAs, "FORBIDDEN", s"DES forbidden error")))
        case UNAUTHORIZED =>
          Unauthorized(ErrorResponseUtils.convertToJson(DESError(e.reportAs, "UNAUTHORIZED", s"DES unauthorised error")))
        case TOO_MANY_REQUESTS =>
          TooManyRequests(ErrorResponseUtils.convertToJson(DESError(TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", s"DES too many requests")))
        case REQUEST_TIMEOUT =>
          RequestTimeout(ErrorResponseUtils.convertToJson(DESError(REQUEST_TIMEOUT, "TIMEOUT", s"DES not responding error")))
        case _ =>
          if (e.statusCode >= 400 && e.statusCode < 500)
            ServiceUnavailable(ErrorResponseUtils.convertToJson(DESError(e.reportAs, "OTHER", s"DES 4xx error")))
          else
            ServiceUnavailable(ErrorResponseUtils.convertToJson(DESError(e.reportAs, "BACKEND_FAILURE", s"DES 5xx error")))
      }
    case e =>
      // $COVERAGE-OFF$
      logger.error(s"Client ${MDC.get("X-Client-ID")} API error: ${e.getMessage}, API returning code $INTERNAL_SERVER_ERROR", e)
      // $COVERAGE-ON$
      InternalServerError(ErrorResponseUtils.convertToJson(DESError(INTERNAL_SERVER_ERROR, "API", s"API or DES internal server error")))
  }
}
