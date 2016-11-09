/*
 * Copyright 2016 HM Revenue & Customs
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

import play.api.hal.{HalLink, HalResource}
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.api.controllers.{ErrorResponse, HeaderValidator}
import uk.gov.hmrc.play.microservice.controller.BaseController
import play.api.mvc.{ActionBuilder, Request, Result, Results, RequestHeader}
import play.api.http.Status._
import uk.gov.hmrc.play.http._
import java.io.IOException
import scala.util.Try
import scala.util.matching.Regex
import java.net.URLDecoder

trait ApiController extends BaseController with HeaderValidator {

  implicit class ErrorResponseSyntax(er: ErrorResponse) {
    def result: Result = Status(er.httpStatusCode)(Json.toJson(er))
  }

  override implicit def hc(implicit rh: RequestHeader): HeaderCarrier = super.hc(rh).withExtraHeaders(rh.headers
                                                                                          .toSimpleMap
                                                                                          .filterKeys(_ == "Environment")
                                                                                          .headOption
                                                                                          .getOrElse(("Environment","clone")))

  val withValidAcceptHeader: ActionBuilder[Request] = validateAccept(acceptHeaderValidationRules)

  val desErrorHandler: PartialFunction[Throwable, Result] = {
        case e: BadRequestException => BadRequest(Json.toJson(DESError(SERVICE_UNAVAILABLE, s"Bad request error: ${extractReason(e.getMessage())}")))
        case e: IOException => ServiceUnavailable(Json.toJson(DESError(SERVICE_UNAVAILABLE, s"DES connection error: ${extractReason(e.getMessage())}")))
        case e: GatewayTimeoutException => RequestTimeout(Json.toJson(DESError(REQUEST_TIMEOUT, s"DES not responding error: ${extractReason(e.getMessage())}")))
        case e: NotFoundException => NotFound(Json.toJson(DESError(NOT_FOUND, s"DES endpoint not found: ${extractReason(e.getMessage())}")))
        case e: Upstream5xxResponse => ServiceUnavailable(Json.toJson(DESError(e.reportAs, s"DES 5xx error: ${extractReason(e.getMessage())}")))
        case e: Upstream4xxResponse => {
          e.upstreamResponseCode match {
            case FORBIDDEN => Forbidden(Json.toJson(DESError(e.reportAs, s"DES forbidden error: ${extractReason(e.getMessage())}")))
            case UNAUTHORIZED => Unauthorized(Json.toJson(DESError(e.reportAs, s"DES unauthorised error: ${extractReason(e.getMessage())}")))
            case TOO_MANY_REQUEST => TooManyRequest(Json.toJson(DESError(TOO_MANY_REQUEST, s"DES too many requests: ${extractReason(e.getMessage())}")))
            case REQUEST_TIMEOUT => RequestTimeout(Json.toJson(DESError(REQUEST_TIMEOUT, s"DES not responding error: ${extractReason(e.getMessage())}")))
            case _ => ServiceUnavailable(Json.toJson(DESError(e.reportAs, s"DES 4xx error: ${extractReason(e.getMessage())}")))
          }
        }
        case e => {
          InternalServerError(Json.toJson(DESError(INTERNAL_SERVER_ERROR, s"API or DES internal server error: ${extractReason(e.getMessage())}")))
        }
    }

  val authErrorHandler: PartialFunction[Throwable, Result] = {
        case e: BadRequestException => BadRequest(Json.toJson(AuthError(SERVICE_UNAVAILABLE, s"Bad request error: ${extractReason(e.getMessage())}")))
        case e: IOException => ServiceUnavailable(Json.toJson(AuthError(SERVICE_UNAVAILABLE, s"Auth connection error: ${extractReason(e.getMessage())}")))
        case e: GatewayTimeoutException =>
          RequestTimeout(Json.toJson(AuthError(REQUEST_TIMEOUT, s"Auth not responding error: ${extractReason(e.getMessage())}")))
        case e: NotFoundException => NotFound(Json.toJson(AuthError(NOT_FOUND, s"Auth endpoint not found: ${extractReason(e.getMessage())}")))
        case e: Upstream5xxResponse => ServiceUnavailable(Json.toJson(AuthError(e.reportAs, s"Auth 5xx error: ${extractReason(e.getMessage())}")))
        case e: Upstream4xxResponse => {
          e.upstreamResponseCode match {
            case FORBIDDEN => Forbidden(Json.toJson(AuthError(e.reportAs, s"Auth forbidden error: ${extractReason(e.getMessage())}")))
            case UNAUTHORIZED => Unauthorized(Json.toJson(AuthError(e.reportAs, s"Auth unauthorised error: ${extractReason(e.getMessage())}")))
            case TOO_MANY_REQUEST => TooManyRequest(Json.toJson(AuthError(TOO_MANY_REQUEST, s"Auth too many requests: ${extractReason(e.getMessage())}")))
            case REQUEST_TIMEOUT => RequestTimeout(Json.toJson(AuthError(REQUEST_TIMEOUT, s"Auth not responding error: ${extractReason(e.getMessage())}")))
            case _ => ServiceUnavailable(Json.toJson(AuthError(e.reportAs, s"Auth 4xx error: ${extractReason(e.getMessage())}")))
          }
        }
        case e => {
          InternalServerError(Json.toJson(DESError(INTERNAL_SERVER_ERROR, s"API or Auth internal server error: ${extractReason(e.getMessage())}")))
        }
    }

  def selfLink(url: String): HalLink = HalLink("self", url)

  def ok(hal: HalResource): Result = Ok(Json.toJson(hal)).withHeaders("Content-Type" -> "application/hal+json")

  protected def extractReason(msg: String) = Try(if (msg.contains("Response body")) {
                                                   val str1 = msg.reverse.substring(1).reverse.substring(msg.indexOf("Response body") + 14).trim
                                                   val m = if (str1.startsWith("{")) str1 else str1.substring(str1.indexOf("{"))
                                                   Try((Json.parse(m) \ "reason").as[String]) getOrElse ((Json.parse(m) \ "Reason").as[String])
                                                 } else {
                                                    msg}) getOrElse(msg)

  protected lazy val emprefParts = "^(\\d{3})([^0-9A-Z]*)([0-9A-Z]{1,10})$".r
  protected def toDESFormat(empref: String): String = URLDecoder.decode(empref, "UTF-8") match {
    case emprefParts(part1, _, part2) => part1 + part2
    case _ => empref
  }
}
