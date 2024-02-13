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

package uk.gov.hmrc.apprenticeshiplevy.controllers

import play.mvc.Http.Status._
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

object ErrorResponses {
  def DESError(statusCode: Int, code: String, msg: String): ErrorResponse = ErrorResponse(statusCode, msg, Some(s"DES_ERROR_${code}"))
  def AuthError(statusCode: Int, code: String, msg: String): ErrorResponse = ErrorResponse(statusCode, msg, Some(s"AUTH_ERROR_${code}"))

  object ErrorNotFound extends ErrorResponse(NOT_FOUND, "NOT_FOUND", Some("Resource was not found"))
  object ErrorFromDateAfterToDate extends ErrorResponse(BAD_REQUEST, "BAD_REQUEST", Some("From date was after to date"))
  object ErrorAcceptHeaderInvalid extends ErrorResponse(NOT_ACCEPTABLE, "ACCEPT_HEADER_INVALID", Some("The accept header is missing or invalid"))
}
