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
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.apprenticeshiplevy.connectors.DesConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.AuthAction
import uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox.ErrorNotVisible
import uk.gov.hmrc.apprenticeshiplevy.data.api.{EmploymentCheck, EmploymentReference, Nino}
import uk.gov.hmrc.apprenticeshiplevy.data.des.{Employed, NotEmployed, Unknown}
import uk.gov.hmrc.apprenticeshiplevy.utils.{ClosedDateRange, ErrorResponseUtils}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

trait EmploymentCheckController extends DesController {

  implicit val executionContext: ExecutionContext

  def desConnector: DesConnector

  val authAction: AuthAction

  // scalastyle:off
  def check(ref: EmploymentReference, ni: Nino, fromDate: LocalDate, toDate: LocalDate): Action[AnyContent] =
    (withValidAcceptHeader andThen authAction).async {
      implicit request =>
        // scalastyle:on
        if (fromDate.isAfter(toDate)) {
          Future.successful(ErrorResponseUtils.errorResponseToResult(ErrorResponses.ErrorFromDateAfterToDate))
        } else {
          desConnector.check(
            toDESFormat(ref.empref), ni.nino, ClosedDateRange(fromDate, toDate)
          ) map {
            case Employed =>
              Ok(Json.toJson(EmploymentCheck(ref.empref, ni.nino, fromDate, toDate, employed = true)))
            case NotEmployed =>
              Ok(Json.toJson(EmploymentCheck(ref.empref, ni.nino, fromDate, toDate, employed = false)))
            case Unknown =>
              ErrorResponseUtils.errorResponseToResult(ErrorNotVisible)
          } recover desErrorHandler
        }
    }
}
