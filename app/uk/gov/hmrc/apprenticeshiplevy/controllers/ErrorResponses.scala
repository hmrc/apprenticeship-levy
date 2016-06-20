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

import ErrorResponse.ErrorContentFormat
import play.api.libs.json.Json.format
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results
import play.api.mvc.Results._
import play.mvc.Http.Status._

case class ResponseContents(code: String, message: String) {

  def getStatus: Results.Status = code match {
    case CODE_UNAUTHORIZED => Unauthorized
    case CODE_INVALID_TAX_YEAR => BadRequest
    case CODE_INVALID_EMP_REF => BadRequest
    case CODE_BAD_REQUEST => BadRequest
    case CODE_NOT_FOUND => NotFound
    case CODE_INVALID_HEADER => NotAcceptable
    case CODE_INTERNAL_SERVER_ERROR => InternalServerError
    case CODE_NOT_IMPLEMENTED => NotImplemented
    case CODE_SERVICE_UNAVAILABLE => ServiceUnavailable
  }
}

case class ErrorResponse(httpStatusCode: Int, content: ResponseContents) extends Error {
  lazy val JSON: JsValue = Json.toJson(content)(ErrorContentFormat)
  lazy val ResponseContents = Json.stringify(JSON)
  lazy val Result = Status(httpStatusCode)(JSON)
}

object ErrorResponse {
  implicit val ErrorContentFormat = format[ResponseContents]

  val ErrorUnauthorized = ErrorResponse(UNAUTHORIZED,
    ResponseContents(CODE_UNAUTHORIZED, "Invalid Authentication information provided"))

  val ErrorTaxYearInvalid = ErrorResponse(BAD_REQUEST,
    ResponseContents(CODE_INVALID_TAX_YEAR,
      "Tax Year must be of the form yyyy-yy and the year values must be consecutive. ex. 2012-13"))

  val ErrorEmpRefInvalid = ErrorResponse(BAD_REQUEST,
    ResponseContents(CODE_INVALID_EMP_REF, "EmpRef requires two identifiers separated by a slash"))

  val ErrorGenericBadRequest = ErrorResponse(BAD_REQUEST,
    ResponseContents(CODE_BAD_REQUEST, "Bad Request"))

  val ErrorNotFound = ErrorResponse(NOT_FOUND, ResponseContents("NOT_FOUND", "Resource was not found"))

  val ErrorAcceptHeaderInvalid = ErrorResponse(NOT_ACCEPTABLE,
    ResponseContents(CODE_INVALID_HEADER, "The accept header is missing or invalid"))

  val ErrorInternalServerError = ErrorResponse(INTERNAL_SERVER_ERROR,
    ResponseContents(CODE_INTERNAL_SERVER_ERROR, "Internal server error"))

  val ErrorServiceUnavailable = ErrorResponse(SERVICE_UNAVAILABLE,
    ResponseContents(CODE_SERVICE_UNAVAILABLE, "Could not connect to DES endpoint"))

  val ErrorNotImplemented = ErrorResponse(NOT_IMPLEMENTED,
    ResponseContents(CODE_SERVICE_UNAVAILABLE, "Service endpoint is not implemented"))
}