/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.api.controllers.ErrorResponse

case class DESError(statusCode: Int, code: String, msg: String) extends ErrorResponse(statusCode, s"DES_ERROR_$code", msg)
case class AuthError(statusCode: Int, code: String, msg: String) extends ErrorResponse(statusCode, s"AUTH_ERROR_$code", msg)

object ErrorResponses {
  object ErrorNotFound extends ErrorResponse(NOT_FOUND, "NOT_FOUND", "Resource was not found")
  object ErrorFromDateAfterToDate extends ErrorResponse(BAD_REQUEST, "BAD_REQUEST", "From date was after to date")
}
