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

import org.joda.time.DateTimeConstants.APRIL
import org.joda.time.Months.monthsBetween
import org.joda.time._
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.apprenticeshiplevy.connectors.RTIConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.ErrorResponses.ErrorNotFound
import uk.gov.hmrc.apprenticeshiplevy.data.{LevyDeclaration, LevyDeclarations, PayrollPeriod}
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import uk.gov.hmrc.apprenticeshiplevy.utils.DateRange
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.http.{HeaderCarrier, NotFoundException}

import scala.concurrent.Future

trait LevyDeclarationController {
  self: ApiController =>

  def rtiConnector: RTIConnector

  implicit val dateTimeOrdering: Ordering[LocalDateTime] = Ordering.fromLessThan { (d1, d2) => d1.isBefore(d2) }

  // scalastyle:off
  def declarations(empref: String, fromDate: Option[LocalDate], toDate: Option[LocalDate]) = withValidAcceptHeader.async { implicit request =>
  // scalastyle:on
    retrieveDeclarations(empref, DateRange(fromDate, toDate))
      .map(ds => buildResult(ds.sortBy(_.submissionTime).reverse, empref))
  }

  private[controllers] def retrieveDeclarations(empref: String, dateRange: DateRange)(implicit hc: HeaderCarrier): Future[Seq[LevyDeclaration]] = {

    rtiConnector.eps(empref, dateRange).map { employerPayments =>
      val declarations = employerPayments.eps.collect(convertToDeclaration)
      if (declarations.size != employerPayments.eps.size) {
        Logger.warn("Unable to convert some of the declarations retrieved from EPS.")
      }
      declarations
    }.recover {
      /*
      * The etmp charges call can return 404 if either the empref is unknown or there is no data for the tax year.
      * We don't know which one it might be, so convert a 404 to an empty result. The controller can decide
      * if it wants to return a 404 if all calls to `charges` return no results.
       */
      case t: NotFoundException => Seq.empty
    }
  }

  private[controllers] def buildResult(ds: Seq[LevyDeclaration], empref: String): Result = {
    if (ds.nonEmpty) {
      Ok(Json.toJson(LevyDeclarations(empref, ds)))
    }
    else {
      ErrorNotFound.result
    }
  }

  private[controllers] val convertToDeclaration: PartialFunction[EmployerPaymentSummary, LevyDeclaration] = {
    case EmployerPaymentSummary(id, hmrcSt, rtiSt, ty, Some(dr), _, _, _, _, _) =>
      LevyDeclaration(id,
                      hmrcSt,
                      payrollPeriod = Some(PayrollPeriod(ty, calculateTaxMonth(dr.to))),
                      noPaymentForPeriod = Some(true))
    case EmployerPaymentSummary(id, hmrcSt, rtiSt, ty, _, Some(dr), _, _, _, _) =>
      LevyDeclaration(id,
                      hmrcSt,
                      inactiveFrom = Some(dr.from),
                      inactiveTo = Some(dr.to))
    case EmployerPaymentSummary(id, hmrcSt, rtiSt, ty, _, _, _, _, Some(SchemeCeased(_, schemeCeasedDate, _)), _) =>
      LevyDeclaration(id, hmrcSt, dateCeased = Some(schemeCeasedDate))
    case EmployerPaymentSummary(id, hmrcSt, rtiSt, ty, _, _, _, Some(al), _, _) =>
      LevyDeclaration(id,
                      hmrcSt,
                      payrollPeriod = Some(PayrollPeriod(ty, al.taxMonth.toInt)),
                      levyDueYTD=Some(al.amountDue),
                      levyAllowanceForFullYear=Some(al.amountAllowance))
  }

  val TAX_YEAR_START_DAY = 6
  val BeginningOfTaxYear = new MonthDay(APRIL, TAX_YEAR_START_DAY)

  private[controllers] def calculateTaxMonth(to: LocalDate) = {
    val monthDay = new MonthDay(to.getMonthOfYear, to.getDayOfMonth)
    val yearReference = if (monthDay.isBefore(BeginningOfTaxYear)) to.getYear - 1 else to.getYear
    monthsBetween(new LocalDate(yearReference, APRIL, TAX_YEAR_START_DAY), to).getMonths + 1
  }
}
