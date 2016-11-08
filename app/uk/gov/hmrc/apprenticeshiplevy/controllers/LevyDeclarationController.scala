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

import org.joda.time.Months.monthsBetween
import org.joda.time._
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.apprenticeshiplevy.connectors.DesConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.ErrorResponses.ErrorNotFound
import uk.gov.hmrc.apprenticeshiplevy.data.api.{LevyDeclaration, LevyDeclarations, PayrollPeriod}
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import uk.gov.hmrc.apprenticeshiplevy.utils.{DateRange,ClosedDateRange}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext

import scala.concurrent.Future

trait LevyDeclarationController {
  self: ApiController =>

  def desConnector: DesConnector

  implicit val dateTimeOrdering: Ordering[LocalDateTime] = Ordering.fromLessThan { (d1, d2) => d1.isBefore(d2) }

  // scalastyle:off
  def declarations(empref: String, fromDate: Option[LocalDate], toDate: Option[LocalDate]) = withValidAcceptHeader.async { implicit request =>
  // scalastyle:on
    retrieveDeclarations(empref, toDateRange(fromDate, toDate))
      .map(ds => buildResult(ds.sortBy(_.submissionTime).reverse, empref))
      .recover(desErrorHandler)
  }

  private[controllers] def retrieveDeclarations(empref: String, dateRange: DateRange)(implicit hc: HeaderCarrier): Future[Seq[LevyDeclaration]] = {
    desConnector.eps(empref, dateRange)
      .map( employerPayments => employerPayments.eps.flatMap(EmployerPaymentSummary.toDeclarations(_)))
      .recover {
        /*
        * The etmp charges call can return 404 if either the empref is unknown or there is no data for the tax year.
        * We don't know which one it might be, so convert a 404 to an empty result. The controller can decide
        * if it wants to return a 404 if all calls to `charges` return no results.
         */
        case t: NotFoundException => Seq.empty
      }
  }

  private[controllers] def toDateRange(fromDate: Option[LocalDate], toDate: Option[LocalDate]): DateRange = if (fromDate.isDefined && toDate.isDefined) {
     DateRange(fromDate, toDate)
   } else {
     ClosedDateRange(new LocalDate().minusYears(AppContext.defaultNumberOfDeclarationYears), new LocalDate())
   }

  private[controllers] def buildResult(ds: Seq[LevyDeclaration], empref: String): Result = {
    if (ds.nonEmpty) {
      Ok(Json.toJson(LevyDeclarations(empref, ds)))
    }
    else {
      ErrorNotFound.result
    }
  }
}
