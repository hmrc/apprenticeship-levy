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

import play.mvc.Http.Status._
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

object ErrorResponses {
  def DESError(statusCode: Int, code: String, msg: String): ErrorResponse = ErrorResponse(statusCode, msg, Some(s"DES_ERROR_${code}"))
  def AuthError(statusCode: Int, code: String, msg: String): ErrorResponse = ErrorResponse(statusCode, msg, Some(s"AUTH_ERROR_${code}"))

  object ErrorNotFound extends ErrorResponse(NOT_FOUND, "Resource was not found", Some("NOT_FOUND"))
  object ErrorFromDateAfterToDate extends ErrorResponse(BAD_REQUEST, "From date was after to date", Some("BAD_REQUEST"))
  object ErrorAcceptHeaderInvalid extends ErrorResponse(NOT_ACCEPTABLE, "The accept header is missing or invalid", Some("ACCEPT_HEADER_INVALID"))
}
