/*
 * Copyright 2020 HM Revenue & Customs
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

import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.apprenticeshiplevy.connectors.DesConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox.ErrorNotVisible
import uk.gov.hmrc.apprenticeshiplevy.data.des.{Employed, NotEmployed, Unknown}
import uk.gov.hmrc.apprenticeshiplevy.data.api.{EmploymentCheck, EmploymentReference, Nino}
import uk.gov.hmrc.apprenticeshiplevy.utils.ClosedDateRange
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future
import play.api.mvc.Result
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.AuthAction

trait EmploymentCheckController extends DesController {

  def desConnector: DesConnector
  val authAction: AuthAction

  // scalastyle:off
  def check(ref: EmploymentReference, ni: Nino, fromDate: LocalDate, toDate: LocalDate) = (withValidAcceptHeader andThen authAction).async { implicit request =>
  // scalastyle:on
    if (fromDate.isAfter(toDate)) {
      Future.successful(ErrorResponses.ErrorFromDateAfterToDate.result)
    } else {
      desConnector.check(toDESFormat(ref.empref), ni.nino, ClosedDateRange(fromDate, toDate)).map {
        case Employed => Ok(Json.toJson(EmploymentCheck(ref.empref, ni.nino, fromDate, toDate, employed = true)))
        case NotEmployed => Ok(Json.toJson(EmploymentCheck(ref.empref, ni.nino, fromDate, toDate, employed = false)))
        case Unknown => ErrorNotVisible.toResult
      } recover desErrorHandler
    }
  }
}
