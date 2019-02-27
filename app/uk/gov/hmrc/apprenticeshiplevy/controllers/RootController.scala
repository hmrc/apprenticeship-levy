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

package uk.gov.hmrc.apprenticeshiplevy.controllers

import play.api.hal.{Hal, HalLink, HalLinks, HalResource}
import play.api.libs.json.{JsArray, JsObject, JsString, Json}
import uk.gov.hmrc.apprenticeshiplevy.connectors.AuthConnector
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference
import play.api.http.HeaderNames
import play.api.mvc.Result
import uk.gov.hmrc.play.http._
import java.io.IOException

import org.slf4j.MDC
import play.api.Logger
import uk.gov.hmrc.apprenticeshiplevy.utils.DecodePath.decodeAnyDoubleEncoding
import uk.gov.hmrc.http.{BadRequestException, GatewayTimeoutException, NotFoundException, Upstream4xxResponse, Upstream5xxResponse}

trait RootController extends ApiController {
  def authConnector: AuthConnector

  def rootUrl: String

  def emprefUrl(empref: EmploymentReference): String

  // Hook to allow post-processing of the links, specifically for sandbox handling
  def processLink(l: HalLink): HalLink = identity(l)

  // scalastyle:off
  def root = withValidAcceptHeader.async { implicit request =>
  // scalastyle:on
    authConnector.getEmprefs.map(es => ok(transformEmpRefs(es))).recover(authErrorHandler)
  }

  private[controllers] val authErrorHandler: PartialFunction[Throwable, Result] = {
        case e: BadRequestException => {
          Logger.warn(s"Client ${MDC.get("X-Client-ID")} API error: ${e.getMessage()}, API returning BadRequest with code ${SERVICE_UNAVAILABLE}")
          BadRequest(Json.toJson(AuthError(SERVICE_UNAVAILABLE, "BAD_REQUEST", s"Bad request error: ${extractReason(e.getMessage())}")))
        }
        case e: IOException => {
          Logger.error(s"Client ${MDC.get("X-Client-ID")} API error: ${e.getMessage()}, API returning ServiceUnavailable with code ${SERVICE_UNAVAILABLE}", e)
          ServiceUnavailable(Json.toJson(AuthError(SERVICE_UNAVAILABLE, "IO", s"Auth connection error: ${extractReason(e.getMessage())}")))
        }
        case e: GatewayTimeoutException => {
          Logger.error(s"Client ${MDC.get("X-Client-ID")} API error: ${e.getMessage()}, API returning RequestTimeout with code ${GATEWAY_TIMEOUT}", e)
          RequestTimeout(Json.toJson(AuthError(REQUEST_TIMEOUT, "GATEWAY_TIMEOUT", s"Auth not responding error: ${extractReason(e.getMessage())}")))
        }
        case e: NotFoundException => {
          Logger.warn(s"Client ${MDC.get("X-Client-ID")} API error: ${e.getMessage()}, API returning NotFound with code ${NOT_FOUND}")
          NotFound(Json.toJson(AuthError(NOT_FOUND, "NOT_FOUND", s"Auth endpoint not found: ${extractReason(e.getMessage())}")))
        }
        case e: Upstream5xxResponse => {
          Logger.error(s"Client ${MDC.get("X-Client-ID")} API error: ${e.getMessage()}, API returning ServiceUnavailable with code ${e.reportAs}", e)
          ServiceUnavailable(Json.toJson(AuthError(e.reportAs, "BACKEND_FAILURE", s"Auth 5xx error: ${extractReason(e.getMessage())}")))
        }
        case e: Upstream4xxResponse => {
          Logger.warn(s"Client ${MDC.get("X-Client-ID")} API error: ${e.getMessage()} with ${e.upstreamResponseCode}, API returning code ${e.reportAs}")
          e.upstreamResponseCode match {
            case FORBIDDEN => Forbidden(Json.toJson(AuthError(e.reportAs, "FORBIDDEN", s"Auth forbidden error: ${extractReason(e.getMessage())}")))
            case UNAUTHORIZED => Unauthorized(Json.toJson(AuthError(e.reportAs, "UNAUTHORIZED", s"Auth unauthorised error: ${extractReason(e.getMessage())}")))
            case TOO_MANY_REQUESTS => TooManyRequests(Json.toJson(AuthError(TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", s"Auth too many requests: ${extractReason(e.getMessage())}")))
            case REQUEST_TIMEOUT => RequestTimeout(Json.toJson(AuthError(REQUEST_TIMEOUT, "TIMEOUT", s"Auth not responding error: ${extractReason(e.getMessage())}")))
            case _ => ServiceUnavailable(Json.toJson(AuthError(e.reportAs, "OTHER", s"Auth 4xx error: ${extractReason(e.getMessage())}")))
          }
        }
        case e: _root_.uk.gov.hmrc.http.JsValidationException => {
          Logger.error(s"Client ${MDC.get("X-Client-ID")} API error: ${e.getMessage()}, API returning Unauthorized 498, WRONG_TOKEN", e)
          Unauthorized(Json.toJson(AuthError(498, "WRONG_TOKEN", s"Auth unauthorised error: OAUTH 2 User Token Required not TOTP")))
        }
        case e => {
          Logger.error(s"Client ${MDC.get("X-Client-ID")} API error: ${e.getMessage()}, API returning code ${INTERNAL_SERVER_ERROR}", e)
          InternalServerError(Json.toJson(AuthError(INTERNAL_SERVER_ERROR, "API", s"API or Auth internal server error: ${extractReason(e.getMessage())}")))
        }
    }

  private[controllers] def transformEmpRefs(empRefs: Seq[String]): HalResource = {
    val links = selfLink(decodeAnyDoubleEncoding(rootUrl)) +: empRefs.map(empref => HalLink(empref, decodeAnyDoubleEncoding(emprefUrl(EmploymentReference(empref)))))
    val body = Json.toJson(Map("emprefs" -> empRefs)).as[JsObject]

    HalResource(HalLinks(links.map(processLink).toVector), body)
  }
}
