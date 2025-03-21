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

import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results._
import uk.gov.hmrc.play.bootstrap.backend.http.ErrorResponse

package object sandbox {
  val NOT_FOUND_HTTP_STATUS = 404
  object ErrorNotVisible extends ErrorResponse(NOT_FOUND_HTTP_STATUS, "The provided NINO or EMPREF was not recognised", Some("EPAYE_UNKNOWN"))

  implicit class ErrorResponseSyntax(er: ErrorResponse) {
    def toResult: Result = Status(er.statusCode)(Json.toJson(er))
  }
}
