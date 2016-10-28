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

import play.mvc.Http.Status._
import uk.gov.hmrc.api.controllers.ErrorResponse


object ErrorResponses {

  //val CODE_UNAUTHORIZED = "UNAUTHORIZED"
  //val CODE_INVALID_TAX_YEAR = "ERROR_TAX_YEAR_INVALID"
  //val CODE_INVALID_EMP_REF = "ERROR_EMP_REF_INVALID"
  //val CODE_BAD_REQUEST = "BAD_REQUEST"
  //val CODE_NOT_FOUND = "NOT_FOUND"
  //val CODE_INVALID_HEADER = "ACCEPT_HEADER_INVALID"
  //val CODE_INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR"
  //val CODE_NOT_IMPLEMENTED = "NOT_IMPLEMENTED"
  //val CODE_SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE"

  /*
  object ErrorUnauthorized extends ErrorResponse(UNAUTHORIZED, CODE_UNAUTHORIZED, "Invalid Authentication information provided")

  object ErrorTaxYearInvalid extends ErrorResponse(BAD_REQUEST, CODE_INVALID_TAX_YEAR, "Tax Year must be of the form yyyy-yy and the year values must be consecutive. ex. 2012-13")

  object ErrorEmpRefInvalid extends ErrorResponse(BAD_REQUEST, CODE_INVALID_EMP_REF, "EmpRef requires two identifiers separated by a slash")

  object ErrorGenericBadRequest extends ErrorResponse(BAD_REQUEST, CODE_BAD_REQUEST, "Bad Request")
  */
  object ErrorNotFound extends ErrorResponse(NOT_FOUND, "NOT_FOUND", "Resource was not found")
  /*
  object ErrorInternalServerError extends ErrorResponse(INTERNAL_SERVER_ERROR, CODE_INTERNAL_SERVER_ERROR, "Internal server error")

  object ErrorServiceUnavailable extends ErrorResponse(SERVICE_UNAVAILABLE, CODE_SERVICE_UNAVAILABLE, "Could not connect to DES endpoint")

  object ErrorNotImplemented extends ErrorResponse(NOT_IMPLEMENTED, CODE_NOT_IMPLEMENTED, "Service endpoint is not implemented")
  */
}