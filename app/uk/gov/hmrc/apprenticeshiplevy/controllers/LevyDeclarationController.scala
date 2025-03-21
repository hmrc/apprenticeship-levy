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

import org.slf4j.MDC
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.apprenticeshiplevy.connectors.DesConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.ErrorResponses.ErrorNotFound
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.AuthAction
import uk.gov.hmrc.apprenticeshiplevy.data.api.{EmploymentReference, LevyDeclaration, LevyDeclarations}
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import uk.gov.hmrc.apprenticeshiplevy.utils.{ClosedDateRange, DateRange, ErrorResponseUtils}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}

import java.time.LocalDate
import scala.concurrent.Future

trait LevyDeclarationController extends Logging {
  self: DesController =>

  val appContext: AppContext
  def desConnector: DesConnector
  val authAction: AuthAction

  // scalastyle:off
  def declarations(ref: EmploymentReference, fromDate: Option[LocalDate], toDate: Option[LocalDate]): Action[AnyContent] =
    (withValidAcceptHeader andThen authAction).async {
      implicit request =>
        // scalastyle:on
        if (fromDate.isDefined && toDate.isDefined && fromDate.get.isAfter(toDate.get))
          Future.successful(ErrorResponseUtils.errorResponseToResult(ErrorResponses.ErrorFromDateAfterToDate))
        else
          retrieveDeclarations(toDESFormat(ref.empref), toDateRange(fromDate, toDate))
            .map { ds =>
              val results = ds.sortWith { (first: LevyDeclaration, second: LevyDeclaration) =>
                first.submissionTime.isAfter(second.submissionTime) && first.id >= second.id
              }
              buildResult(results, ref.empref)
            }.recover(desErrorHandler)
    }

  private[controllers] def retrieveDeclarations(empref: String, dateRange: DateRange)(implicit hc: HeaderCarrier): Future[Seq[LevyDeclaration]] = {
    desConnector
      .eps(empref, dateRange)
      .map(
        employerPayments =>
          employerPayments.eps.flatMap(EmployerPaymentSummary.toDeclarations)
      )
      .recover {
        /*
        * The etmp charges call can return 404 if either the empref is unknown or there is no data for the tax year.
        * We don't know which one it might be, so convert a 404 to an empty result. The controller can decide
        * if it wants to return a 404 if all calls to `charges` return no results.
         */
        case t: NotFoundException =>
          logger.warn(s"Client ${MDC.get("X-Client-ID")} DES error: ${t.getMessage}, API returning empty sequence")
          Seq.empty
      }
  }

  private[controllers] def toDateRange(fromDate: Option[LocalDate], toDate: Option[LocalDate]): DateRange = (fromDate, toDate) match {
    case (None, None) => ClosedDateRange(LocalDate.now().minusYears(appContext.defaultNumberOfDeclarationYears), LocalDate.now())
    case (Some(from), Some(to)) => ClosedDateRange(from, to)
    case (None, Some(to)) => ClosedDateRange(to.minusYears(appContext.defaultNumberOfDeclarationYears), to)
    case (Some(from), None) => ClosedDateRange(from, LocalDate.now())
  }

  private[controllers] def buildResult(ds: Seq[LevyDeclaration], empref: String): Result = {
    if (ds.nonEmpty) {
      Ok(Json.toJson(LevyDeclarations(empref, ds)))
    }
    else {
      logger.warn(s"Client ${MDC.get("X-Client-ID")} DES returned empty list of declarations: API returning not found")
      ErrorResponseUtils.errorResponseToResult(ErrorNotFound)
    }
  }
}
